import asyncio
import hashlib
import re
import time

from langchain_core.messages import HumanMessage

import goods_client
from chroma_client import collection, embed_texts, invalidate_bm25
from defenses import to_float, to_int
from graph import llm

ENHANCE_PROMPT = (
    "你是电商文案专家。根据下面的商品信息写一段不超过100字的中文卖点介绍，突出适用人群、使用场景和核心优点。\n"
    "要求：只能依据给定信息，不得编造参数或功能；不要出现价格；直接输出文案正文，不要任何前缀或说明。\n"
    "商品信息：{info}"
)

_sem = asyncio.Semaphore(6)


async def _enhance_description(g):
    try:
        info = (f"名称={g.get('goodsName')}，品牌={g.get('brand')}，分类ID={g.get('categoryId')}，"
                f"销量={g.get('sales')}，简介={g.get('description')}")
        async with _sem:
            content = await asyncio.wait_for(
                llm.ainvoke([HumanMessage(content=ENHANCE_PROMPT.format(info=info))]), 180)
        text = (content.content or "").strip()
        if text.startswith("```"):
            text = re.sub(r"^```[a-z]*", "", text)
            text = re.sub(r"```\s*$", "", text).strip()
        return text or None
    except Exception:
        return None


async def _load_category_names():
    names = {}
    try:
        resp = await goods_client.get_categories()
        for g in resp.get("data") or []:
            pid = to_int(g.get("categoryId"))
            pname = str(g.get("categoryName"))
            names[pid] = pname
            for c in g.get("children") or []:
                names[to_int(c.get("categoryId"))] = f"{pname}/{c.get('categoryName')}"
    except Exception:
        pass
    return names


_HASH_FIELDS = ("goodsName", "brand", "categoryId", "price", "sales", "description", "updateTime")


def _src_hash(g):
    """商品源数据指纹：任一字段变更即视为需要重新入库。"""
    raw = "|".join("" if g.get(k) is None else str(g.get(k)) for k in _HASH_FIELDS)
    return hashlib.sha1(raw.encode("utf-8")).hexdigest()


def _same_basic(md, g):
    """存量向量没有指纹时，仅比对基础字段，避免首次对账把全部商品重算一遍卖点。"""
    try:
        return (str(md.get("goodsName")) == str(g.get("goodsName"))
                and str(md.get("brand")) == str(g.get("brand"))
                and to_int(md.get("categoryId")) == to_int(g.get("categoryId"))
                and abs(to_float(md.get("price")) - to_float(g.get("price"))) < 1e-6
                and to_int(md.get("sales")) == to_int(g.get("sales")))
    except Exception:
        return False


def _build_doc(g, cats, selling):
    goods_id = to_int(g.get("goodsId"))
    name = str(g.get("goodsName"))
    brand = str(g.get("brand"))
    category_id = to_int(g.get("categoryId"))
    price = to_float(g.get("price"))
    sales = to_int(g.get("sales"))
    description = "" if g.get("description") is None else str(g.get("description"))

    text = (f"商品名称：{name}\n品牌：{brand}\n分类：{cats.get(category_id, '')}\n"
            f"价格：{price} 元\n销量：{sales}\n商品简介：{description}\n")
    if selling:
        text += f"卖点介绍：{selling}"

    metadata = {
        "goodsId": goods_id,
        "goodsName": name,
        "brand": brand,
        "categoryId": category_id,
        "price": price,
        "sales": sales,
        "srcHash": _src_hash(g),
    }
    return f"goods-{goods_id}", text, metadata


async def reindex(enhance=True):
    try:
        cats = await _load_category_names()
        resp = await goods_client.list_goods(None, None, 500)
        rows = resp.get("rows") or []
        selling_list = [None] * len(rows)
        if enhance and rows:
            selling_list = await asyncio.gather(*[_enhance_description(r) for r in rows])
        ids, texts, metas = [], [], []
        enhanced = 0
        for g, selling in zip(rows, selling_list):
            if selling:
                enhanced += 1
            doc_id, text, md = _build_doc(g, cats, selling)
            ids.append(doc_id)
            texts.append(text)
            metas.append(md)
        embeddings = await embed_texts(texts)
        for i in range(0, len(ids), 50):
            collection.upsert(
                ids=ids[i:i + 50],
                embeddings=embeddings[i:i + 50],
                documents=texts[i:i + 50],
                metadatas=metas[i:i + 50],
            )
        invalidate_bm25()
        return {"success": True, "total": len(ids), "enhanced": enhanced,
                "message": "已全量灌入商品向量库（集合 mall_goods）"}
    except Exception as e:
        return {"success": False, "error": f"灌库失败：{str(e)[:200]}"}


async def _index_row(g, cats):
    """对已拿到的商品字典做卖点生成 + 向量入库；下架或已删除则移除向量。"""
    goods_id = to_int(g.get("goodsId"))
    status = g.get("status")
    if (status is not None and str(status) != "0") or str(g.get("delFlag")) == "2":
        return delete_goods(goods_id)
    selling = await _enhance_description(g)
    doc_id, text, md = _build_doc(g, cats, selling)
    emb = (await embed_texts([text]))[0]
    collection.upsert(
        ids=[doc_id],
        embeddings=[emb],
        documents=[text],
        metadatas=[md],
    )
    invalidate_bm25()
    return {"success": True, "goodsId": goods_id, "action": "upsert"}


async def upsert_goods(goods_id: int):
    """单个商品增量入库；下架、删除或不存在的商品直接从向量库移除。"""
    try:
        resp = await goods_client.get_goods(goods_id)
        data = resp.get("data") or {}
        g = data.get("goods") if isinstance(data, dict) else None
        if not g and isinstance(data, dict) and data.get("goodsId"):
            g = data
        if not g:
            return delete_goods(goods_id)
        cats = await _load_category_names()
        return await _index_row(g, cats)
    except Exception as e:
        return {"success": False, "goodsId": goods_id, "error": f"增量入库失败：{str(e)[:200]}"}


def delete_goods(goods_id: int):
    """从向量库删除单个商品（幂等，不存在时为空操作）。"""
    try:
        collection.delete(ids=[f"goods-{goods_id}"])
        invalidate_bm25()
        return {"success": True, "goodsId": goods_id, "action": "delete"}
    except Exception as e:
        return {"success": False, "goodsId": goods_id, "error": f"删除向量失败：{str(e)[:200]}"}


async def _fetch_selling_goods(page_size=200):
    """分页拉全在售商品（接口只返回 status=0 且未删除）。"""
    rows, page, total = [], 1, 0
    while page <= 50:
        resp = await goods_client.list_goods_page(page, page_size)
        batch = resp.get("rows") or []
        total = to_int(resp.get("total")) or total
        if not batch:
            break
        rows.extend(batch)
        if total and len(rows) >= total:
            break
        page += 1
    return rows


_reconcile_lock = asyncio.Lock()


async def reconcile():
    """定时对账兜底：补齐缺失/变更商品，删除已下架或已删除商品的向量。"""
    if _reconcile_lock.locked():
        return {"success": False, "message": "对账任务正在执行中，请稍后再试"}
    async with _reconcile_lock:
        start = time.time()
        try:
            cats = await _load_category_names()
            rows = await _fetch_selling_goods()
            snapshot = collection.get(include=["metadatas"])
            existing = {doc_id: (md or {})
                        for doc_id, md in zip(snapshot.get("ids") or [], snapshot.get("metadatas") or [])}

            keep, added, refreshed, adopted, deleted, failed = set(), 0, 0, 0, 0, []
            for g in rows:
                goods_id = to_int(g.get("goodsId"))
                if not goods_id:
                    continue
                doc_id = f"goods-{goods_id}"
                keep.add(doc_id)
                md = existing.get(doc_id)
                hash_now = _src_hash(g)
                if md is None:
                    r = await _index_row(g, cats)
                    added += 1 if r.get("success") else 0
                    if not r.get("success"):
                        failed.append(goods_id)
                    continue
                if md.get("srcHash") == hash_now:
                    continue
                if not md.get("srcHash") and _same_basic(md, g):
                    # 存量向量：只补指纹，不重新生成卖点
                    collection.update(ids=[doc_id], metadatas={**md, "srcHash": hash_now})
                    adopted += 1
                    continue
                r = await _index_row(g, cats)
                refreshed += 1 if r.get("success") else 0
                if not r.get("success"):
                    failed.append(goods_id)

            for doc_id in existing:
                if doc_id not in keep:
                    collection.delete(ids=[doc_id])
                    deleted += 1
            if deleted:
                invalidate_bm25()

            return {"success": True, "total": len(rows), "added": added, "refreshed": refreshed,
                    "adopted": adopted, "deleted": deleted, "failed": failed,
                    "costMs": int((time.time() - start) * 1000)}
        except Exception as e:
            return {"success": False, "error": f"对账失败：{str(e)[:200]}",
                    "costMs": int((time.time() - start) * 1000)}

