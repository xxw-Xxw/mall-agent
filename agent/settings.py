import os
from pathlib import Path

from pydantic import model_validator
from pydantic_settings import BaseSettings, SettingsConfigDict

BASE_DIR = Path(__file__).resolve().parent

# 运行环境由系统环境变量决定：dev（默认）/ prod
# dev 读取 agent/.env.dev，prod 读取 agent/.env.prod
APP_ENV = os.environ.get("APP_ENV", "dev").strip().lower()
if APP_ENV not in ("dev", "prod"):
    raise ValueError(f"APP_ENV 仅支持 dev / prod，当前为：{APP_ENV}")


class Settings(BaseSettings):
    zhipu_api_key: str = ""
    zhipu_base_url: str = "https://open.bigmodel.cn/api/paas"
    llm_model: str = "glm-4-flash"
    llm_temperature: float = 0.3
    embedding_model: str = "embedding-3"
    ruoyi_base_url: str = "http://localhost:8080"
    chroma_base_url: str = "http://localhost:8000"
    collection_name: str = "mall_goods"
    # 允许跨域的前端地址，多个用英文逗号分隔；生产环境只填实际站点域名
    cors_origins: str = "http://localhost:3000"
    # 管理接口 JWT 签名密钥（HS256）：后端签发、Agent 验签，两端必须一致
    # 未配置时管理接口一律拒绝访问
    agent_jwt_secret: str = ""
    # Redis：存放按用户签发的 JWT 会话（key: agent:session:{jti}）
    redis_host: str = "localhost"
    redis_port: int = 6379
    redis_password: str = ""
    redis_database: int = 0
    log_level: str = "INFO"
    # 会话状态持久化：一期为本地 SQLite 文件，后续可换 postgres / redis 实现横向扩展
    checkpoint_backend: str = "sqlite"
    checkpoint_db_path: str = str(BASE_DIR / "data" / "sessions.db")
    # 中期记忆压缩：未摘要内容超过该字符数时生成摘要，摘要后对话只保留最近 keep 条原文
    summary_trigger_chars: int = 8000
    summary_keep: int = 8
    summary_batch: int = 20
    # 定时对账：每天 reconcile_hour 点核对一次商品与向量库
    reconcile_enabled: bool = True
    reconcile_hour: int = 3
    # 混合检索：向量 + BM25 召回后走 rerank 重排
    rerank_enabled: bool = True
    rerank_model: str = "rerank"

    # 按环境加载对应文件，并固定按 agent 目录定位，避免依赖启动时的工作目录
    model_config = SettingsConfigDict(
        env_file=BASE_DIR / f".env.{APP_ENV}",
        env_file_encoding="utf-8",
        extra="ignore",
    )

    @property
    def cors_origin_list(self) -> list[str]:
        return [item.strip() for item in self.cors_origins.split(",") if item.strip()]

    @model_validator(mode="after")
    def _check_required_secrets(self):
        if APP_ENV == "prod" and not self.zhipu_api_key.strip():
            raise ValueError("生产环境必须配置 ZHIPU_API_KEY（环境变量或 agent/.env.prod）")
        if APP_ENV == "prod" and not self.agent_jwt_secret.strip():
            raise ValueError("生产环境必须配置 AGENT_JWT_SECRET（环境变量或 agent/.env.prod）")
        return self


settings = Settings()
