# 液位控制模块设计规格

## 概述

为液位控制页面提供后端数据支持，包括实时数据查询、控制配置管理、算法结果存储和预测曲线展示。前端模块位于 `kic-xyg/src/views/control/level/`，当前使用 mock 数据，需对接后端接口。

## 设计决策

- **数据关联**：配置和结果按点位 dataCode 关联，不关联窑炉
- **点位编码**：从 sysconfig 模块动态读取，不硬编码
- **算法数据获取**：后端主动调用算法接口，结果保存到数据库
- **架构方案**：新建 `module/level-control` 独立模块，复用 `TaosDataService`、`BaseEntity` 等现有基础设施
- **响应格式**：Controller 直接返回数据对象，由 `ResponseBodyAdvice` 自动包装为 `{ code, message, data }`

## 数据库设计

### 表 1：level_control_config（液位控制配置表）

每个 dataCode 一条记录，覆盖更新。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT AUTO | 主键 |
| data_code | VARCHAR(64) | 点位编码 |
| mode | VARCHAR(16) | 控制模式：ai / pid / manual |
| ai_predict_window | DECIMAL(10,2) | AI预测窗口 |
| ai_predict_duration | DECIMAL(10,2) | AI预测时长 |
| pid_pb | DECIMAL(10,2) | PID比例带 PB |
| pid_ti | DECIMAL(10,2) | PID积分时间 TI |
| pid_td | DECIMAL(10,2) | PID微分时间 TD |
| manual_control_value | DECIMAL(10,2) | 人工控制值 |
| safe_limit | DECIMAL(10,2) | 安全限制 |
| opening_upper_limit | DECIMAL(10,2) | 开度上限 |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |
| create_by | VARCHAR(64) | 创建人 |
| update_by | VARCHAR(64) | 更新人 |
| deleted | TINYINT(1) | 逻辑删除 |

### 表 2：level_algorithm_result（液位算法结果表）

每个 dataCode 一条记录，算法调用后覆盖更新。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT AUTO | 主键 |
| data_code | VARCHAR(64) | 点位编码 |
| level_trend | DECIMAL(10,4) | 液位趋势值 |
| recommended_opening | DECIMAL(10,4) | 推荐开度 |
| level_status | VARCHAR(32) | 液位状态（normal/warning/alarm） |
| create_time | DATETIME | 创建时间 |
| update_time | DATETIME | 更新时间 |

### 表 3：level_predicted_data（液位预测数据表，预留）

建表但暂不对接算法，接口预留。

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT AUTO | 主键 |
| data_code | VARCHAR(64) | 点位编码 |
| predicted_time | VARCHAR(32) | 预测时间点 |
| predicted_value | DECIMAL(10,4) | 预测值 |
| predicted_type | INT | 预测类型（预留） |
| unit | VARCHAR(16) | 单位 |
| create_time | DATETIME | 创建时间 |

## 接口设计

### 接口总览

Controller 拆分为两个：`LevelControlController`（写操作）和 `LevelDataController`（读操作）。

| 方法 | 路径 | 说明 | 返回类型 |
|------|------|------|----------|
| GET | `/level-control/config/{dataCode}` | 获取控制配置 | `LevelControlConfigVO` |
| PUT | `/level-control/config` | 保存/更新控制配置 | `void` |
| PUT | `/level-control/mode` | 切换控制模式 | `void` |
| GET | `/level-control/realtime/{dataCode}` | 获取实时数据（液位+开度） | `LevelRealtimeVO` |
| GET | `/level-control/algorithm-result/{dataCode}` | 获取算法结果 | `LevelAlgorithmResultVO` |
| POST | `/level-control/predict-curve` | 液位预测曲线查询 | `LevelPredictCurveVO` |

### 接口详细格式

#### 1. 获取实时数据 GET `/level-control/realtime/{dataCode}`

通过 `TaosDataService.queryRealValue()` 查询液位和投料机开度的实时值。点位编码从 sysconfig 读取。

返回：
```json
{
  "level": { "value": 0.015, "unit": "mm" },
  "opening": { "value": 23.68, "unit": "%" }
}
```

#### 2. 获取控制配置 GET `/level-control/config/{dataCode}`

从 MySQL `level_control_config` 表查询。

返回：`LevelControlConfigVO`，包含 mode、AI参数、PID参数、人工控制值、安全限制、开度上限。

#### 3. 保存控制配置 PUT `/level-control/config`

请求体：
```json
{
  "dataCode": "YWMC",
  "mode": "ai",
  "aiPredictWindow": 20,
  "aiPredictDuration": 60,
  "pidPb": 300,
  "pidTi": 300,
  "pidTd": 1.5,
  "manualControlValue": 23.12,
  "safeLimit": 100,
  "openingUpperLimit": 80
}
```

按 dataCode 查询，存在则更新，不存在则新增。

#### 4. 切换控制模式 PUT `/level-control/mode`

请求体：
```json
{
  "dataCode": "YWMC",
  "mode": "pid"
}
```

仅更新 mode 字段。

#### 5. 获取算法结果 GET `/level-control/algorithm-result/{dataCode}`

从 MySQL `level_algorithm_result` 表查询。

返回：
```json
{
  "levelTrend": 0.012,
  "recommendedOpening": 23.12,
  "levelStatus": "normal"
}
```

#### 6. 液位预测曲线查询 POST `/level-control/predict-curve`

参考温度预测 `TempForecastQuery` 格式，复用 `TaosDataService.queryIntervalVal()`。目前只返回实际值，预测值预留。

请求体：
```json
{
  "dataCode": "YWMC",
  "startTime": "2026-05-07 00:00:00",
  "endTime": "2026-05-07 23:59:59",
  "ts": 5,
  "tsUnit": "MIN",
  "calcType": "AVG"
}
```

返回：
```json
{
  "xdata": ["00:00", "00:05", "00:10"],
  "series": [
    {
      "dataCode": "YWMC",
      "name": "液位",
      "data": {
        "actual": { "name": "液位实际值", "value": [["00:00", "0.015"], ["00:05", "0.018"]] },
        "predicted": null
      }
    }
  ]
}
```

## 模块代码结构

```
com.siact.module.levelcontrol/
├── controller/
│   ├── LevelControlController.java        # 配置CRUD + 模式切换
│   └── LevelDataController.java           # 实时数据 + 算法结果 + 预测曲线
├── service/
│   ├── LevelControlConfigService.java
│   ├── LevelAlgorithmResultService.java
│   ├── LevelPredictService.java
│   └── impl/
│       ├── LevelControlConfigServiceImpl.java
│       ├── LevelAlgorithmResultServiceImpl.java
│       └── LevelPredictServiceImpl.java
├── repository/
│   ├── LevelControlConfigRepository.java
│   ├── LevelAlgorithmResultRepository.java
│   └── LevelPredictedDataRepository.java
├── entity/
│   ├── LevelControlConfigEntity.java
│   ├── LevelAlgorithmResultEntity.java
│   └── LevelPredictedDataEntity.java
├── mapper/
│   ├── LevelControlConfigMapper.java
│   ├── LevelAlgorithmResultMapper.java
│   └── LevelPredictedDataMapper.java
├── dto/
│   ├── LevelControlConfigDTO.java
│   └── LevelModeSwitchDTO.java
├── vo/
│   ├── LevelControlConfigVO.java
│   ├── LevelRealtimeVO.java
│   ├── LevelAlgorithmResultVO.java
│   ├── LevelPredictCurveVO.java
│   └── LevelPredictCurveSeriesVO.java
├── query/
│   └── LevelPredictCurveQuery.java
└── enums/
    └── LevelControlModeEnum.java
```

## 跨模块复用

| 复用项 | 来源模块 |
|--------|----------|
| `TaosDataService` | `com.siact.tdengine` |
| `BaseEntity` | `com.siact.common.entity` |
| `ResponseBodyAdvice` 自动包装 | `com.siact.core.web.advice` |
| 点位编码配置读取 | `sysconfig` 模块 |

## 范围边界

- 本次实现：数据库建表、后端接口开发、TDengine 实时数据和预测曲线查询
- 预留但不实现：算法接口调用、预测数据写入和查询（`level_predicted_data` 表建好但接口预留）
- 不涉及：前端页面改造、算法服务对接
