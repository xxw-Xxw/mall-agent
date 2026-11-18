import asyncio
import json
import logging
import re
import time
from contextlib import asynccontextmanager
from datetime import datetime, timedelta

import httpx
from fastapi import Depends, FastAPI, Header
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import StreamingResponse
from langchain_core.messages import AIMessage, AIMessageChunk, HumanMessage, ToolMessage
from pydantic import BaseModel

import goods_client
import graph as agent_graph
import tools
from checkpoint import create_checkpointer
from chroma_client import embed_texts
from defenses import recommend_ids, strip_recommend
from graph import llm
from jwt_auth import require_admin
from member_auth import resolve_member, resolve_thread
from reindex import delete_goods, reconcile as run_reconcile, reindex as run_reindex, upsert_goods
from settings import settings

logging.basicConfig(level=settings.log_level.upper(), format="%(asctime)s %(levelname)s %(message)s")
log = logging.getLogger("mall-agent")


def _seconds_until(hour):
    now = datetime.now()
    target = now.replace(hour=hour, minute=0, second=0, microsecond=0)
    if target <= now:
        target += timedelta(days=1)
    return (target - now).total_seconds()


async def _reconcile_loop():
    """每日定时对账：补齐漏同步/变更商品，并删除已下架或已删除商品的向量。"""
    while True:
        await asyncio.sleep(_seconds_until(settings.reconcile_hour))
        try:
            result = await run_reconcile()
            log.info("定时向量对账完成: %s", result)
        except Exception as e:
            log.warning("定时向量对账异常: %s", e)


@asynccontextmanager
async def lifespan(app):
    task = None
    # 先挂载持久化 checkpointer 再对外服务：重启后能接着用之前的会话上下文
    async with create_checkpointer() as checkpointer:
        agent_graph.init_graph(checkpointer)
        if settings.reconcile_enabled:
            task = asyncio.create_task(_reconcile_loop())
            log.info("定时向量对账已开启，每天 %02d:00 执行", settings.reconcile_hour)
        try:
            yield
        finally:
            if task:
                task.cancel()


app = FastAPI(title="mall-agent", lifespan=lifespan)
app.add_middleware(CORSMiddleware, allow_origins=settings.cors_origin_list, allow_methods=["*"], allow_headers=["*"],
                   expose_headers=["X-Anon-Id"])


class ChatRequest(BaseModel):
    anonId: str | None = None
    message: str | None = None


def _sse(event, data):
    return f"event: {event}\ndata: {json.dumps(data, ensure_ascii=False)}\n\n"


def _identity_headers(anon_generated: str | None) -> dict:
    headers = {"Cache-Control": "no-cache"}
    if anon_generated:
        headers["X-Anon-Id"] = anon_generated
    return headers


@app.post("/agent/chat")
async def chat(req: ChatRequest, authorization: str | None = Header(default=None)):
    member = await resolve_member(authorization)
    thread_id, anon_generated = resolve_thread(member, req.anonId)
    message = req.message or ""
    config = {"configurable": {"thread_id": thread_id}, "recursion_limit": 15}

    async def gen():
        # 缓冲制：工具执行前的 agent 轮次（抢答）只缓冲不外发；工具执行完毕后
        # 最终轮统一流式输出；全程无工具的普通对话在流结束时一次性吐出缓冲。
        pending = []
        sent = []
        tools_ran = False
        sent_any = False
        try:
            async for chunk, meta in agent_graph.graph.astream(
                    {"messages": [HumanMessage(content=message)]}, config, stream_mode="messages"):
                node = meta.get("langgraph_node")
                if isinstance(chunk, ToolMessage):
                    if node == "tools":
                        tools_ran = True
                        pending.clear()
                    continue
                if not isinstance(chunk, AIMessageChunk) or node != "agent":
                    continue
                piece = chunk.content
                if not (isinstance(piece, str) and piece):
                    continue
                if tools_ran:
                    sent_any = True
                    sent.append(piece)
                    yield _sse("delta", {"text": piece})
                else:
                    pending.append(piece)
            if not sent_any and pending:
                sent.extend(pending)
                yield _sse("delta", {"text": "".join(pending)})
            state = await agent_graph.graph.aget_state(config)
            msgs = state.values.get("messages") or []
            final = strip_recommend(msgs[-1].content or "") if msgs else ""
            if final and final != strip_recommend("".join(sent)):
                # 模型回答脱离检索结果时 postprocess 会重写，这里把纠正后的正文补发给前端
                yield _sse("replace", {"text": final})
            goods = state.values.get("recommend_goods") or []
            if goods:
                yield _sse("goods", goods)
            yield _sse("done", {"ok": True})
        except Exception:
            yield _sse("error", {"message": "小橙开小差了，请稍后再试～"})

    return StreamingResponse(gen(), media_type="text/event-stream",
                             headers=_identity_headers(anon_generated))


@app.post("/agent/chat/sync")
async def chat_sync(req: ChatRequest, authorization: str | None = Header(default=None)):
    member = await resolve_member(authorization)
    thread_id, _ = resolve_thread(member, req.anonId)
    message = req.message or ""
    config = {"configurable": {"thread_id": thread_id}, "recursion_limit": 15}
    start = time.time()
    try:
        await agent_graph.graph.ainvoke({"messages": [HumanMessage(content=message)]}, config)
        state = await agent_graph.graph.aget_state(config)
        msgs = state.values.get("messages") or []
        content = msgs[-1].content if msgs else ""
        return {"success": True, "reply": strip_recommend(content),
                "goods": state.values.get("recommend_goods") or [],
                "costMs": int((time.time() - start) * 1000)}
    except Exception as e:
        return {"success": False, "reply": "小橙开小差了，请稍后再试～", "error": str(e),
                "costMs": int((time.time() - start) * 1000)}


@app.post("/agent/reindex", dependencies=[Depends(require_admin)])
async def reindex(enhance: bool = True):
    return await run_reindex(enhance)


@app.post("/agent/goods/{goods_id}/index", dependencies=[Depends(require_admin)])
async def index_goods(goods_id: int):
    """单个商品增量入向量库（商品新增/编辑后由商城后端调用）。"""
    return await upsert_goods(goods_id)


@app.delete("/agent/goods/{goods_id}/index", dependencies=[Depends(require_admin)])
async def remove_goods_index(goods_id: int):
    """单个商品从向量库移除（商品删除/下架后由商城后端调用）。"""
    return delete_goods(goods_id)


@app.post("/agent/reconcile", dependencies=[Depends(require_admin)])
async def reconcile():
    """手动触发向量库对账（定时任务之外的兜底入口，也在启动排障时用）。"""
    return await run_reconcile()


@app.delete("/agent/session/{session_id}", dependencies=[Depends(require_admin)])
async def clear_session(session_id: str):
    try:
        await agent_graph.graph.checkpointer.adelete_thread(session_id)
    except Exception as e:
        log.warning("清理会话失败: %s", e)
    return {"success": True}


async def _history_goods(text: str) -> list:
    """把历史回答末尾的 RECOMMEND 行还原成商品卡片数据。"""
    ids = recommend_ids(text)[:5]
    if not ids:
        return []
    results = await asyncio.gather(*[goods_client.get_goods(i) for i in ids], return_exceptions=True)
    goods = []
    for item in results:
        if isinstance(item, Exception):
            continue
        data = (item or {}).get("data")
        if isinstance(data, dict):
            detail = data.get("goods")
            goods.append(detail if isinstance(detail, dict) else data)
    return goods


_USER_SUFFIX = re.compile(r"（(?:指「.*?」|序号无法对应到推荐列表)）\s*$")


def _clean_user_text(text: str) -> str:
    """序号轮会把解析结果拼进用户消息，展示历史时去掉这段系统附加说明。"""
    return _USER_SUFFIX.sub("", text or "").strip()


@app.get("/agent/history")
async def history(limit: int = 50, anonId: str | None = None,
                  authorization: str | None = Header(default=None)):
    """恢复当前身份的会话历史：会员按 memberId，匿名按 anonId。"""
    member = await resolve_member(authorization)
    thread_id, anon_generated = resolve_thread(member, anonId)
    state = await agent_graph.graph.aget_state({"configurable": {"thread_id": thread_id}})
    values = state.values or {}
    messages = values.get("messages") or []
    # 每条 AI 消息实际展示过的卡片，按消息 id 持久化，刷新后原样还原
    shown_goods = values.get("history_goods") or {}

    out = []
    for i, m in enumerate(messages):
        if isinstance(m, ToolMessage):
            continue
        text = m.content if isinstance(m.content, str) else ""
        if not text.strip():
            continue
        if isinstance(m, HumanMessage):
            out.append({"role": "user", "text": _clean_user_text(text)})
        elif isinstance(m, AIMessage):
            if getattr(m, "tool_calls", None):
                # 工具调用轮的旁白（"请稍等，我帮您查一下"）不是最终回答，
                # 实时流里已被缓冲丢弃，历史里也要剔除，否则刷新后会多出一个气泡
                continue
            # 本轮后面还有更终局的回答（模型先反问、被强制补检索后重答）时，
            # 这条只是中间态，实时流不展示，历史里同样丢弃
            rest = []
            for n in messages[i + 1:]:
                if isinstance(n, HumanMessage):
                    break
                rest.append(n)
            if any(isinstance(n, AIMessage) and not getattr(n, "tool_calls", None) for n in rest):
                continue
            # _raw 保留 RECOMMEND 行用于兜底回查，返回前会移除
            out.append({"role": "ai", "text": strip_recommend(text), "goods": [],
                        "msgId": m.id or "", "_raw": text})

    out = out[-max(1, limit):]
    # 老会话没有持久化卡片时，才按 RECOMMEND 行回查商品；回查限制并发，避免打满商城接口
    sem = asyncio.Semaphore(8)

    async def fill(item):
        cached = shown_goods.get(item.get("msgId")) if item.get("msgId") else None
        if cached:
            item["goods"] = cached
            return
        async with sem:
            item["goods"] = await _history_goods(item.pop("_raw", ""))

    await asyncio.gather(*[fill(item) for item in out if item["role"] == "ai"])
    for item in out:
        item.pop("msgId", None)
        item.pop("_raw", None)
    return {"success": True, "messages": out, "anonId": anon_generated}


@app.get("/agent/health")
async def health():
    async with httpx.AsyncClient(timeout=10.0) as c:
        chroma = "down"
        try:
            await c.get(settings.chroma_base_url + "/api/v2/heartbeat")
            chroma = "ok"
        except Exception:
            try:
                await c.get(settings.chroma_base_url + "/api/v1/heartbeat")
                chroma = "ok(v1)"
            except Exception:
                chroma = "down"
    try:
        await llm.ainvoke([HumanMessage(content="回复：OK")])
        llm_status = "ok"
    except Exception as e:
        llm_status = "down: " + _brief(e)
    try:
        await embed_texts(["ping"])
        emb = "ok"
    except Exception as e:
        emb = "unavailable: " + _brief(e)
    return {"time": datetime.now().isoformat(), "chroma": chroma, "llm": llm_status, "embedding": emb}


@app.get("/agent/debug/semantic", dependencies=[Depends(require_admin)])
async def debug_semantic(q: str, minPrice: float | None = None, maxPrice: float | None = None):
    args = {"query": q}
    if maxPrice is not None:
        args["maxPrice"] = maxPrice
    if minPrice is not None:
        args["minPrice"] = minPrice
    return {"result": await tools.search_goods_semantic.ainvoke(args)}


def _brief(e):
    cur, msg = e, str(e) or repr(e)
    guard = 0
    while getattr(cur, "__cause__", None) is not None and guard < 5:
        cur = cur.__cause__
        if str(cur):
            msg = str(cur)
        guard += 1
    return " ".join(msg.splitlines())[:120]
