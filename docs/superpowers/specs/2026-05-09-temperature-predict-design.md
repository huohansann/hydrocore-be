# 温度预测数据保存设计文档

**日期**: 2026-05-09
**状态**: 待实现

## 背景

`IntelligentDataServiceImpl.callIntelligentInterface()` 调用智能控制算法后，响应中包含温度预测数据（`result.temps`），需要解析并保存到新表 `temperature_predict`。

## 需求

1. 新建 `temperature_predict` 表
2. 新建 Entity、Mapper、Repository（接口+实现）
3. 在 `callIntelligentInterface()` 中解析 `result.temps`，批量保存预测数据

## 数据映射

### 输入数据源

**controlTargetPoints（cptData）** — key 为点位名称如 "TE202"，value 包含 `code`、`ekl`、`name`（新增）：
```json
{
  "TE202": { "code": "PGY...MPWD22001", "ekl": "1.5", "name": "熔窑碹顶温度2" }
}
```

**算法响应 result.temps** — key 为 prop_name（如 "熔窑碹顶温度2"），value 包含 `pred_value`：
```json
{
  "temps": {
    "熔窑碹顶温度2": { "pred_value": 1418.824 },
    "熔窑碹顶温度6": { "pred_value": 1437.824 }
  }
}
```

### 字段映射

| 表字段 | 来源 |
|--------|------|
| id | 雪花算法（ASSIGN_ID） |
| point_name | cptData 的 key（如 "TE202"） |
| prop_name | cptData 每项的 `name` 字段（如 "熔窑碹顶温度2"） |
| prop_code | cptData 每项的 `code` 字段 |
| time | 响应时间变量 `time` |
| item_value | `result.temps[prop_name].pred_value` |
| create_time | 自动填充 |

### 匹配逻辑

遍历 `cptData` 的每个 entry：
1. 取 `name` 字段
2. 在 `result.temps` 中查找 `name` 对应的 JSONObject
3. 取其 `pred_value` 作为 `item_value`

## 变更范围

### 新建文件

1. **TemperaturePredictEntity** — `com.siact.module.algorithm.entity`
   - 遵循 `IncrementalLearnEntity` 模式
   - `@TableName("temperature_predict")`, `@TableId(type = IdType.ASSIGN_ID)`
   - `@Data @Builder @AllArgsConstructor @NoArgsConstructor`

2. **TemperaturePredictMapper** — `com.siact.module.algorithm.mapper`
   - `@Mapper extends BaseMapper<TemperaturePredictEntity>`
   - 空接口

3. **TemperaturePredictRepository** — `com.siact.module.algorithm.repository`（接口）
   - 遵循 `BaseRepository` 模式
   - 提供 `saveBatch` 方法

4. **TemperaturePredictRepositoryImpl** — `com.siact.module.algorithm.repository.impl`
   - `@Repository implements TemperaturePredictRepository`
   - 注入 `TemperaturePredictMapper`

5. **建表 SQL** — 添加到 `db/schema.sql`

### 修改文件

6. **IntelligentDataServiceImpl.java** — 删除 TODO 注释块（第 143-190 行），新增预测数据解析和保存逻辑
   - 注入 `TemperaturePredictRepository`
   - 解析 `result.temps`，遍历 cptData 匹配并构建实体列表
   - 调用 repository 批量保存

## 不变的部分

- 原有的 EK 参数构建逻辑
- 原有的 expertDeltaC / lastGasSum 提取和保存逻辑
- Redis flag 逻辑
