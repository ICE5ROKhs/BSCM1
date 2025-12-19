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

1. 安装并启动PostgreSQL服务

2. 创建数据库：
```sql
CREATE ROLE bscm LOGIN PASSWORD 'bscm';
CREATE DATABASE bscm_db OWNER bscm;
```

3. 修改 `src/main/resources/application.yml` 中的数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/bscm_db
    username: bscm
    password: bscm
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

