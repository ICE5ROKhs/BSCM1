# BSCM 后端项目

基于Spring Boot构建的AI智能诊断后端服务。

## 技术栈

- Spring Boot 3.2.0
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT
- Lombok

## 环境要求

- JDK 17+
- Maven 3.6+
- PostgreSQL 12+

## 配置数据库

### 首次设置

1. **创建配置文件**（配置文件已添加到 `.gitignore`，不会提交到版本控制）：
   ```bash
   # 从模板文件复制
   cp src/main/resources/application.yml.example src/main/resources/application.yml
   ```

2. **编辑配置文件** `src/main/resources/application.yml`，设置你的数据库密码

### 方式一：使用 postgres 用户（推荐用于快速开发）

1. 安装并启动PostgreSQL服务

2. 确保 postgres 用户密码已设置（如果未设置，使用以下命令）：
```sql
-- 在 PostgreSQL 命令行中执行
ALTER USER postgres WITH PASSWORD '你的密码';
```

3. 创建数据库：
```sql
CREATE DATABASE bscm_db;
```

4. 配置数据库连接：
   - 编辑 `src/main/resources/application.yml`
   - 设置用户名：`postgres`
   - 设置密码：可通过环境变量 `BSCM_DB_PASSWORD` 或直接在配置文件中填写

### 方式二：创建专用用户（推荐用于生产环境）

1. 安装并启动PostgreSQL服务

2. 创建专用用户和数据库：
```sql
CREATE ROLE bscm LOGIN PASSWORD 'bscm';
CREATE DATABASE bscm_db OWNER bscm;
```

3. 编辑 `src/main/resources/application.yml`，修改数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:postgresql://127.0.0.1:5432/bscm_db
    username: bscm
    password: bscm
```

**注意**：如果 `application.yml` 不存在，请先执行：
```bash
cp src/main/resources/application.yml.example src/main/resources/application.yml
```

### 密码认证失败问题解决

如果遇到 "Password 认证失败" 错误，请按以下步骤操作：

**Windows PowerShell:**
```powershell
# 设置环境变量（当前会话有效）
$env:BSCM_DB_PASSWORD="你的实际密码"

# 或者永久设置（需要重启终端）
[System.Environment]::SetEnvironmentVariable('BSCM_DB_PASSWORD', '你的实际密码', 'User')
```

**或者直接修改配置文件：**
编辑 `src/main/resources/application.yml`，将第9行的密码改为你的实际密码：
```yaml
password: 你的实际密码  # 替换 ${BSCM_DB_PASSWORD:123456}
```

**验证 PostgreSQL 连接：**
```bash
# 使用 psql 命令行工具测试连接
psql -U postgres -d bscm_db
```

## 运行项目

```bash
mvn spring-boot:run
```

或使用IDE直接运行 `BscmApplication.java`

## API接口

- POST `/api/diagnosis/submit` - 提交诊断请求
- GET `/api/diagnosis/history` - 获取诊断历史
- GET `/api/diagnosis/{id}` - 获取诊断详情
- DELETE `/api/diagnosis/{id}` - 删除诊断记录

## 数据库实体

项目已包含以下JPA实体：

- `User` - 用户实体
- `DiagnosisRecord` - 诊断记录实体

## Repository接口

- `UserRepository` - 用户数据访问接口
- `DiagnosisRecordRepository` - 诊断记录数据访问接口

## 开发计划

- [x] JPA实体设计
- [x] Repository接口
- [ ] 用户认证系统
- [ ] AI模型集成
- [ ] 文件上传处理
- [ ] 日志记录

