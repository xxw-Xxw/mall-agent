@echo off
chcp 65001 >nul
title 橙心优选商城 - 一键启动
set ROOT=%~dp0
set ROOT=%ROOT:~0,-1%
set JAVA_HOME=D:\JDK\java17
set PYTHONDONTWRITEBYTECODE=1

echo ============================================
echo   橙心优选商城 启动中...
echo   启动顺序: Redis -^> Chroma -^> RuoYi -^> Agent -^> Web
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
start "RuoYi" /min "%JAVA_HOME%\bin\java.exe" -jar "%ROOT%\backend\ruoyi-admin.jar"

REM 4. mall-agent AI 导购 (:8090) —— cwd 必须是 agent 目录以读取 .env
echo [4/5] 启动 mall-agent AI 导购...
pushd "%ROOT%\agent"
start "mall-agent" /min "%ROOT%\venv\Scripts\python.exe" -m uvicorn main:app --host 0.0.0.0 --port 8090
popd

REM 5. mall-web 前端 (:3000)
echo [5/5] 启动前端页面...
pushd "%ROOT%\web"
start "mall-web" /min cmd /c "npm run dev"
popd

echo.
echo ============================================
echo   全部服务已拉起，约 30-60 秒后访问:
echo   http://localhost:3000
echo ============================================
pause
