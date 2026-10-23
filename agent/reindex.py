import asyncio
import re

from langchain_core.messages import HumanMessage

import goods_client
from chroma_client import collection, embed_texts
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
        return {"success": True, "total": len(ids), "enhanced": enhanced,
                "message": "已全量灌入商品向量库（集合 mall_goods）"}
    except Exception as e:
        return {"success": False, "error": f"灌库失败：{str(e)[:200]}"}
