@echo off
chcp 65001 >nul
title 橙心优选商城 - 停止全部服务
set ROOT=%~dp0
set ROOT=%ROOT:~0,-1%

REM 读取开发环境变量文件（用于获取自定义端口）
if exist "%ROOT%\.env.dev" (
    for /f "usebackq eol=# tokens=1,* delims==" %%a in ("%ROOT%\.env.dev") do (
        set "%%a=%%b"
    )
)
if not defined WEB_PORT set "WEB_PORT=3000"
if not defined REDIS_PORT set "REDIS_PORT=6379"
if not defined CHROMA_PORT set "CHROMA_PORT=8000"
if not defined SERVER_PORT set "SERVER_PORT=8080"
if not defined AGENT_PORT set "AGENT_PORT=8090"

echo 正在停止商城全部服务...
for %%p in (%WEB_PORT% %REDIS_PORT% %CHROMA_PORT% %SERVER_PORT% %AGENT_PORT%) do (
  for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%%p " ^| findstr LISTENING') do (
    taskkill /PID %%a /F >nul 2>&1
  )
)
echo 完成。所有服务已停止。
pause
