# BSCM 数据库配置自动化脚本
# 功能：自动创建 PostgreSQL 数据库和用户

param(
    [string]$PostgresPassword = "",
    [string]$BscmPassword = "bscm123456",
    [string]$DbHost = "127.0.0.1",
    [int]$Port = 5432,
    [string]$DatabaseName = "bscm_db",
    [string]$UserName = "bscm"
)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  BSCM 数据库配置自动化脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 检查 PostgreSQL 是否安装
Write-Host "[1/6] 检查 PostgreSQL 安装..." -ForegroundColor Yellow
$psqlPath = Get-Command psql -ErrorAction SilentlyContinue
if (-not $psqlPath) {
    Write-Host "错误: 未找到 psql 命令，请确保 PostgreSQL 已安装并添加到 PATH 环境变量" -ForegroundColor Red
    Write-Host "提示: 通常 PostgreSQL 的 bin 目录位于: C:\Program Files\PostgreSQL\<version>\bin" -ForegroundColor Yellow
    exit 1
}
Write-Host "✓ PostgreSQL 已安装" -ForegroundColor Green
Write-Host ""

# 检查 PostgreSQL 服务是否运行
Write-Host "[2/6] 检查 PostgreSQL 服务状态..." -ForegroundColor Yellow
$pgService = Get-Service -Name "postgresql*" -ErrorAction SilentlyContinue | Where-Object { $_.Status -eq 'Running' }
if (-not $pgService) {
    Write-Host "警告: 未检测到运行中的 PostgreSQL 服务" -ForegroundColor Yellow
    Write-Host "尝试启动 PostgreSQL 服务..." -ForegroundColor Yellow
    try {
        $pgService = Get-Service -Name "postgresql*" -ErrorAction SilentlyContinue | Select-Object -First 1
        if ($pgService) {
            Start-Service -Name $pgService.Name
            Start-Sleep -Seconds 3
            Write-Host "✓ PostgreSQL 服务已启动" -ForegroundColor Green
        } else {
            Write-Host "错误: 无法找到 PostgreSQL 服务，请手动启动 PostgreSQL 服务" -ForegroundColor Red
            exit 1
        }
    } catch {
        Write-Host "错误: 无法启动 PostgreSQL 服务: $_" -ForegroundColor Red
        Write-Host "请手动启动 PostgreSQL 服务后重试" -ForegroundColor Yellow
        exit 1
    }
} else {
    Write-Host "✓ PostgreSQL 服务正在运行" -ForegroundColor Green
}
Write-Host ""

# 获取 postgres 用户密码
if ([string]::IsNullOrEmpty($PostgresPassword)) {
    Write-Host "[3/6] 请输入 postgres 超级用户密码:" -ForegroundColor Yellow
    $securePassword = Read-Host -AsSecureString
    $BSTR = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword)
    $PostgresPassword = [System.Runtime.InteropServices.Marshal]::PtrToStringAuto($BSTR)
} else {
    Write-Host "[3/6] 使用提供的 postgres 密码" -ForegroundColor Yellow
}
Write-Host ""

# 设置 PostgreSQL 连接环境变量
$env:PGPASSWORD = $PostgresPassword

# 测试连接
Write-Host "[4/6] 测试 PostgreSQL 连接..." -ForegroundColor Yellow
$testConnection = & psql -h $DbHost -p $Port -U postgres -d postgres -c "SELECT version();" 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "错误: 无法连接到 PostgreSQL 服务器" -ForegroundColor Red
    Write-Host "请检查:" -ForegroundColor Yellow
    Write-Host "  1. PostgreSQL 服务是否正在运行" -ForegroundColor Yellow
    Write-Host "  2. postgres 用户密码是否正确" -ForegroundColor Yellow
    Write-Host "  3. 连接参数是否正确 (Host: $DbHost, Port: $Port)" -ForegroundColor Yellow
    $env:PGPASSWORD = ""
    exit 1
}
Write-Host "✓ 连接成功" -ForegroundColor Green
Write-Host ""

# 创建用户（如果不存在）
Write-Host "[5/6] 创建数据库用户 '$UserName'..." -ForegroundColor Yellow
$userExists = & psql -h $DbHost -p $Port -U postgres -d postgres -t -c "SELECT 1 FROM pg_roles WHERE rolname='$UserName';" 2>&1
if ($userExists -match "1") {
    Write-Host "  用户 '$UserName' 已存在，跳过创建" -ForegroundColor Gray
    # 更新密码（如果需要）
    & psql -h $DbHost -p $Port -U postgres -d postgres -c "ALTER ROLE $UserName WITH PASSWORD '$BscmPassword';" 2>&1 | Out-Null
    Write-Host "  ✓ 已更新用户密码" -ForegroundColor Green
} else {
    $createUser = & psql -h $DbHost -p $Port -U postgres -d postgres -c "CREATE ROLE $UserName LOGIN PASSWORD '$BscmPassword';" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✓ 用户 '$UserName' 创建成功" -ForegroundColor Green
    } else {
        Write-Host "  错误: 创建用户失败" -ForegroundColor Red
        Write-Host "  $createUser" -ForegroundColor Red
        $env:PGPASSWORD = ""
        exit 1
    }
}
Write-Host ""

# 创建数据库（如果不存在）
Write-Host "[6/6] 创建数据库 '$DatabaseName'..." -ForegroundColor Yellow
$dbExists = & psql -h $DbHost -p $Port -U postgres -d postgres -t -c "SELECT 1 FROM pg_database WHERE datname='$DatabaseName';" 2>&1
if ($dbExists -match "1") {
    Write-Host "  数据库 '$DatabaseName' 已存在，跳过创建" -ForegroundColor Gray
} else {
    $createDb = & psql -h $DbHost -p $Port -U postgres -d postgres -c "CREATE DATABASE $DatabaseName OWNER $UserName;" 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  ✓ 数据库 '$DatabaseName' 创建成功" -ForegroundColor Green
    } else {
        Write-Host "  错误: 创建数据库失败" -ForegroundColor Red
        Write-Host "  $createDb" -ForegroundColor Red
        $env:PGPASSWORD = ""
        exit 1
    }
}

# 授予权限
Write-Host "  授予权限..." -ForegroundColor Yellow
& psql -h $DbHost -p $Port -U postgres -d $DatabaseName -c "GRANT ALL PRIVILEGES ON DATABASE $DatabaseName TO $UserName;" 2>&1 | Out-Null
& psql -h $DbHost -p $Port -U postgres -d $DatabaseName -c "GRANT ALL ON SCHEMA public TO $UserName;" 2>&1 | Out-Null
Write-Host "  ✓ 权限授予成功" -ForegroundColor Green
Write-Host ""

# 清理环境变量
$env:PGPASSWORD = ""

# 验证配置
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  配置验证" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "数据库配置信息:" -ForegroundColor Yellow
Write-Host "  主机: $DbHost" -ForegroundColor White
Write-Host "  端口: $Port" -ForegroundColor White
Write-Host "  数据库名: $DatabaseName" -ForegroundColor White
Write-Host "  用户名: $UserName" -ForegroundColor White
Write-Host "  密码: $BscmPassword" -ForegroundColor White
Write-Host ""

# 测试新用户连接
Write-Host "测试新用户连接..." -ForegroundColor Yellow
$env:PGPASSWORD = $BscmPassword
$testUserConnection = & psql -h $DbHost -p $Port -U $UserName -d $DatabaseName -c "SELECT current_database(), current_user;" 2>&1
$env:PGPASSWORD = ""

if ($LASTEXITCODE -eq 0) {
    Write-Host "✓ 新用户连接测试成功" -ForegroundColor Green
} else {
    Write-Host "警告: 新用户连接测试失败，但数据库已创建" -ForegroundColor Yellow
    Write-Host "请手动验证连接" -ForegroundColor Yellow
}
Write-Host ""

# 显示配置说明
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  下一步操作" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "1. 配置后端 application.yml:" -ForegroundColor Yellow
Write-Host "   确保 backend/src/main/resources/application.yml 存在" -ForegroundColor White
Write-Host "   如果不存在，请从 application.yml.example 复制:" -ForegroundColor White
Write-Host "   Copy-Item backend\src\main\resources\application.yml.example backend\src\main\resources\application.yml" -ForegroundColor Gray
Write-Host ""
Write-Host "2. 设置环境变量（可选）:" -ForegroundColor Yellow
Write-Host "   `$env:BSCM_DB_PASSWORD='$BscmPassword'" -ForegroundColor Gray
Write-Host "   或永久设置:" -ForegroundColor White
Write-Host "   [System.Environment]::SetEnvironmentVariable('BSCM_DB_PASSWORD', '$BscmPassword', 'User')" -ForegroundColor Gray
Write-Host ""
Write-Host "3. 验证 application.yml 配置:" -ForegroundColor Yellow
Write-Host "   spring:" -ForegroundColor Gray
Write-Host "     datasource:" -ForegroundColor Gray
Write-Host "       url: jdbc:postgresql://$DbHost`:$Port/$DatabaseName" -ForegroundColor Gray
Write-Host "       username: $UserName" -ForegroundColor Gray
Write-Host "       password: `${BSCM_DB_PASSWORD:$BscmPassword}" -ForegroundColor Gray
Write-Host ""
Write-Host "✓ 数据库配置完成！" -ForegroundColor Green
Write-Host ""


