import math
import re
import time
from collections import Counter
from urllib.parse import urlparse

import chromadb
import httpx

from defenses import to_float
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


# ---------------- BM25 关键词召回 ----------------

_TOKEN_RE = re.compile(r"[\u4e00-\u9fff]+|[0-9a-zA-Z]+")
_BM25_TTL = 300


def _tokenize(text):
    """中文按单字 + 二元组切分，英文/数字按整词，不依赖分词库。"""
    out = []
    for seg in _TOKEN_RE.findall((text or "").lower()):
        if "\u4e00" <= seg[0] <= "\u9fff":
            out.extend(seg)
            out.extend(seg[i:i + 2] for i in range(len(seg) - 1))
        else:
            out.append(seg)
    return out


_bm25_cache = None


def invalidate_bm25():
    """商品增删改后调用，让 BM25 索引下次检索时重建。"""
    global _bm25_cache
    _bm25_cache = None


def _bm25_index():
    global _bm25_cache
    total = collection.count()
    if _bm25_cache and _bm25_cache["count"] == total and time.time() - _bm25_cache["ts"] < _BM25_TTL:
        return _bm25_cache
    data = collection.get(include=["documents", "metadatas"])
    docs = data.get("documents") or []
    metas = data.get("metadatas") or []
    toks = [_tokenize(d) for d in docs]
    df = Counter()
    for t in toks:
        df.update(set(t))
    _bm25_cache = {
        "ts": time.time(),
        "count": total,
        "docs": docs,
        "metas": metas,
        "toks": toks,
        "df": df,
        "n": max(len(toks), 1),
        "avgdl": (sum(len(t) for t in toks) / len(toks)) if toks else 0.0,
    }
    return _bm25_cache


def bm25_search(query, min_price=None, max_price=None, top_k=20):
    idx = _bm25_index()
    if not idx["docs"]:
        return []
    terms = _tokenize(query)
    if not terms:
        return []
    k1, b = 1.5, 0.75
    n, avgdl = idx["n"], idx["avgdl"] or 1.0
    scored = []
    for i, tokens in enumerate(idx["toks"]):
        md = idx["metas"][i] or {}
        price = to_float(md.get("price"))
        if min_price is not None and price < min_price:
            continue
        if max_price is not None and price > max_price:
            continue
        dl = len(tokens) or 1
        tf = Counter(tokens)
        score = 0.0
        for t in terms:
            f = tf.get(t)
            if not f:
                continue
            idf = math.log(1 + (n - idx["df"].get(t, 0) + 0.5) / (idx["df"].get(t, 0) + 0.5))
            score += idf * f * (k1 + 1) / (f + k1 * (1 - b + b * dl / avgdl))
        if score > 0:
            scored.append((score, i))
    scored.sort(key=lambda x: -x[0])
    return [(idx["docs"][i], idx["metas"][i]) for _, i in scored[:top_k]]


# ---------------- 融合 + 重排 ----------------

def _rrf(hit_lists, k=60):
    """Reciprocal Rank Fusion：按名次倒数相加，抵消两路分数量纲差异。"""
    scores, items = {}, {}
    for hits in hit_lists:
        for rank, (doc, md) in enumerate(hits, start=1):
            key = (md or {}).get("goodsId") or doc
            items.setdefault(key, (doc, md))
            scores[key] = scores.get(key, 0.0) + 1.0 / (k + rank)
    return [items[key] for key in sorted(scores, key=lambda x: -scores[x])]


async def rerank_texts(query, docs, top_n=10):
    """调用智谱 rerank 模型重排，返回原始下标列表；失败返回 None 由上层降级。"""
    if not docs:
        return []
    try:
        resp = await _http.post(
            "/v4/rerank",
            headers={"Authorization": f"Bearer {settings.zhipu_api_key}"},
            json={"model": settings.rerank_model, "query": query,
                  "documents": docs, "top_n": top_n},
        )
        resp.raise_for_status()
        results = resp.json().get("results") or []
        ordered = sorted(results, key=lambda r: -float(r.get("relevance_score") or 0))
        return [int(r["index"]) for r in ordered]
    except Exception:
        return None


async def hybrid_search(query, min_price=None, max_price=None, top_k=10, candidate_k=20):
    """向量召回 + BM25 召回 → RRF 融合 → rerank 重排 → 返回 (文档, metadata)。"""
    vec_hits = await raw_semantic_query(query, min_price, max_price, top_k=candidate_k)
    kw_hits = bm25_search(query, min_price, max_price, top_k=candidate_k)
    fused = _rrf([vec_hits, kw_hits])
    if not fused:
        return []
    pool = fused[:candidate_k]
    if not settings.rerank_enabled:
        return pool[:top_k]
    ranked = await rerank_texts(query, [doc or "" for doc, _ in pool], top_n=top_k)
    if ranked is None:
        return pool[:top_k]
    picked = [pool[i] for i in ranked if 0 <= i < len(pool)]
    return picked or pool[:top_k]

