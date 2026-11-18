# 橙心优选商城（mall-agent）

RuoYi 后端 + Vue3 前端 + LangGraph AI 导购 Agent（RAG / ChromaDB / 智谱 GLM）

- `agent/`：AI 导购服务（LangGraph 状态图、Function Calling、五层防幻觉、SSE 流式）
- `web/`：Vue3 + Vite 商城前端
- `RuoYi/`：Spring Boot 后端；`RuoYi-Vue3/`：管理后台

## 配置说明

密钥与可变配置全部走环境变量，仓库只保留 `*.example` 模板，不放真实密钥。

### 开发环境（默认）

| 配置 | 位置 |
| --- | --- |
| 后端 | 根目录 `.env.dev`（由 `.env.dev.example` 复制）+ `application-dev.yml` |
| Agent | `agent/.env.dev`（由 `agent/.env.dev.example` 复制） |
| 商城前端 | `web/.env.development` |
| 管理后台 | `RuoYi-Vue3/.env.development` |

`start-all.bat` 会自动加载根目录 `.env.dev`，并设置 `SPRING_PROFILES_ACTIVE=dev`、`APP_ENV=dev`。

### 生产环境

| 配置 | 位置 |
| --- | --- |
| 后端 | 环境变量注入 + `application-prod.yml`，启动时设置 `SPRING_PROFILES_ACTIVE=prod` |
| Agent | `agent/.env.prod` 或环境变量注入，启动时设置 `APP_ENV=prod` |
| 商城前端 | `web/.env.production`，`npm run build` 自动加载 |
| 管理后台 | `RuoYi-Vue3/.env.production` |

后端生产启动：

```bash
# 必填：DB_URL DB_USERNAME DB_PASSWORD REDIS_HOST REDIS_PORT REDIS_PASSWORD
#       TOKEN_SECRET RUOYI_PROFILE AGENT_BASE_URL
SPRING_PROFILES_ACTIVE=prod java -jar backend/ruoyi-admin.jar
```

- 生产 profile 未提供默认值，上述变量缺失会直接启动失败。
- 生产环境关闭 Swagger、Druid 监控台和热部署。
- Agent 在 `APP_ENV=prod` 下缺少 `ZHIPU_API_KEY` 或 `AGENT_JWT_SECRET` 会拒绝启动，`CORS_ORIGINS` 只填实际站点域名。
- Agent 管理接口（`/agent/reindex`、`/agent/goods/*`、`/agent/reconcile`、`/agent/session/*`、`/agent/debug/*`）采用 **JWT + Redis 会话**鉴权：后端按用户/调用方用 `AGENT_JWT_SECRET` 签发短期 JWT（HS256，默认 10 分钟），会话写入 Redis（`agent:session:{jti}`）；Agent 验签后到 Redis 校验会话，删除 key 即可主动撤销。调用需携带 `Authorization: Bearer <jwt>`。`/agent/chat`、`/agent/health` 保持公开。
- 管理员获取管理 JWT：`POST /mall/agent/token`（需 admin 角色登录）；后端商品同步由 `AgentIndexClient` 自动签发系统 JWT。
- 修改后端配置后需重新构建 `backend/ruoyi-admin.jar`。
