@echo off
chcp 65001 >nul
title 橙心优选商城 - 一键启动
set ROOT=%~dp0
set ROOT=%ROOT:~0,-1%
set PYTHONDONTWRITEBYTECODE=1

REM 读取开发环境变量文件（不入库，参考 .env.dev.example）
if exist "%ROOT%\.env.dev" (
    for /f "usebackq eol=# tokens=1,* delims==" %%a in ("%ROOT%\.env.dev") do (
        set "%%a=%%b"
    )
)
if not defined SPRING_PROFILES_ACTIVE set "SPRING_PROFILES_ACTIVE=dev"
if not defined APP_ENV set "APP_ENV=dev"

REM 端口与代理地址：优先使用 .env / 系统环境变量，未配置时用默认值
if not defined SERVER_PORT set "SERVER_PORT=8080"
if not defined AGENT_PORT set "AGENT_PORT=8090"
if not defined WEB_PORT set "WEB_PORT=3000"
if not defined CHROMA_PORT set "CHROMA_PORT=8000"
if not defined VITE_DEV_PORT set "VITE_DEV_PORT=%WEB_PORT%"
if not defined VITE_API_TARGET set "VITE_API_TARGET=http://localhost:%SERVER_PORT%"
if not defined VITE_AGENT_TARGET set "VITE_AGENT_TARGET=http://localhost:%AGENT_PORT%"

REM Java：优先使用 JAVA_HOME，未配置时回退到 PATH 中的 java
set "JAVA_EXE=java"
if defined JAVA_HOME set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"

echo ============================================
echo   橙心优选商城 启动中...
echo   启动顺序: Redis -^> Chroma -^> RuoYi -^> Agent -^> Web
echo   端口: Web %WEB_PORT% / RuoYi %SERVER_PORT% / Agent %AGENT_PORT% / Chroma %CHROMA_PORT%
echo ============================================

REM 1. Redis (:6379) —— cwd 必须在 redis 目录
echo [1/5] 启动 Redis...
pushd "%ROOT%\redis"
start "Redis" /min redis-server.exe redis.windows.conf
popd
timeout /t 2 /nobreak >nul

REM 2. ChromaDB (:8000)
echo [2/5] 启动 ChromaDB...
start "Chroma" /min "%ROOT%\venv\Scripts\python.exe" "%ROOT%\start-chroma.py"
timeout /t 3 /nobreak >nul

REM 3. RuoYi 后端 (:8080) —— 需要 MySQL 服务已启动
echo [3/5] 启动 RuoYi 后端 (需要 MySQL 服务已启动)...
start "RuoYi" /min "%JAVA_EXE%" -jar "%ROOT%\backend\ruoyi-admin.jar"

REM 4. mall-agent AI 导购 (:8090) —— 配置读取 agent\.env，与工作目录无关
echo [4/5] 启动 mall-agent AI 导购...
start "mall-agent" /min "%ROOT%\venv\Scripts\python.exe" -m uvicorn main:app --app-dir "%ROOT%\agent" --host 0.0.0.0 --port %AGENT_PORT%

REM 5. mall-web 前端 (:3000)
echo [5/5] 启动前端页面...
pushd "%ROOT%\web"
start "mall-web" /min cmd /c "npm run dev"
popd

echo.
echo ============================================
echo   全部服务已拉起，约 30-60 秒后访问:
echo   http://localhost:%WEB_PORT%
echo ============================================
pause
