# Fyrn Backend 项目开发指南

## 项目架构

这是一个基于 Spring Boot 3.4.12 + Maven 的多模块单体应用，使用 Java 17。

### 核心模块职责

- **fyrn-aggregation** - 应用程序入口，聚合所有业务模块，包含主类 `AggregationApplication`，打包成可执行 JAR
- **fyrn-biz-domain** - 领域层，存放 MyBatis-Plus 实体类（`entity`）和 Mapper 接口（`mapper`）
- **fyrn-biz-mms** - 媒体管理系统业务模块，包含小说相关的 Service 和 Controller
- **fyrn-common** - 通用工具、异常处理器（`GlobalExceptionHandler`）、基础配置
- **fyrn-oms/** - 运维管理系统（独立子项目），包含认证、任务调度、Redis 管理、参数管理等功能
  - `oms-pub` - 公共接口和工具类（如 `Result`）
  - `oms-biz` - 业务逻辑层
  - `oms-biz-impl` - 业务实现层
  - `oms-support` - 支撑服务
  - `oms-deploy` - 部署模块，包含主类 `OmsApplication`

### 依赖方向

```
fyrn-aggregation → fyrn-biz-mms → fyrn-common → oms-pub
                → fyrn-biz-domain
                → oms-biz-impl
                → oms-support
```

## 代码约定

### 包结构

- Controller: `com.arctel.<module>.controller`
- Service: `com.arctel.<module>.service` 和 `service.impl`
- Entity: `com.arctel.domain.dao.entity`
- Mapper: `com.arctel.domain.dao.mapper`

### API 响应格式

所有 Controller 方法必须返回 `Result<T>`（定义在 `oms-pub`）：

```java
@GetMapping("/page")
public Result<BaseQueryPage<MmsNovel>> page(MmsPageInput input) {
    return Result.success(mmsNovelService.pageMmsNovel(...));
}
```

### 实体类约定

- 使用 `@TableName` 映射数据库表名
- 主键使用雪花算法：`@TableId(type = IdType.ASSIGN_ID)`
- 逻辑删除：`@TableLogic(value = "2", delval = "4")` - 2 正常，4 删除
- 自动填充时间：`@TableField(fill = FieldFill.INSERT)` / `FieldFill.INSERT_UPDATE`

### Service 层约定

- Service 实现必须继承 `ServiceImpl<Mapper, Entity>` 并实现自定义接口
- 需要事务的方法使用 `@Transactional`
- 自引用需注入 `self`：`@Resource XxxService self;`（用于方法内部调用启用事务/AOP）

### License Header

所有 Java 文件必须包含 Apache License 2.0 头部注释（由 Spotless Maven 插件自动应用）。编译时会自动添加，不需要手动编写。

## 技术栈

- **持久化**: MyBatis-Plus 3.5.9 + Druid 连接池
- **缓存**: Redis + Caffeine（本地缓存）
- **文件存储**: MinIO
- **工具库**: Hutool 5.8.27, Apache Commons Lang3, Fastjson2
- **文档**: SpringDoc OpenAPI 3
- **验证**: Jakarta Validation

## 开发工作流

### 构建和运行

```bash
# 根目录构建所有模块
mvn clean package -DskipTests

# 运行主应用（开发环境）
cd fyrn-aggregation
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Docker 构建和运行（生产环境）
docker-compose up --build
```

### 配置管理

- 基础配置：`fyrn-aggregation/src/main/resources/application.yml`
- 开发环境：`fyrn-backend/config/application-dev.yml`
- 生产环境：容器内通过 volume 挂载 `application-prod.yml`
- 配置占位符：使用自定义前缀 `${fyrn.datasource.*}`、`${fyrn.redis.*}`、`${fyrn.minio.*}`

### 数据库

- MySQL 8.x，连接参数：`serverTimezone=Asia/Shanghai`, `useSSL=false`
- SQL 初始化脚本位于 `SQL/` 目录（`fyrn-init.sql`, `oms-init.sql`）
- 使用 Docker 开发时数据库主机为 `host.docker.internal`

### 端口

- 应用端口：45600（见 `application.yml` 和 `docker-compose.yml`）
- Redis: 6379
- MySQL: 3306
- MinIO: 9000

## 特殊注意事项

1. **循环依赖**：项目允许循环引用（`spring.main.allow-circular-references=true`），但应避免创建新的循环依赖
2. **Docker 构建**：Dockerfile 使用多阶段构建，最终打包 `fyrn-aggregation.jar`，需确保所有模块在 aggregation 前编译完成
3. **时区**：容器和数据库连接统一使用 `Asia/Shanghai`
4. **代码格式化**：Spotless 在编译阶段自动应用，无需手动格式化
5. **接口开发**: 只允许使用 GET 和 POST 方法，禁止使用 PUT、DELETE 等其他 HTTP 方法
