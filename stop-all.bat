@echo off
chcp 65001 >nul
title 橙心优选商城 - 停止全部服务
echo 正在停止商城全部服务...
for %%p in (3000 6379 8000 8080 8090) do (
  for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":%%p " ^| findstr LISTENING') do (
    taskkill /PID %%a /F >nul 2>&1
  )
)
echo 完成。所有服务已停止。
pause
