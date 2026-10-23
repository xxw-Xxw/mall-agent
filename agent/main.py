import json
import time
from datetime import datetime

import httpx
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import StreamingResponse
from langchain_core.messages import AIMessageChunk, HumanMessage
from pydantic import BaseModel

import tools
from chroma_client import embed_texts
from defenses import strip_recommend
from graph import graph, llm, saver
from reindex import reindex as run_reindex
from settings import settings

app = FastAPI(title="mall-agent")
app.add_middleware(CORSMiddleware, allow_origins=["*"], allow_methods=["*"], allow_headers=["*"])


class ChatRequest(BaseModel):
    sessionId: str | None = None
    message: str | None = None


def _sse(event, data):
    return f"event: {event}\ndata: {json.dumps(data, ensure_ascii=False)}\n\n"


@app.post("/agent/chat")
async def chat(req: ChatRequest):
    session_id = req.sessionId or "default"
    message = req.message or ""
    config = {"configurable": {"thread_id": session_id}, "recursion_limit": 15}

    async def gen():
        try:
            async for chunk, meta in graph.astream(
                    {"messages": [HumanMessage(content=message)]}, config, stream_mode="messages"):
                if not isinstance(chunk, AIMessageChunk):
                    continue
                if meta.get("langgraph_node") != "agent":
                    continue
                piece = chunk.content
                if isinstance(piece, str) and piece:
                    yield _sse("delta", {"text": piece})
            state = await graph.aget_state(config)
            goods = state.values.get("recommend_goods") or []
            if goods:
                yield _sse("goods", goods)
            yield _sse("done", {"ok": True})
        except Exception:
            yield _sse("error", {"message": "小橙开小差了，请稍后再试～"})

    return StreamingResponse(gen(), media_type="text/event-stream",
                             headers={"Cache-Control": "no-cache"})


@app.post("/agent/chat/sync")
async def chat_sync(req: ChatRequest):
    session_id = req.sessionId or "default"
    message = req.message or ""
    config = {"configurable": {"thread_id": session_id}, "recursion_limit": 15}
    start = time.time()
    try:
        await graph.ainvoke({"messages": [HumanMessage(content=message)]}, config)
        state = await graph.aget_state(config)
        msgs = state.values.get("messages") or []
        content = msgs[-1].content if msgs else ""
        return {"success": True, "reply": strip_recommend(content),
                "goods": state.values.get("recommend_goods") or [],
                "costMs": int((time.time() - start) * 1000)}
    except Exception as e:
        return {"success": False, "reply": "小橙开小差了，请稍后再试～", "error": str(e),
                "costMs": int((time.time() - start) * 1000)}


@app.post("/agent/reindex")
async def reindex(enhance: bool = True):
    return await run_reindex(enhance)


@app.delete("/agent/session/{session_id}")
async def clear_session(session_id: str):
    try:
        graph.delete_thread({"configurable": {"thread_id": session_id}})
    except Exception:
        try:
            saver.storage.pop(session_id, None)
        except Exception:
            pass
    return {"success": True}


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


@app.get("/agent/debug/semantic")
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
