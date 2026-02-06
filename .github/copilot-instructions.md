# Fyrn Backend 项目开发指南

## 项目架构

这是一个基于 Spring Boot 3.4.13 + Maven 的多模块单体应用，使用 Java 17。

### 核心模块职责

- **fyrn-aggregation** - 应用程序入口，聚合所有业务模块，包含主类 `AggregationApplication`，打包成可执行 JAR
- **fyrn-mms/** - 媒体管理系统（小说管理）
  - `fyrn-mms-api` - Service 接口定义
  - `fyrn-mms-service` - Service 实现 + Controller
  - `fyrn-mms-domain` - 实体类（entity）和 Mapper 接口
- **fyrn-common** - 通用工具、异常处理器（`GlobalExceptionHandler`）、基础配置
- **fyrn-oms/** - 运维管理系统（独立子项目），包含认证、任务调度、Redis 管理、参数管理等功能
  - `oms-api` - Service 接口定义
  - `oms-service` - Service 实现 + Controller
  - `oms-domain` - 实体类和 Mapper 接口
  - `oms-common` - 公共工具类（如 `Result`）

### 依赖方向

```
fyrn-aggregation → fyrn-mms-service → fyrn-mms-api → fyrn-common → oms-common
                                   → fyrn-mms-domain
                → fyrn-oms-service → fyrn-oms-api
                                   → fyrn-oms-domain
```

## 代码约定

### 包结构

- Controller: `com.arctel.<module>.controller` (在 `*-service` 模块中)
- Service 接口: `com.arctel.<module>.service` (在 `*-api` 模块中)
- Service 实现: `com.arctel.<module>.service.impl` (在 `*-service` 模块中)
- Entity: `com.arctel.<module>.entity` (在 `*-domain` 模块中)
- Mapper: `com.arctel.<module>.mapper` (在 `*-domain` 模块中)

### API 响应格式

所有 Controller 方法必须返回 `Result<T>`（定义在 `oms-common`）：

```java
@RestController
@RequestMapping("/mms")
public class MmsNovelController {
    @GetMapping("/page")
    public Result<BaseQueryPage<MmsNovel>> page(MmsNovel input, Integer pageNo, Integer pageSize) {
        return Result.success(mmsNovelService.pageMmsNovel(input, pageNo, pageSize));
    }
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
# 根目录（fyrn-backend/）构建所有模块
cd fyrn-backend
mvn clean package -DskipTests

# 运行主应用（开发环境）
cd fyrn-aggregation
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# 运行测试（整个项目）
cd fyrn-backend
mvn test

# 运行单个测试类
cd fyrn-backend
mvn test -Dtest=FileUtilTest

# 运行单个测试方法
mvn test -Dtest=FileUtilTest#cosineSimilarity

# 代码格式化（Spotless 在编译时自动应用）
mvn spotless:apply

# Docker 启动基础设施（MySQL, Redis, MinIO）
cd .. # 回到项目根目录
docker-compose up -d
```

### 配置管理

- 基础配置：`fyrn-backend/fyrn-aggregation/src/main/resources/application.yml`
- 开发环境：`fyrn-backend/config/application-dev.yml` 或 `fyrn-aggregation/src/main/resources/application-dev.yml`
- 生产环境：容器内通过 volume 挂载 `application-prod.yml`
- 配置占位符：使用自定义前缀 `${fyrn.datasource.*}`、`${fyrn.redis.*}`、`${fyrn.minio.*}`
- 默认 profile：`prod`（在 `application.yml` 中设置，开发时需要覆盖为 `dev`）

### 数据库

- MariaDB 10.11（兼容 MySQL 8.x），连接参数：`serverTimezone=Asia/Shanghai`, `useSSL=false`
- SQL 初始化脚本位于项目根目录 `SQL/` 目录（`fyrn-init.sql`, `oms-init.sql`）
- Docker 开发环境端口映射：
  - MariaDB: `localhost:53306` → `container:3306`
  - Redis: `localhost:56379` → `container:6379`
  - MinIO: `localhost:59000` → `container:9000` (API), `59001` → `9001` (Console)

### 端口

- 应用端口：45600（见 `fyrn-aggregation/src/main/resources/application.yml`）
- Docker 服务端口（宿主机访问）：
  - MariaDB: 53306
  - Redis: 56379
  - MinIO API: 59000
  - MinIO Console: 59001

## 特殊注意事项

1. **循环依赖**：项目允许循环引用（`spring.main.allow-circular-references=true`），但应避免创建新的循环依赖
2. **模块构建顺序**：Maven 构建时需要先构建依赖模块（common, domain, api）再构建 service 和 aggregation 模块
3. **时区**：容器和数据库连接统一使用 `Asia/Shanghai`
4. **代码格式化**：Spotless 在编译阶段（compile phase）自动应用 Apache License 2.0 头部，无需手动添加
5. **接口开发**：只允许使用 GET 和 POST 方法，禁止使用 PUT、DELETE 等其他 HTTP 方法
6. **项目结构**：注意项目有两层目录结构 - 根目录包含 `docker-compose.yaml` 和 `SQL/`，`fyrn-backend/` 子目录包含所有 Java 模块
