from urllib.parse import urlparse

import chromadb
import httpx

from settings import settings

_parsed = urlparse(settings.chroma_base_url)
_chroma = chromadb.HttpClient(host=_parsed.hostname or "localhost", port=_parsed.port or 8000)
collection = _chroma.get_or_create_collection(name=settings.collection_name, metadata={"hnsw:space": "cosine"})

_http = httpx.AsyncClient(base_url=settings.zhipu_base_url, timeout=60.0)


async def embed_texts(texts):
    out = []
    for i in range(0, len(texts), 50):
        batch = texts[i:i + 50]
        resp = await _http.post(
            "/v4/embeddings",
            headers={"Authorization": f"Bearer {settings.zhipu_api_key}"},
            json={"model": settings.embedding_model, "input": batch},
        )
        resp.raise_for_status()
        out.extend(d["embedding"] for d in resp.json()["data"])
    return out


def _where(min_price, max_price):
    conds = []
    if min_price is not None:
        conds.append({"price": {"$gte": float(min_price)}})
    if max_price is not None:
        conds.append({"price": {"$lte": float(max_price)}})
    if not conds:
        return None
    return conds[0] if len(conds) == 1 else {"$and": conds}


async def raw_semantic_query(query, min_price=None, max_price=None, top_k=10):
    emb = (await embed_texts([query]))[0]
    res = collection.query(
        query_embeddings=[emb],
        n_results=top_k,
        where=_where(min_price, max_price),
        include=["documents", "metadatas"],
    )
    docs = (res.get("documents") or [[]])[0]
    metas = (res.get("metadatas") or [[]])[0]
    return list(zip(docs, metas))
