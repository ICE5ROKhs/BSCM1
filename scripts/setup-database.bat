@echo off
REM BSCM 数据库配置自动化脚本（批处理版本）
REM 调用 PowerShell 脚本

echo ========================================
echo   BSCM 数据库配置自动化脚本
echo ========================================
echo.

REM 检查 PowerShell 是否可用
powershell -Command "Get-Host" >nul 2>&1
if errorlevel 1 (
    echo 错误: 无法执行 PowerShell 脚本
    echo 请确保 PowerShell 已安装
    pause
    exit /b 1
)

REM 执行 PowerShell 脚本
powershell -ExecutionPolicy Bypass -File "%~dp0setup-database.ps1" %*

if errorlevel 1 (
    echo.
    echo 脚本执行失败，请检查错误信息
    pause
    exit /b 1
)

pause



