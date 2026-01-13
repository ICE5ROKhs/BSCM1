# Android 构建脚本
# 用于快速构建 Android APK

Write-Host "开始构建 Android 应用..." -ForegroundColor Green

# 检查是否在正确的目录
if (-not (Test-Path "frontend")) {
    Write-Host "错误: 请在项目根目录运行此脚本" -ForegroundColor Red
    exit 1
}

# 进入 frontend 目录
Set-Location frontend

# 检查依赖是否安装
if (-not (Test-Path "node_modules")) {
    Write-Host "安装依赖..." -ForegroundColor Yellow
    npm install
}

# 检查 API 配置
Write-Host "`n提醒: 请确保已配置后端 API 地址" -ForegroundColor Yellow
Write-Host "编辑 frontend/src/config/api.js 或设置环境变量 VITE_API_BASE_URL`n" -ForegroundColor Yellow

# 构建前端
Write-Host "构建前端..." -ForegroundColor Green
npm run build

if ($LASTEXITCODE -ne 0) {
    Write-Host "前端构建失败!" -ForegroundColor Red
    Set-Location ..
    exit 1
}

# 同步到 Android
Write-Host "同步到 Android 项目..." -ForegroundColor Green
npx cap sync

if ($LASTEXITCODE -ne 0) {
    Write-Host "同步失败!" -ForegroundColor Red
    Set-Location ..
    exit 1
}

Write-Host "`n构建完成!" -ForegroundColor Green
Write-Host "下一步:" -ForegroundColor Yellow
Write-Host "1. 运行: cd frontend; npm run cap:open:android" -ForegroundColor Cyan
Write-Host "2. 在 Android Studio 中构建 APK/AAB" -ForegroundColor Cyan
Write-Host "`n或使用命令行构建:" -ForegroundColor Yellow
Write-Host "   cd frontend/android" -ForegroundColor Cyan
Write-Host "   .\gradlew assembleRelease" -ForegroundColor Cyan

Set-Location ..

