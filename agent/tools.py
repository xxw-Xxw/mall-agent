import asyncio
import json
import re

from langchain_core.tools import tool

import goods_client
from chroma_client import hybrid_search
from defenses import category_matches, named_category_segment_in, to_float, to_int

_category_cache = None
_lock = asyncio.Lock()


async def category_names():
    global _category_cache
    if _category_cache is not None:
        return _category_cache
    async with _lock:
        if _category_cache is not None:
            return _category_cache
        loaded = {}
        try:
            resp = await goods_client.get_categories()
            for g in resp.get("data") or []:
                pid = to_int(g.get("categoryId"))
                pname = str(g.get("categoryName"))
                loaded[pid] = pname
                for c in g.get("children") or []:
                    loaded[to_int(c.get("categoryId"))] = f"{pname}/{c.get('categoryName')}"
        except Exception:
            pass
        _category_cache = loaded
        return loaded


async def category_of(category_id):
    cats = await category_names()
    return cats.get(to_int(category_id), "")


async def named_category_segment(query):
    if not query or not query.strip():
        return None
    return named_category_segment_in(query, await category_names())


def _dumps(obj):
    return json.dumps(obj, ensure_ascii=False)


def _err(msg, e):
    detail = str(e) or type(e).__name__
    return _dumps({"error": f"{msg}：{detail}"})


async def _compact(g):
    cats = await category_names()
    return {
        "goodsId": g.get("goodsId"),
        "goodsName": g.get("goodsName"),
        "brand": g.get("brand"),
        "price": g.get("price"),
        "sales": g.get("sales"),
        "categoryId": g.get("categoryId"),
        "category": cats.get(to_int(g.get("categoryId")), ""),
    }


_STOPWORDS = re.compile(r"我想|我要|想买|购买|买|要|请|帮我|推荐|一下|一个|几款|几件|几|款|件|什么|好的|好|的|吗|呢|啊|吧|元|钱|以内|以下|左右|大概|有没有|有|卖|商城|可以|能|我|你")
_SCENEWORDS = re.compile(r"送|礼|妈妈|爸爸|妈|爸|学生|办公|户外|露营|通勤|解乏|性价比|生日|节日|搭配|场合|老人|孩子|儿童|男生|女生|男|女|居家|旅行")


def concrete_term(text):
    """提取查询里的具体商品词；含场景/人群词时返回空串，不做词面校验。"""
    t = re.sub(r"[^\u4e00-\u9fff]", "", str(text or ""))
    if _SCENEWORDS.search(t):
        return ""
    t = _STOPWORDS.sub("", t)
    return t if 2 <= len(t) <= 6 else ""


def relevance_filter(query, rows, seg=None):
    if seg:
        return rows
    term = concrete_term(query)
    if not term:
        return rows
    return [r for r in rows if _lexical_overlap(term, r)]


async def semantic_rows(query, min_price=None, max_price=None):
    hits = await hybrid_search(query, min_price, max_price, top_k=10)
    cats = await category_names()
    wanted = await named_category_segment(query)
    out = []
    for text, md in hits:
        category = cats.get(to_int(md.get("categoryId")), "")
        if not category_matches(wanted, category, md.get("goodsName")):
            continue
        snippet = text or ""
        out.append({
            "goodsId": md.get("goodsId"),
            "goodsName": md.get("goodsName"),
            "brand": md.get("brand"),
            "categoryId": md.get("categoryId"),
            "category": category,
            "price": md.get("price"),
            "sales": md.get("sales"),
            "snippet": snippet[:150] + "..." if len(snippet) > 150 else snippet,
        })
    return relevance_filter(query, out, wanted)


def _tokens(text):
    t = (str(text or "")).lower()
    toks = set(re.findall(r"[a-z0-9]{2,}", t))
    han = re.sub(r"[^\u4e00-\u9fff]", "", t)
    toks |= {han[i:i + 2] for i in range(len(han) - 1)}
    return toks


def _lexical_overlap(query, row):
    """语义兜底时做词面校验，避免把毫不相关的品类当成结果返回。"""
    q = _tokens(query)
    d = _tokens((row.get("goodsName") or "") + (row.get("brand") or "") + (row.get("category") or ""))
    return bool(q & d)


async def search_goods_impl(keyword, min_price=None, max_price=None):
    try:
        resp = await goods_client.list_goods(keyword, None, 200)
        out = []
        for r in resp.get("rows") or []:
            price = to_float(r.get("price"))
            if min_price is not None and price < min_price:
                continue
            if max_price is not None and price > max_price:
                continue
            out.append(await _compact(r))
        result = {}
        if not out and keyword and keyword.strip():
            seg = await named_category_segment(keyword)
            if seg:
                out = await semantic_rows(seg, min_price, max_price)
            else:
                out = relevance_filter(keyword, await semantic_rows(keyword, min_price, max_price), seg)
            result["matchedBy"] = "semantic"
            result["note"] = "关键词没有精确匹配，以下为同品类或语义相近商品，可直接推荐；只有与用户所需品类明显不符时才说明暂无"
        else:
            result["matchedBy"] = "keyword"
        result["count"] = len(out)
        result["goods"] = out[:8]
        return _dumps(result)
    except Exception as e:
        return _err("商品搜索失败", e)


async def search_goods_semantic_impl(query, max_price=None, min_price=None):
    try:
        out = await semantic_rows(query, min_price, max_price)
        return _dumps({"count": len(out), "goods": out})
    except Exception as e:
        return _err("语义检索暂不可用（可能向量库未灌入数据，或向量化服务异常）", e)


@tool("searchGoods", description="按关键词搜索商城在售商品。用户提到具体商品名、品牌或明确商品类型（如手机、耳机）时优先使用；预算通过 maxPrice/minPrice 参数传入，不作为选择本工具的依据")
async def search_goods(keyword, minPrice=None, maxPrice=None):
    """按关键词搜索商城在售商品。

    Args:
        keyword: 搜索关键词，如商品名或品牌词
        minPrice: 最低价格(元)，无要求则不传
        maxPrice: 最高价格(元)，无要求则不传
    """
    return await search_goods_impl(keyword, minPrice, maxPrice)


@tool("searchGoodsSemantic", description="语义检索商品知识库。用户需求模糊、口语化、场景化（如'送妈妈的礼物'、'露营装备'、'学生党性价比'）时优先使用")
async def search_goods_semantic(query, maxPrice=None, minPrice=None):
    """语义检索商品知识库。

    Args:
        query: 用户需求或场景描述
        maxPrice: 最高价格(元)，无要求则不传
        minPrice: 最低价格(元)，无要求则不传
    """
    return await search_goods_semantic_impl(query, maxPrice, minPrice)


@tool("getGoodsDetail", description="获取指定商品的详细信息（名称、品牌、价格、库存、销量、简介）。goodsId 必须来自其他工具的返回结果，禁止猜测")
async def get_goods_detail(goodsId):
    """获取指定商品的详细信息。

    Args:
        goodsId: 商品ID
    """
    try:
        resp = await goods_client.get_goods(goodsId)
        data = (resp or {}).get("data")
        if data is None:
            return _dumps({"error": "未找到该商品。请先用 searchGoods 以商品名作为关键词搜索，拿到正确的 goodsId 后再查询"})
        if isinstance(data, dict):
            goods = data.get("goods") if isinstance(data.get("goods"), dict) else data
            goods.pop("imageUrl", None)
        return _dumps(data)
    except Exception as e:
        return _err("查询商品详情失败", e)


@tool("getCategories", description="获取商城全部商品分类树（一级分类及子分类）。用户问有哪些品类、类别时使用")
async def get_categories():
    """获取商城全部商品分类树。"""
    try:
        resp = await goods_client.get_categories()
        data = (resp or {}).get("data")
        if not isinstance(data, list):
            return _dumps({"error": "分类数据获取失败"})
        out = []
        for g in data:
            children = "、".join(str(c.get("categoryName")) for c in (g.get("children") or []))
            out.append({
                "categoryId": g.get("categoryId"),
                "categoryName": str(g.get("categoryName")),
                "children": children,
            })
        return _dumps(out)
    except Exception as e:
        return _err("分类获取失败", e)


@tool("getHotGoods", description="获取商城热销商品排行（按销量前10）。用户问热销、爆款、人气商品时使用")
async def get_hot_goods():
    """获取商城热销商品排行。"""
    try:
        resp = await goods_client.list_goods(None, "sales", 10)
        out = [await _compact(r) for r in (resp.get("rows") or [])]
        return _dumps({"count": len(out), "goods": out})
    except Exception as e:
        return _err("热销排行获取失败", e)


TOOLS = [search_goods, search_goods_semantic, get_goods_detail, get_categories, get_hot_goods]

TOOL_MAP = {t.name: t for t in TOOLS}
