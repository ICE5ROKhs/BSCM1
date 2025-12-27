# BSCM - AI智能诊断应用

一个基于 Vue3 前端和 Spring Boot 后端的 AI 智能诊断应用项目（开发中）。

## 📋 目录

- [项目简介](#项目简介)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [环境要求](#环境要求)
- [快速开始](#快速开始)
- [已实现功能](#已实现功能)
- [API 接口](#api-接口)
- [日志系统](#日志系统)
- [开发计划](#开发计划)

## 项目简介

BSCM（AI智能诊断应用）是一个正在开发中的医疗诊断辅助系统项目。项目采用前后端分离架构，目前已完成用户认证系统和日志系统的基础功能。

## 技术栈

### 前端技术

- **框架**: Vue 3.4+ (Composition API)
- **构建工具**: Vite 5.1+
- **路由**: Vue Router 4.3+
- **状态管理**: Pinia 2.1+
- **HTTP 客户端**: Axios 1.6+
- **UI 组件库**: Element Plus 2.5+ (桌面端和移动端共用)
- **样式处理**:
  - PostCSS
  - PostCSS px-to-viewport (移动端适配)
  - Autoprefixer

### 后端技术

- **框架**: Spring Boot 3.2.0
- **安全**: Spring Security (JWT 认证)
- **数据访问**: Spring Data JPA
- **数据库**: PostgreSQL 12+
- **工具库**:
  - Lombok (简化代码)
  - JWT (io.jsonwebtoken 0.12.3)
  - Apache Commons IO

### 开发工具

- **前端**: ESLint, Prettier
- **后端**: Spotless Maven Plugin (代码格式化)

## 项目结构

```
BSCM/
├── frontend/                 # Vue3 前端项目
│   ├── src/
│   │   ├── api/             # API 接口封装
│   │   ├── router/          # 路由配置
│   │   ├── stores/          # Pinia 状态管理
│   │   ├── utils/           # 工具函数（日志、移动端检测等）
│   │   ├── views/           # 页面组件
│   │   └── styles/          # 样式文件
│   ├── package.json
│   └── vite.config.js
├── backend/                  # Spring Boot 后端项目
│   ├── src/main/java/com/bscm/
│   │   ├── config/          # 配置类（Security、Web、JWT等）
│   │   ├── controller/      # 控制器（Auth、Diagnosis、Log）
│   │   ├── service/         # 业务逻辑
│   │   ├── repository/      # 数据访问
│   │   ├── entity/          # 实体类
│   │   ├── logging/         # 日志记录器
│   │   └── util/            # 工具类
│   ├── src/main/resources/
│   │   ├── application.yml  # 应用配置
│   │   └── logback-spring.xml # 日志配置
│   └── pom.xml
├── scripts/                  # 数据库初始化脚本
├── LOGGING_SYSTEM.md         # 日志系统详细文档
└── README.md                 # 项目说明文档
```

## 环境要求

### 前端环境

- Node.js 16+ 
- npm 8+ 或 yarn 1.22+

### 后端环境

- JDK 17+
- Maven 3.6+
- PostgreSQL 12+

## 快速开始

### 1. 克隆项目

```bash
git clone <repository-url>
cd BSCM
```

### 2. 数据库配置

#### 方式一：使用 PowerShell 脚本（推荐）

```powershell
cd scripts
.\pg_init_bscm.ps1
```

#### 方式二：手动配置

1. 启动 PostgreSQL 服务

2. 创建数据库和用户：

```sql
-- 创建用户
CREATE ROLE bscm LOGIN PASSWORD 'bscm123456';

-- 创建数据库
CREATE DATABASE bscm_db OWNER bscm;

-- 授予权限
GRANT ALL PRIVILEGES ON DATABASE bscm_db TO bscm;
```

3. 修改后端配置（如需要）：

编辑 `backend/src/main/resources/application.yml`：

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/bscm_db
    username: bscm
    password: ${BSCM_DB_PASSWORD:bscm123456}  # 可通过环境变量覆盖
```

### 3. 启动后端服务

```bash
cd backend
mvn spring-boot:run
```

或者使用提供的启动脚本：

```bash
# Windows
start-backend.bat

# 或使用 IDE 运行 BscmApplication.java
```

后端服务默认运行在：`http://localhost:8080`

### 4. 启动前端服务

```bash
cd frontend
npm install
npm run dev
```

或者使用提供的启动脚本：

```bash
# Windows
start-frontend.bat
```

前端服务默认运行在：`http://localhost:5173`

### 5. 访问应用

- 前端地址: http://localhost:5173
- 后端 API: http://localhost:8080/api

## 已实现功能

### ✅ 用户认证系统

- **手机号注册**: 
  - 手机号唯一性验证
  - 验证码验证（6位数字，5分钟有效期）
  - 密码设置（BCrypt 加密存储）
  - 注册成功后自动登录

- **用户登录**: 
  - 手机号 + 密码登录
  - 记住密码功能（本地存储）
  - JWT Token 认证（24小时有效期）

- **安全特性**:
  - JWT Token 认证机制
  - 密码 BCrypt 加密存储
  - 验证码自动清理过期记录
  - 路由守卫保护

### ✅ 日志系统

- **前端日志**:
  - 多级别日志记录（DEBUG/INFO/WARN/ERROR）
  - 自动记录 API 请求和响应
  - 自动记录全局错误和未处理的 Promise 拒绝
  - 错误和警告日志自动发送到服务器
  - localStorage 本地存储（最多1000条）
  - 日志导出功能

- **后端日志**:
  - HTTP 请求日志（自动记录所有请求）
  - 业务日志（用户操作、业务事件）
  - 错误日志（异常和错误信息）
  - 日志文件按天滚动，自动归档
  - 客户端日志存储到数据库

详细使用说明请参考 [LOGGING_SYSTEM.md](./LOGGING_SYSTEM.md)

### ✅ 响应式设计

- 桌面端和移动端共用 Element Plus UI 组件库
- 响应式布局，适配不同屏幕尺寸
- PostCSS px-to-viewport 自动适配移动端

## API 接口

### 认证相关

| 方法 | 路径 | 说明 | 认证 | 状态 |
|------|------|------|------|------|
| POST | `/api/auth/send-code` | 发送验证码 | 否 | ✅ |
| POST | `/api/auth/register` | 用户注册 | 否 | ✅ |
| POST | `/api/auth/login` | 用户登录 | 否 | ✅ |

### 日志相关

| 方法 | 路径 | 说明 | 认证 | 状态 |
|------|------|------|------|------|
| POST | `/api/logs/client` | 接收客户端日志 | 否 | ✅ |

### 诊断相关（接口已实现，功能未完成）

| 方法 | 路径 | 说明 | 认证 | 状态 |
|------|------|------|------|------|
| POST | `/api/diagnosis/submit` | 提交诊断请求 | 是 | 🚧 |
| GET | `/api/diagnosis/history` | 获取诊断历史 | 是 | 🚧 |
| GET | `/api/diagnosis/{id}` | 获取诊断详情 | 是 | 🚧 |
| DELETE | `/api/diagnosis/{id}` | 删除诊断记录 | 是 | 🚧 |

**注意**: 诊断相关接口已实现，但 AI 诊断功能尚未集成，目前返回示例数据。

### 请求示例

#### 发送验证码

```bash
curl -X POST http://localhost:8080/api/auth/send-code \
  -H "Content-Type: application/json" \
  -d '{"phone": "13800138000"}'
```

#### 用户注册

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "13800138000",
    "password": "password123",
    "verificationCode": "123456"
  }'
```

#### 用户登录

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "13800138000",
    "password": "password123"
  }'
```

#### 提交诊断（需要认证）

```bash
curl -X POST http://localhost:8080/api/diagnosis/submit \
  -H "Authorization: Bearer <token>" \
  -F "symptoms=头痛、发热" \
  -F "images=@image1.jpg"
```

## 日志系统

项目集成了完整的前后端日志系统：

- **前端日志**: 多级别日志（DEBUG/INFO/WARN/ERROR），自动记录 API 请求响应，错误日志自动发送到服务器
- **后端日志**: HTTP 请求日志、业务日志、错误日志分离存储，支持日志文件滚动和归档
- **数据库日志**: 客户端错误和警告日志自动存储到数据库的 `system_logs` 表

详细使用说明请参考 [LOGGING_SYSTEM.md](./LOGGING_SYSTEM.md)

## 开发计划

### 已完成 ✅

- [x] 项目基础架构搭建
- [x] 用户认证系统
  - [x] 手机号注册
  - [x] 验证码发送和验证
  - [x] 用户登录（手机号+密码）
  - [x] JWT 认证
  - [x] 记住密码功能
- [x] 日志系统
  - [x] 前端日志系统
  - [x] 后端日志系统
  - [x] 数据库日志存储
- [x] 响应式设计
  - [x] Element Plus UI 集成
  - [x] 移动端适配

### 进行中 🚧

- [ ] AI 模型集成
- [ ] 诊断功能实现
- [ ] 诊断历史记录功能完善
- [ ] 用户信息管理

### 计划中 📋

- [ ] 诊断报告生成
- [ ] 数据统计分析
- [ ] 文件上传功能完善
- [ ] 用户权限管理
- [ ] 消息推送功能

## 部署说明

### 前端部署

1. 构建生产版本：

```bash
cd frontend
npm run build
```

2. 构建产物在 `frontend/dist` 目录，可部署到 Nginx 或其他静态文件服务器

3. 配置 Nginx（示例）：

```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /path/to/frontend/dist;
    
    location / {
        try_files $uri $uri/ /index.html;
    }
    
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

### 后端部署

1. 打包应用：

```bash
cd backend
mvn clean package
```

2. 运行 JAR 包：

```bash
java -jar target/bscm-backend-1.0.0.jar
```

3. 生产环境配置建议：

- 修改 `application.yml` 中的数据库连接信息
- 修改 JWT secret 密钥
- 调整日志级别为 INFO 或 WARN
- 配置 HTTPS
- 设置适当的文件上传大小限制

### 环境变量

后端支持通过环境变量覆盖配置：

- `BSCM_DB_PASSWORD`: 数据库密码
- `SPRING_PROFILES_ACTIVE`: Spring 配置文件（dev/prod）

## 开发规范

### 代码格式化

- **前端**: 使用 Prettier 格式化，运行 `npm run format`
- **后端**: 使用 Spotless Maven 插件，运行 `mvn spotless:apply`

### 代码检查

- **前端**: 运行 `npm run lint` 进行 ESLint 检查

## 常见问题

### Q: 前端无法连接后端 API？

A: 检查 `vite.config.js` 中的代理配置，确保后端服务已启动在 8080 端口。

### Q: 数据库连接失败？

A: 检查 PostgreSQL 服务是否启动，数据库和用户是否已创建，密码是否正确。

### Q: 验证码发送失败？

A: 当前版本验证码为模拟发送（开发环境会在日志中显示验证码），生产环境需要集成真实的短信服务。

### Q: JWT Token 过期？

A: 默认 Token 有效期为 24 小时，可在 `application.yml` 中修改 `jwt.expiration` 配置。

### Q: 诊断功能无法使用？

A: 诊断功能接口已实现，但 AI 模型尚未集成，目前返回示例数据。需要等待 AI 模型集成完成。

## 许可证

[待添加]

## 联系方式

[待添加]
