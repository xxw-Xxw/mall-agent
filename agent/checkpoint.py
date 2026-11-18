"""会话状态持久化。

一期使用本地 SQLite 文件（agent/data/sessions.db），重启和多 worker 共享同一份上下文。
后续要跨机器横向扩展时，只需在此工厂里增加 postgres / redis 分支，业务代码不用改。
"""

import logging
from contextlib import asynccontextmanager
from pathlib import Path

import aiosqlite
from langgraph.checkpoint.sqlite.aio import AsyncSqliteSaver

from settings import BASE_DIR, settings

log = logging.getLogger("mall-agent")


@asynccontextmanager
async def create_checkpointer():
    backend = settings.checkpoint_backend.strip().lower()
    if backend != "sqlite":
        raise ValueError(f"暂不支持的 checkpoint_backend：{backend}（当前仅支持 sqlite）")

    path = Path(settings.checkpoint_db_path)
    if not path.is_absolute():
        # 相对路径按 agent 目录解析，避免依赖启动时的工作目录
        path = BASE_DIR / path
    path.parent.mkdir(parents=True, exist_ok=True)

    conn = await aiosqlite.connect(str(path))
    try:
        # WAL 允许读写并发，busy_timeout 避免多 worker 写冲突时立刻抛 database is locked
        await conn.execute("PRAGMA journal_mode=WAL")
        await conn.execute("PRAGMA busy_timeout=5000")
        saver = AsyncSqliteSaver(conn)
        await saver.setup()
        log.info("会话持久化已启用: %s", path)
        yield saver
    finally:
        await conn.close()
