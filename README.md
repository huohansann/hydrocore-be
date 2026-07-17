# HydroCore Backend

HydroCore backend is the Spring Boot foundation for a water-treatment secondary-development baseline. It keeps generic capabilities only: authentication, authorization, users, roles, menus, organizations, system configuration, device/point management, generic data query plumbing, Redis, Nacos, TDengine access, MQTT infrastructure, and WebSocket support.

Real process models, control algorithms, prediction algorithms, production reports, and plant-specific data are not part of this baseline.

## Tech Stack

- JDK 8
- Spring Boot 2.6.13
- Spring Cloud / Alibaba 2021.0.5 / 2021.0.5.0
- MyBatis-Plus 3.4.3.1
- MySQL, Redis, TDengine, Nacos
- Knife4j, JWT, STOMP/WebSocket

## Entry Point

```text
com.siact.hydrocore.HydrocoreApplication
```

## Database

Import `src/main/resources/db/schema/hydrocore_schema.sql` for a fresh local baseline.

The seed account `admin / ChangeMe123!` is for local development only. Change its password or recreate accounts before any production deployment.

## Nacos

Use the templates in `src/main/resources/nacos/`. Production secrets, database hosts, Redis hosts, TDengine hosts, and external service URLs must be supplied by the deployment environment.

Publish local templates with:

```powershell
.\scripts\publish-nacos-configs.ps1 -ServerAddr "localhost:8848" -Namespace "hydrocore" -Username "nacos" -Password "123456"
```

## Verify

```powershell
mvn -q -DskipTests compile
mvn -q test
```

Private dependencies may require VPN or internal Nexus access.

## Documentation

- Backend architecture: `docs/architecture.md`
- System config API: `docs/api/sys-config-api.md`
- Root Comet/OpenSpec work products: `..\docs\` and `..\openspec\`
