# 日志系统使用说明

本项目已集成完整的前后端日志系统，用于记录系统运行状态、用户操作和错误信息。

## 前端日志系统

### 功能特性

- **多级别日志**：支持 DEBUG、INFO、WARN、ERROR 四个级别
- **多种存储方式**：
  - 控制台输出（开发环境）
  - localStorage 本地存储（浏览器）
  - 服务器发送（错误和警告自动发送）
- **自动记录**：
  - API 请求和响应
  - 全局错误和未处理的 Promise 拒绝
  - 用户操作

### 使用方法

```javascript
import logger from '@/utils/logger'

// 记录不同级别的日志
logger.debug('调试信息', { data: 'some data' })
logger.info('普通信息', { data: 'some data' })
logger.warn('警告信息', { data: 'some data' })
logger.error('错误信息', { data: 'some data' })

// API 日志会自动记录（已在拦截器中集成）

// 获取存储的日志
const logs = logger.getStoredLogs()

// 导出日志
logger.exportLogs()

// 清空存储的日志
logger.clearStoredLogs()
```

### 配置

在 `frontend/src/utils/logger.js` 中可以配置：

- `logLevel`: 日志级别（默认 INFO）
- `enableConsole`: 是否输出到控制台（默认 true）
- `enableStorage`: 是否存储到 localStorage（默认 true）
- `enableServer`: 是否发送到服务器（默认 true）
- `maxStorageSize`: localStorage 最大存储条数（默认 1000）

## 后端日志系统

### 日志文件

日志文件存储在 `backend/logs/` 目录下：

- `bscm.log` - 所有日志
- `bscm-error.log` - 错误日志
- `bscm-http.log` - HTTP 请求日志
- `bscm-business.log` - 业务日志

日志文件按天滚动，保留 30 天（错误日志保留 90 天）。

### 日志级别

- **HTTP 请求日志**：记录所有 HTTP 请求和响应
- **业务日志**：记录业务操作（用户注册、登录、诊断等）
- **错误日志**：记录所有错误和异常

### 使用方法

#### 业务日志记录器

```java
@Autowired
private BusinessLogger businessLogger;

// 记录用户操作
businessLogger.logUserOperation("用户登录", userId, "详情信息");

// 记录业务事件
businessLogger.logBusinessEvent("提交诊断", "详情信息");

// 记录业务错误
businessLogger.logBusinessError("诊断失败", "错误详情", exception);
```

#### HTTP 请求日志

HTTP 请求日志会自动记录（通过拦截器），包括：
- 请求方法、URI、参数
- 客户端 IP 地址
- 请求头（敏感信息已隐藏）
- 响应状态码
- 请求耗时

#### 客户端日志接收

前端发送的 ERROR 和 WARN 级别日志会自动存储到数据库的 `system_logs` 表中。

### 配置

日志配置在 `backend/src/main/resources/logback-spring.xml` 中：

- 日志文件路径：`./logs/`
- 日志保留天数：3 天（错误日志 9 天）
- 日志文件大小限制：1GB（错误日志 500MB）

## 数据库日志表

系统会自动创建 `system_logs` 表用于存储客户端日志，包含以下字段：

- `id` - 主键
- `level` - 日志级别
- `message` - 日志消息
- `data` - 日志数据（JSON）
- `user_id` - 用户ID
- `url` - 请求URL
- `user_agent` - 用户代理
- `created_at` - 创建时间

## 安全考虑

- 敏感信息（密码、token 等）会自动隐藏
- 手机号中间部分会被隐藏（如：138****1234）
- 日志文件权限应限制为仅管理员可访问

## 性能优化

- 使用异步日志记录（AsyncAppender）
- 不记录大型响应体（超过 10KB 会被截断）
- 客户端日志发送使用 sendBeacon，不阻塞页面关闭

## 日志查看

### 前端日志

1. 浏览器控制台：开发环境下所有日志都会输出
2. localStorage：使用 `logger.getStoredLogs()` 查看
3. 导出日志：使用 `logger.exportLogs()` 导出为 JSON 文件

### 后端日志

1. 日志文件：直接查看 `backend/logs/` 目录下的日志文件
2. 数据库：查询 `system_logs` 表查看客户端日志
3. 控制台：开发环境下会同时输出到控制台

## 注意事项

1. 生产环境建议将日志级别设置为 INFO 或 WARN
2. 定期清理旧日志文件，避免磁盘空间不足
3. 敏感操作日志应加密存储
4. 日志文件应定期备份






