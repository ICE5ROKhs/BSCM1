# BSCM 数据库配置脚本

本目录包含用于自动配置 BSCM 项目数据库的脚本。

## 脚本说明

### setup-database.ps1

PowerShell 自动化脚本，用于创建 PostgreSQL 数据库和用户。

**功能：**
- 检查 PostgreSQL 安装和服务状态
- 自动创建数据库用户 `bscm`
- 自动创建数据库 `bscm_db`
- 自动授予必要权限
- 验证配置并显示连接信息

**使用方法：**

```powershell
# 基本使用（会提示输入 postgres 密码）
.\setup-database.ps1

# 指定 postgres 用户密码
.\setup-database.ps1 -PostgresPassword "你的postgres密码"

# 指定 bscm 用户密码（默认：bscm123456）
.\setup-database.ps1 -BscmPassword "自定义密码"

# 完整参数示例
.\setup-database.ps1 `
    -PostgresPassword "postgres密码" `
    -BscmPassword "bscm123456" `
    -DbHost "127.0.0.1" `
    -Port 5432 `
    -DatabaseName "bscm_db" `
    -UserName "bscm"
```

**参数说明：**

| 参数 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `PostgresPassword` | string | "" | postgres 超级用户密码（为空时会提示输入） |
| `BscmPassword` | string | "bscm123456" | bscm 用户密码 |
| `DbHost` | string | "127.0.0.1" | PostgreSQL 服务器地址 |
| `Port` | int | 5432 | PostgreSQL 服务器端口 |
| `DatabaseName` | string | "bscm_db" | 要创建的数据库名 |
| `UserName` | string | "bscm" | 要创建的用户名 |

### setup-database.bat

批处理脚本，用于在命令提示符中调用 PowerShell 脚本。

**使用方法：**

```cmd
setup-database.bat
```

## 前置要求

1. **PostgreSQL 已安装**
   - 确保 PostgreSQL 已正确安装
   - `psql` 命令在 PATH 环境变量中可用

2. **PostgreSQL 服务运行中**
   - 脚本会自动尝试启动服务，但建议手动确保服务正在运行

3. **postgres 用户密码**
   - 需要知道 postgres 超级用户的密码
   - 如果未通过参数提供，脚本会提示输入

## 执行权限

### 问题：执行策略限制

如果遇到以下错误：
```
File cannot be loaded because running scripts is disabled on this system.
```

这是因为 Windows PowerShell 的执行策略限制了脚本运行。

### 解决方案

**方案 1：使用批处理脚本（推荐）**

批处理脚本已内置绕过执行策略，直接运行：

```cmd
cd scripts
setup-database.bat
```

**方案 2：在 PowerShell 中临时绕过**

```powershell
# 方法 1：单次执行时绕过（推荐）
powershell -ExecutionPolicy Bypass -File .\setup-database.ps1

# 方法 2：临时修改当前会话的执行策略
Set-ExecutionPolicy -ExecutionPolicy Bypass -Scope Process
.\setup-database.ps1
```

**方案 3：永久修改执行策略（可选，有安全风险）**

```powershell
# 查看当前执行策略
Get-ExecutionPolicy

# 修改为 RemoteSigned（允许本地未签名脚本运行）
Set-ExecutionPolicy -ExecutionPolicy RemoteSigned -Scope CurrentUser

# 然后就可以直接运行脚本
.\setup-database.ps1
```

**执行策略说明：**
- `Restricted`：默认策略，禁止所有脚本运行
- `RemoteSigned`：允许本地脚本运行，远程脚本需要签名（推荐）
- `Bypass`：绕过所有策略检查（仅用于临时测试）

## 故障排除

### 问题：找不到 psql 命令

**解决方案：**
1. 确保 PostgreSQL 已安装
2. 将 PostgreSQL 的 bin 目录添加到 PATH 环境变量
   - 通常位于：`C:\Program Files\PostgreSQL\<version>\bin`
3. 重新打开终端后重试

### 问题：无法连接到 PostgreSQL

**检查项：**
1. PostgreSQL 服务是否正在运行
2. postgres 用户密码是否正确
3. 连接参数（Host、Port）是否正确
4. PostgreSQL 的 `pg_hba.conf` 配置是否允许连接

### 问题：用户或数据库已存在

**说明：**
- 脚本会自动检测用户和数据库是否已存在
- 如果已存在，会跳过创建步骤
- 如果用户已存在，会更新密码

## 验证配置

脚本执行成功后，可以通过以下方式验证：

```powershell
# 使用 psql 连接测试
$env:PGPASSWORD="bscm123456"
psql -h 127.0.0.1 -p 5432 -U bscm -d bscm_db -c "SELECT current_database(), current_user;"
$env:PGPASSWORD=""
```

## 后续步骤

脚本执行完成后：

1. **创建 application.yml 配置文件**（如果不存在）：
   ```powershell
   Copy-Item backend\src\main\resources\application.yml.example backend\src\main\resources\application.yml
   ```

2. **设置环境变量**（可选）：
   ```powershell
   # 临时设置（当前会话）
   $env:BSCM_DB_PASSWORD="bscm123456"
   
   # 永久设置（用户级别）
   [System.Environment]::SetEnvironmentVariable('BSCM_DB_PASSWORD', 'bscm123456', 'User')
   ```

3. **验证 application.yml 配置**：
   确保 `backend/src/main/resources/application.yml` 中的数据库配置正确：
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://127.0.0.1:5432/bscm_db
       username: bscm
       password: ${BSCM_DB_PASSWORD:bscm123456}
   ```


