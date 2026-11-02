from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    zhipu_api_key: str = ""
    zhipu_base_url: str = "https://open.bigmodel.cn/api/paas"
    llm_model: str = "glm-4-flash"
    llm_temperature: float = 0.3
    embedding_model: str = "embedding-3"
    ruoyi_base_url: str = "http://localhost:8080"
    chroma_base_url: str = "http://localhost:8000"
    collection_name: str = "mall_goods"
    # 定时对账：每天 reconcile_hour 点核对一次商品与向量库
    reconcile_enabled: bool = True
    reconcile_hour: int = 3
    # 混合检索：向量 + BM25 召回后走 rerank 重排
    rerank_enabled: bool = True
    rerank_model: str = "rerank"

    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")


settings = Settings()
