import httpx

from settings import settings

_client = httpx.AsyncClient(base_url=settings.ruoyi_base_url, timeout=30.0)


async def list_goods(keyword=None, sort_field=None, page_size=200):
    params = {"pageNum": 1, "pageSize": page_size}
    if keyword and keyword.strip():
        params["keyword"] = keyword
    if sort_field and sort_field.strip():
        params["sortField"] = sort_field
    resp = await _client.get("/store/goods/list", params=params)
    resp.raise_for_status()
    return resp.json()


async def get_goods(goods_id):
    resp = await _client.get(f"/store/goods/{goods_id}")
    resp.raise_for_status()
    return resp.json()


async def get_categories():
    resp = await _client.get("/store/categories")
    resp.raise_for_status()
    return resp.json()
