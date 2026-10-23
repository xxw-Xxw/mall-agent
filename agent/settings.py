from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    zhipu_api_key: str = ""
    zhipu_base_url: str = "https://open.bigmodel.cn/api/paas"
    llm_model: str = "glm-4-flash"
    llm_temperature: float = 0.6
    embedding_model: str = "embedding-3"
    ruoyi_base_url: str = "http://localhost:8080"
    chroma_base_url: str = "http://localhost:8000"
    collection_name: str = "mall_goods"

    model_config = SettingsConfigDict(env_file=".env", env_file_encoding="utf-8")


settings = Settings()
