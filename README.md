# HydroCore 后端

HydroCore 后端是水处理系统二次开发基线中的 Spring Boot 底座。它只保留通用能力：认证、授权、用户、角色、菜单、组织、系统配置、设备与点位管理、通用数据查询管道、Redis、Nacos、TDengine 接入、MQTT 基础设施和 WebSocket 支持。

真实工艺模型、控制算法、预测算法、生产报表和现场专用数据不属于当前基线范围。

## 技术栈

- JDK 8
- Spring Boot 2.6.13
- Spring Cloud / Alibaba 2021.0.5 / 2021.0.5.0
- MyBatis-Plus 3.4.3.1
- MySQL, Redis, TDengine, Nacos
- Knife4j, JWT, STOMP/WebSocket

## 启动入口

```text
com.siact.hydrocore.HydrocoreApplication
```

## 数据库

全新本地基线环境请导入 `src/main/resources/db/schema/hydrocore_schema.sql`。

初始化账号 `admin / ChangeMe123!` 仅用于本地开发。生产部署前必须修改密码或重新创建账号。

## Nacos

使用 `src/main/resources/nacos/` 中的模板。生产密钥、数据库地址、Redis 地址、TDengine 地址和外部服务 URL 必须由部署环境提供。

发布本地模板：

```powershell
.\scripts\publish-nacos-configs.ps1 -ServerAddr "localhost:8848" -Namespace "hydrocore" -Username "nacos" -Password "123456"
```

## 验证

```powershell
mvn -q -DskipTests compile
mvn -q test
```

私有依赖可能需要 VPN 或内部 Nexus 访问权限。

## 文档

- 后端架构：`docs/architecture.md`
- 系统配置 API：`docs/api/sys-config-api.md`
- 根仓库 Comet/OpenSpec 产物：`..\docs\` 和 `..\openspec\`
