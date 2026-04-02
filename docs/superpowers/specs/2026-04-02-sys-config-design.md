---
name: sys-config-design
description: 选项式配置功能设计 - 对象扁平化存储与路径索引
type: project
---

# 选项式配置功能设计文档

## 1. 概述

### 1.1 背景

系统界面中存在大量选项式组件（下拉框、多选框等），这些组件的配置项过多，为每个配置创建独立的数据表不合适，且修改维护困难。需要一个统一的配置管理系统来集中管理这些配置。

### 1.2 目标

设计一个通用的配置存储方案，支持：
- 配置对象的扁平化存储（JSON → 多行数据）
- 路径索引机制（通过 sc_path 记录对象 key 链路）
- 读取时自动组装还原为完整 JSON 对象
- 界面系统设置管理

### 1.3 核心设计思路

将复杂 JSON 配置对象拆解为多行扁平数据存储，每行记录一个叶子节点的值及其路径索引。读取时通过路径重组还原为完整对象结构。

---

## 2. 架构设计

### 2.1 分层架构

```
┌─────────────────────────────────────────────────────────────┐
│                      Controller 层                           │
│  SysConfigController - 提供 REST API                         │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                      Service 层                              │
│  SysConfigService - 业务编排、缓存管理、事务控制              │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                      处理器层                                │
│  ConfigFlattener  - 对象 → 多行扁平化                        │
│  ConfigAssembler  - 多行 → 对象组装                          │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                      数据层                                  │
│  SysConfigEntity + SysConfigMapper + Redis Cache            │
└─────────────────────────────────────────────────────────────┘
```

### 2.2 职责划分

| 组件 | 职责 |
|------|------|
| Controller | 接收请求、参数校验、返回响应 |
| Service | 业务逻辑编排、缓存读写、乐观锁处理、事务管理 |
| ConfigFlattener | 纯逻辑组件，将 JSON 对象转换为 `List<SysConfigEntity>` |
| ConfigAssembler | 纯逻辑组件，将 `List<SysConfigEntity>` 组装为 JSON 对象 |
| Mapper | MyBatis Plus 数据访问 |

---

## 3. 数据模型

### 3.1 表结构

```sql
create table if not exists sys_config
(
    id          bigint              not null primary key comment '主键',
    module      varchar(255)        not null comment '模块: SYSTEM, CONTROL, FORECAST 等',
    sc_code     varchar(255)        not null comment '配置编码，全局唯一',
    sc_path     varchar(255)        not null comment '配置路径: 如 devices.[0].name',
    sc_name     varchar(500)        not null comment '配置名称',
    sc_type     varchar(50)         not null comment '配置类型: STRING, INTEGER, FLOAT, DOUBLE, DECIMAL, BOOLEAN, TIMESTAMP',
    sc_value    text                not null comment '配置值',
    description varchar(255)        not null comment '配置说明',
    version     int       default 1 not null comment '乐观锁版本号',
    create_time timestamp default current_timestamp,
    update_time timestamp default current_timestamp on update current_timestamp,
    unique key uk_sc_code_path (sc_code, sc_path)
);
```

### 3.2 唯一性约束

- `(sc_code, sc_path)` 组合唯一：同一配置编码下路径不重复
- `sc_code` 全局唯一：通过业务逻辑保证，简化 API 使用

### 3.3 枚举定义

**SysConfigModuleEnum：**
```java
public enum SysConfigModuleEnum {
    SYSTEM,    // 系统配置
    CONTROL,   // 控制模块配置
    FORECAST   // 预测模块配置
}
```

**SysConfigTypeEnum：**
```java
public enum SysConfigTypeEnum {
    STRING,    // 字符串
    INTEGER,   // 整数
    FLOAT,     // 单精度浮点
    DOUBLE,    // 双精度浮点
    DECIMAL,   // 高精度数值
    BOOLEAN,   // 布尔值
    TIMESTAMP  // 时间戳
}
```

---

## 4. 接口设计

### 4.1 单配置 CRUD

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 获取配置 | GET | `/sys-config/{scCode}` | 返回组装后的完整 JSON 对象 |
| 创建配置 | POST | `/sys-config` | 接收 JSON，内部扁平化存储 |
| 更新配置 | PUT | `/sys-config/{scCode}` | 接收完整 JSON，覆盖更新（乐观锁校验） |
| 删除配置 | DELETE | `/sys-config/{scCode}` | 删除该 scCode 下所有行 |

### 4.2 批量查询

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 按模块查询 | GET | `/sys-config/module/{module}` | 返回该模块下所有配置对象列表 |
| 按编码列表查询 | POST | `/sys-config/batch` | Body: `["code1", "code2"]`，返回多个配置对象 |

### 4.3 配置项管理（细粒度操作）

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 获取单个配置项 | GET | `/sys-config/{scCode}/path/{scPath}` | 返回单行数据 |
| 更新单个配置项 | PATCH | `/sys-config/{scCode}/path/{scPath}` | 仅更新指定路径的值 |
| 删除单个配置项 | DELETE | `/sys-config/{scCode}/path/{scPath}` | 删除指定路径的行 |

### 4.4 全量刷新

| 接口 | 方法 | 路径 | 描述 |
|------|------|------|------|
| 全量更新配置 | POST | `/sys-config/{scCode}/refresh` | 接收完整 JSON，先删除旧数据再插入新数据 |

---

## 5. 扁平化与组装逻辑

### 5.1 扁平化规则

| 原始 JSON 值 | 扁平化后的 sc_type | sc_value 示例 |
|-------------|-------------------|---------------|
| `"设备A"` | STRING | `设备A` |
| `100` | INTEGER | `100` |
| `100.5` | FLOAT 或 DOUBLE | `100.5` |
| `true` | BOOLEAN | `true` |
| `"2026-01-01 00:00:00"` | TIMESTAMP | `2026-01-01 00:00:00` |
| 对象 `{...}` | 递归扁平化，对象本身不存储 | — |
| 数组 `[...]` | 递归扁平化，数组本身不存储 | — |

### 5.2 路径生成规则

- 对象属性：`parent.child`
- 数组元素：`parent.[index]`
- 根级属性：直接使用属性名，如 `name`

### 5.3 扁平化示例

**输入 JSON：**
```json
{
  "name": "温度配置",
  "range": [100, 500],
  "devices": [
    {"id": 1, "label": "设备A"}
  ]
}
```

**扁平化结果：**

| sc_path | sc_type | sc_value |
|---------|---------|----------|
| name | STRING | 温度配置 |
| range.[0] | INTEGER | 100 |
| range.[1] | INTEGER | 500 |
| devices.[0].id | INTEGER | 1 |
| devices.[0].label | STRING | 设备A |

### 5.4 组装逻辑

1. 按 `sc_path` 排序（保证数组顺序正确）
2. 根据 `sc_type` 解析 `sc_value` 为对应类型
3. 递归构建 JSON 对象结构

---

## 6. 缓存设计

### 6.1 缓存 Key 设计

| Key 模式 | 存储内容 | 用途 |
|----------|----------|------|
| `sys:config:{scCode}` | 组装后的 JSON 对象 | 单配置缓存 |
| `sys:config:module:{module}` | 该模块所有配置的 Map | 模块配置列表缓存 |

### 6.2 缓存一致性策略

采用 **Cache-Aside + 写时失效** 策略：

**读流程：**
1. 查 Redis 缓存
2. 缓存命中 → 直接返回
3. 缓存未命中 → 查 DB → 写缓存 → 返回

**写流程：**
1. 更新 DB（事务内）
2. 事务提交后删除相关缓存 Key：
   - `sys:config:{scCode}`
   - `sys:config:module:{module}`
3. 下次读取时自动重建缓存

### 6.3 关键点

- 写操作后**删除缓存**而非更新缓存，避免并发写入导致脏数据
- 删除缓存在事务提交后执行
- 乐观锁失败时不删除缓存（数据未变更）

---

## 7. 错误处理

### 7.1 异常处理

| 场景 | HTTP 状态码 | 错误信息 |
|------|-------------|----------|
| 配置不存在（sc_code） | 404 | 配置不存在 |
| 路径不存在（sc_path） | 404 | 配置项不存在 |
| 乐观锁冲突 | 409 | 配置已被修改，请刷新后重试 |
| 类型转换失败 | 400 | 值类型不匹配 |
| JSON 解析失败 | 400 | JSON 格式错误 |
| sc_code 已存在 | 409 | 配置编码已存在 |
| 必填字段缺失 | 400 | 参数校验失败 |

### 7.2 边界情况处理

| 场景 | 处理方式 |
|------|----------|
| 空数组 `[]` | 扁平化后无对应行，组装时还原为空数组 |
| 空对象 `{}` | 扁平化后无对应行，组装时还原为空对象 |
| null 值 | 存储为空字符串 `""`，sc_type 为 STRING，组装时根据上下文还原为 null |
| 大数组 | 扁平化后产生多行，使用批量插入优化性能 |

### 7.3 字段校验规则

| 字段 | 校验规则 |
|------|----------|
| sc_code | 必填，全局唯一，建议字母/数字/下划线组合 |
| sc_path | 必填，同一 sc_code 下唯一 |
| sc_name | 必填，最大 500 字符 |
| sc_type | 必填，枚举值校验 |
| sc_value | 必填，根据 sc_type 进行类型校验 |

---

## 8. 权限与通知

### 8.1 权限控制

沿用现有的菜单权限机制，拥有系统设置菜单权限即可管理所有配置，无需额外的细粒度权限控制。

### 8.2 变更通知

无需通知机制，配置更新后其他模块下次读取时自动获取新值。

---

## 9. 文件结构

```
src/main/java/com/siact/module/system/
├── controller/
│   └── SysConfigController.java          # REST API 控制器
├── service/
│   ├── SysConfigService.java             # 业务服务接口
│   └── impl/
│       └── SysConfigServiceImpl.java     # 业务服务实现
├── entity/
│   └── SysConfigEntity.java              # 数据实体
├── mapper/
│   └── SysConfigMapper.java              # MyBatis Mapper
├── enums/
│   ├── SysConfigModuleEnum.java          # 模块枚举
│   └── SysConfigTypeEnum.java            # 类型枚举
├── processor/
│   ├── ConfigFlattener.java              # 扁平化处理器
│   └── ConfigAssembler.java              # 组装处理器
├── dto/
│   ├── SysConfigDTO.java                 # 配置对象传输
│   └── SysConfigItemDTO.java             # 单项配置传输
└── command/
│   ├── SysConfigCreateCommand.java       # 创建命令
│   └── SysConfigUpdateCommand.java       # 更新命令
```

---

## 10. 约束与限制

1. 配置对象深度建议不超过 5 层，避免路径过长
2. 单个配置的数组元素建议不超过 1000 个
3. sc_code 建议使用有意义的命名，如 `temperature_range`、`device_step_config`
4. 大数值类型使用 DECIMAL，避免精度丢失