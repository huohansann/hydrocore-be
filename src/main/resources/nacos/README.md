# Nacos Templates

These files are the HydroCore baseline Nacos templates.

| data-id | Template | Purpose |
|---|---|---|
| `hydrocore.yml` | `nacos/hydrocore.yml` | Main application settings |
| `hydrocore-constant.yml` | `nacos/hydrocore-constant.yml` | Dynamic constants |
| `hydrocore-config.properties` | `nacos/hydrocore-config.properties` | Extra key/value properties |
| `redis.yml` | `nacos/redis.yml` | Redis settings |
| `mybatis-plus.yml` | `nacos/mybatis-plus.yml` | MyBatis-Plus settings |
| `pagehelper.yml` | `nacos/pagehelper.yml` | PageHelper settings |
| `sec-knife4j.yml` | `nacos/sec-knife4j.yml` | API documentation settings |

For a fresh baseline, create a HydroCore namespace and publish these files. Replace every `CHANGE_ME_*` value before running outside local development.

```powershell
cd D:\project\HydroCore\hydrocore-be
.\scripts\publish-nacos-configs.ps1 -ServerAddr "localhost:8848" -Namespace "hydrocore" -Username "nacos" -Password "123456"
```

Historical migration scripts may mention removed legacy business terms. Current Nacos templates should not enable those capabilities by default.
