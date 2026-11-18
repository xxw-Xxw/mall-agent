"""共享 Redis 异步客户端（管理端 JWT 会话、商城会员会话）。"""

import redis.asyncio as aioredis

from settings import settings

_redis: aioredis.Redis | None = None


def get_redis() -> aioredis.Redis:
    global _redis
    if _redis is None:
        _redis = aioredis.Redis(
            host=settings.redis_host,
            port=settings.redis_port,
            password=settings.redis_password or None,
            db=settings.redis_database,
            decode_responses=True,
            socket_connect_timeout=3,
            socket_timeout=3,
            # 项目部署的 Redis 版本较旧，强制 RESP2，避免 redis-py 默认的 HELLO 握手失败
            protocol=2,
        )
    return _redis
