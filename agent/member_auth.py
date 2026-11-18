"""聊天身份解析与会话标识。

已登录会员：前端带 Authorization: Bearer <商城会员token>，
Agent 从 Redis 的 store_tokens:{token} 取 MemberSession 得到 memberId，
会话标识固定为 member:{memberId}，换设备/刷新后仍是同一段上下文。

匿名访客：前端用 localStorage 保存的 anonId，会话标识为 anon:{anonId}。
前端未带或格式非法时，服务端生成一次性 ID 并通过响应头回传。
"""

import json
import logging
import re
import uuid

from fastapi import HTTPException

from redis_client import get_redis

log = logging.getLogger("mall-agent.auth")

MEMBER_TOKEN_PREFIX = "store_tokens:"
ANON_ID_RE = re.compile(r"^[A-Za-z0-9_-]{8,64}$")


async def resolve_member(authorization: str | None) -> dict | None:
    """解析会员登录态；无 token 返回 None（匿名），token 失效抛 401。"""
    if not authorization or not authorization.startswith("Bearer "):
        return None
    token = authorization[len("Bearer "):].strip()
    if not token:
        return None

    try:
        raw = await get_redis().get(MEMBER_TOKEN_PREFIX + token)
    except Exception as e:
        log.warning("会员会话查询失败: %s", e)
        raise HTTPException(status_code=503, detail="会员会话服务不可用")
    if not raw:
        raise HTTPException(status_code=401, detail="登录已过期，请重新登录")

    try:
        data = json.loads(raw)
    except Exception:
        raise HTTPException(status_code=401, detail="登录状态无效")
    member_id = data.get("memberId")
    if member_id is None:
        raise HTTPException(status_code=401, detail="登录状态无效")

    return {
        "memberId": int(member_id),
        "username": data.get("username") or "",
        "nickname": data.get("nickname") or "",
    }


def resolve_thread(member: dict | None, anon_id: str | None) -> tuple[str, str | None]:
    """返回 (thread_id, 需要回传给前端保存的匿名ID)。"""
    if member:
        return f"member:{member['memberId']}", None
    anon = (anon_id or "").strip()
    if ANON_ID_RE.match(anon):
        return f"anon:{anon}", None
    generated = uuid.uuid4().hex[:16]
    return f"anon:{generated}", generated
