"""管理接口鉴权：JWT 验签 + Redis 会话校验。

后端按用户/调用方签发 JWT（HS256），并把会话写入 Redis：
    key = agent:session:{jti}，value = {"sub":..., "username":...}，TTL 与 JWT 过期一致。
Agent 收到请求后：
    1. 验签 + 校验过期时间；
    2. 取 jti 到 Redis 查会话，不存在/已过期即拒绝（删除 key 即可主动撤销）。
"""
import json
import logging

import jwt
from fastapi import Header, HTTPException

from redis_client import get_redis
from settings import settings

log = logging.getLogger("mall-agent.auth")

SESSION_PREFIX = "agent:session:"


async def require_admin(authorization: str | None = Header(default=None)) -> dict:
    """FastAPI 依赖：校验 Authorization: Bearer <jwt> 并到 Redis 确认会话有效。"""
    if not settings.agent_jwt_secret:
        raise HTTPException(status_code=503, detail="管理接口未配置 AGENT_JWT_SECRET")
    if not authorization or not authorization.startswith("Bearer "):
        raise HTTPException(status_code=401, detail="未授权")

    token = authorization[len("Bearer "):].strip()
    try:
        payload = jwt.decode(token, settings.agent_jwt_secret, algorithms=["HS256"])
    except jwt.ExpiredSignatureError:
        raise HTTPException(status_code=401, detail="令牌已过期")
    except jwt.InvalidTokenError:
        raise HTTPException(status_code=401, detail="令牌无效")

    session_id = payload.get("jti")
    if not session_id:
        raise HTTPException(status_code=401, detail="令牌缺少会话标识")

    try:
        session = await get_redis().get(SESSION_PREFIX + session_id)
    except Exception as e:
        log.warning("Redis 会话查询失败: %s", e)
        raise HTTPException(status_code=503, detail="会话服务不可用")
    if not session:
        raise HTTPException(status_code=401, detail="会话不存在或已过期")

    return json.loads(session)
