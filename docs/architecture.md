# Backend Architecture

HydroCore backend is a generic Spring Boot foundation for later water-treatment modules. Current baseline capability is generic system foundation only. Forecasting, level/pressure control, process-specific monitoring, plant reporting, and production plant data are not part of this baseline.

## Runtime

- Entry point: `com.siact.hydrocore.HydrocoreApplication`
- Config: `bootstrap.yml` loads Nacos data IDs for application, Redis, MyBatis-Plus, PageHelper, constants, Knife4j, and extra properties.
- Security: JWT filter, `LoginContext`, and Spring Security integration under `core.security`.
- Response shape: REST APIs return `com.siact.hydrocore.common.result.R<T>`; paginated APIs use `PageVO<T>`.
- WebSocket: STOMP over `/ws`; clients authenticate on CONNECT.
- TDengine: JDBC access for generic time-series/data-code queries.

## Package Responsibilities

| Package | Responsibility |
|---|---|
| `com.siact.hydrocore.module.system` | Users, roles, menus, organizations, permissions, system configuration |
| `com.siact.hydrocore.module.device` | Devices, points, and realtime query foundation |
| `com.siact.hydrocore.sec` | Generic external data integration caller |
| `com.siact.hydrocore.tdengine` | TDengine query and insert utilities |
| `com.siact.hydrocore.core` | Security, WebSocket, event bus, global response/error handling |
| `com.siact.hydrocore.common` | Shared DTOs, constants, utilities, annotations |
| `com.siact.hydrocore.config` | Application-level configuration beans |

## Extension Rule

Add real water-treatment capabilities through a new OpenSpec/Comet change. Keep domain schema, process dashboards, algorithms, report templates, and plant-specific integration values out of the baseline.

Historical migration scripts may mention removed legacy business names only to help old installations migrate into this clean baseline.
