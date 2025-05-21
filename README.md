# kiln-intelligent-control 项目说明

## 项目简介

**kiln-intelligent-control**（窑炉控制系统）是基于 Spring Boot 的智能控制平台，主要用于窑炉的数字孪生、数据采集、权限管理等业务场景，具备微服务架构、分布式配置、权限安全、数据可视化等能力。

---

## 技术栈与核心依赖

- **JDK 版本**：1.8
- **Spring Boot 版本**：2.6.13
- **Spring Cloud 版本**：2021.0.5
- **Spring Cloud Alibaba 版本**：2021.0.5.0
- **MyBatis-Plus 版本**：3.4.3.1
- **MySQL 驱动**：8.0.33
- **Redis 客户端**：Spring Data Redis
- **Knife4j（API文档）**：4.3.0
- **EasyPoi（Excel导入导出）**：4.4.0
- **Hutool 工具包**：5.7.22
- **JWT 支持**：jjwt 0.11.5
- **fastjson2**：2.0.45
- **Lombok**：1.18.24（仅开发期依赖）

---

## 主要功能模块

- **数字孪生**：集成 `siact-common-code` 与 `siact-sec-api-feign`，实现窑炉数字孪生相关业务。
- **权限管理**：基于 Spring Security，支持用户、角色、菜单、组织等权限体系。
- **数据采集与展示**：通过 MyBatis-Plus、Redis 实现高效数据存储与缓存。
- **API 文档**：集成 Knife4j，自动生成接口文档，便于前后端联调。
- **分布式配置与注册中心**：使用 Nacos 作为配置中心和服务注册中心，支持多环境（如 dev、uat）切换。
- **WebSocket 支持**：实现实时数据推送与前端交互。
- **AOP 切面**：支持日志、权限等横切关注点。

---

## 配置说明

### 1. 多环境配置

- 配置文件采用 `bootstrap.yml` 及 `bootstrap-dev.yml`、`bootstrap-uat.yml` 等多环境分离。
- 通过 `spring.profiles.active` 指定当前激活环境（如 `uat`）。
- Nacos 配置中心地址、命名空间等在各环境配置文件中分别指定。

### 2. Nacos 配置中心

- 通过 `spring.cloud.nacos.config` 配置，支持扩展加载多个业务配置文件（如 `kiln-intelligent-control.yml`、`redis.yml`、`mybatis-plus.yml` 等）。
- 注册中心与配置中心均指向 Nacos，支持独立命名空间。

### 3. 数据库与缓存

- 数据库驱动为 MySQL 8.0.33，ORM 框架为 MyBatis-Plus。
- Redis 用于缓存用户信息、权限等，配置见 `RedisConfig.java`。

### 4. 安全与认证

- 使用 Spring Security 进行接口安全保护。
- JWT 用于用户认证，密钥与过期时间在配置文件中指定。

---

## 启动入口

主启动类为：

```java
com.siact.KilnApplication
```

---

## 依赖管理与构建

- 构建工具：Maven
- 依赖管理采用 `dependencyManagement`，统一 Spring Cloud、Alibaba 版本。
- 插件配置包括 `spring-boot-maven-plugin`、`maven-compiler-plugin`、`maven-surefire-plugin` 等。

---

## 注意事项

1. **私有依赖**：部分依赖（如 `siact-common-code`、`siact-sec-api-feign`）需从公司私有 Nexus 仓库下载，外部环境可能无法直接构建。
2. **Nacos 配置**：需保证 Nacos 服务可用，且相关配置文件已上传至对应命名空间。
3. **数据库与Redis**：请根据实际环境配置数据库与Redis连接信息。

---

## 参考配置片段

**bootstrap.yml**
```yaml
spring:
  application:
    name: kiln-intelligent-control
  profiles:
    active: uat
```

**bootstrap-dev.yml**
```yaml
spring:
  cloud:
    nacos:
      server-addr: 192.100.4.26:8848
      namespace: ylkz
      # 其他配置...
```

---

## 联系方式

如需技术支持或有疑问，请联系项目负责人或相关开发同事。

---

如需进一步补充详细模块介绍、接口文档或部署说明，请告知！ 