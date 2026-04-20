# 设备实时数据查询与导出设计

## 背景

在 `device` 模块新增 TDengine 历史数据查询和导出功能。用户通过界面选择点位ID、设备名称等条件，查询对应的时序数据并支持导出。

## 接口定义

| Method | Path | 说明 |
|--------|------|------|
| GET | `/device/realtime/itemIds` | 点位ID下拉选项 |
| GET | `/device/realtime/deviceNames` | 设备名称下拉选项 |
| POST | `/device/realtime/query` | 分页查询 TDengine 数据 |
| GET | `/device/realtime/export` | 导出数据（Excel/CSV/JSON） |

## 请求参数

### DeviceRealtimeQuery（查询/导出共用）

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| itemIds | List\<String\> | 否 | 点位ID列表（多选） |
| propName | String | 否 | 属性名称（模糊搜索） |
| deviceCodes | List\<String\> | 否 | 设备编码列表（前端传 deviceName 的 value 即 deviceCode） |
| startTime | String | 是 | 查询开始时间 yyyy-MM-dd HH:mm:ss |
| endTime | String | 是 | 查询结束时间 |
| tsUnit | String | 是 | 查询模式：`raw`（原始）/ `m`（分钟）/ `h`（小时）/ `d`（天） |
| calcType | String | 条件必填 | 聚合类型（AVG/MAX/MIN/LAST），raw 模式忽略，聚合模式下必填 |
| page | int | 查询时必填 | 页码 |
| pageSize | int | 查询时必填 | 每页条数 |
| format | String | 导出时必填 | 导出格式：excel/csv/json |

### 下拉接口返回

```
GET /device/realtime/itemIds
→ [{ "label": "itemId1", "value": "itemId1" }, ...]

GET /device/realtime/deviceNames
→ [{ "label": "设备名称A", "value": "设备编码A" }, ...]
```

## 数据流

```
前端查询条件（itemIds / propName / deviceCodes / startTime / endTime / tsUnit / calcType）
  ↓
① 根据 itemIds / propName / deviceCodes 查 device_mapping 表
  → 得到匹配记录列表，提取 propCode 列表
  ↓
② 根据 propCode 列表 + startTime + endTime + tsUnit + calcType
  → 构建 TDengine SQL 查询 datasource 表（使用 devproperty TAG 过滤）
  ↓
③ 查询结果中每条记录的 datacode (devproperty) 回查 device_mapping
  → 补充 itemId / propName / deviceName / deviceCode 信息
  ↓
④ 返回分页数据（查询）或全量数据（导出）
```

## 响应字段

每条记录包含：

| 字段 | 说明 |
|------|------|
| itemId | 点位ID |
| propName | 属性名称 |
| deviceCode | 设备编码 |
| deviceName | 设备名称 |
| ts | 时间戳 |
| itemValue | 数值 |

## 查询模式

| tsUnit | 查询方式 | 说明 |
|--------|---------|------|
| `raw` | 原始时序数据 | 不聚合，返回每个时间点的值 |
| `m` | 按分钟聚合 | INTERVAL 聚合 |
| `h` | 按小时聚合 | INTERVAL 聚合 |
| `d` | 按天聚合 | INTERVAL 聚合 |

聚合模式下 `calcType` 必填，支持 AVG/MAX/MIN/LAST。

## 导出分级策略

导出前先执行 `COUNT` 查询获取总条数，根据数据量选择策略：

| 数据量 | 策略 | 实现方式 |
|--------|------|---------|
| ≤ 5万条 | 全量内存写入 | 一次查完，直接写 response |
| 5万~50万条 | 分批查询 + 流式写入 | 每批1万条，逐批查逐批写，不落盘 |
| > 50万条 | 分批查询 + 临时文件 + 流式写入 | 每批1万条写入临时文件，完成后流式输出并删除临时文件 |

- 临时文件写入 `java.io.tmpdir`，文件名包含时间戳防冲突
- 导出完成后 `finally` 块中删除临时文件
- Excel 使用 SXSSFWorkbook（流式写入，避免 POI 内存溢出）
- CSV 逐行写入
- JSON 使用流式 JSON 写入

## 新增文件

```
device/controller/DeviceRealtimeController.java   — 控制器
device/service/DeviceRealtimeService.java          — 服务接口
device/service/impl/DeviceRealtimeServiceImpl.java — 服务实现
device/query/DeviceRealtimeQuery.java              — 查询参数
device/vo/DeviceRealtimeVO.java                    — 返回 VO
device/vo/SelectOptionVO.java                      — 通用下拉选项 VO（label/value）
```

## 复用

- `DeviceMappingRepository` — 查询 device_mapping 表
- `TaosJdbcClient` — 执行 TDengine 查询
- `ExcelUtils` — 导出工具（EasyPoi）

## 技术决策

- device 模块直接注入 `TaosJdbcClient` 构建 SQL，不经过 `TaosDataService`，原因：
  - 查询使用 `devproperty` TAG 按 propCode 过滤，但需要额外关联 device_mapping 补充字段
  - 需要返回所有字段（现有 TaosDataService 只返回 ts + itemvalue）
  - 需要支持原始数据查询（现有只支持聚合/间隔查询）