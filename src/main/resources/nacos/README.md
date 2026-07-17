# Nacos 模板

这些文件是 HydroCore 基线的 Nacos 模板。

| data-id | 模板 | 用途 |
|---|---|---|
| `hydrocore.yml` | `nacos/hydrocore.yml` | 主应用配置 |
| `hydrocore-constant.yml` | `nacos/hydrocore-constant.yml` | 动态常量 |
| `hydrocore-config.properties` | `nacos/hydrocore-config.properties` | 额外键值配置 |
| `redis.yml` | `nacos/redis.yml` | Redis 配置 |
| `mybatis-plus.yml` | `nacos/mybatis-plus.yml` | MyBatis-Plus 配置 |
| `pagehelper.yml` | `nacos/pagehelper.yml` | PageHelper 配置 |
| `sec-knife4j.yml` | `nacos/sec-knife4j.yml` | API 文档配置 |

全新基线环境需要先创建 HydroCore 命名空间，再发布这些文件。在本地开发以外的环境运行前，必须替换所有 `CHANGE_ME_*` 值。

```powershell
cd D:\project\HydroCore\hydrocore-be
.\scripts\publish-nacos-configs.ps1 -ServerAddr "localhost:8848" -Namespace "hydrocore" -Username "nacos" -Password "123456"
```

历史迁移脚本中可能仍会出现已移除的旧业务术语。当前 Nacos 模板不应默认启用这些能力。

## 线程池配置

`hydrocore.thread-pools` 统一管理 IO、CPU、后台任务和事件处理线程池。每类线程池支持：

- `core-size` / `max-size` / `queue-capacity`
- `keep-alive-seconds` / `thread-name-prefix`
- `allow-core-thread-timeout`
- `wait-for-tasks-to-complete-on-shutdown` / `await-termination-seconds`
- `rejection-policy: caller-runs`

当前拒绝策略固定为 `caller-runs`：队列满且线程达到上限时，由提交任务的线程执行任务，用调用方背压避免静默丢弃。
