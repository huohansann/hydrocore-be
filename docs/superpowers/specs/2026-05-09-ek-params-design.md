# 添加温度限制参数 EK 设计文档

**日期**: 2026-05-09
**状态**: 待实现

## 背景

`IntelligentDataServiceImpl.callIntelligentInterface()` 方法在调用智能控制算法时，需要新增 EK（温度限制）参数传递给算法接口。同时，`controlTargetPoints` 的数据格式已从简单的 `Map<String, String>` 更新为嵌套结构。

## 需求

1. 解析更新后的 `controlTargetPoints` 数据格式（`Map<String, Map>`），每项包含 `code`（dataCode）和 `ekl`（死区半径）
2. 构建 EK 参数结构，每个点位包含：
   - **EKL**: 死区半径，从 `controlTargetPoints` 每项的 `ekl` 字段读取
   - **EKH**: 波动限，对应 `ControlIntervalConfigDTO.lowControl`
   - **EKS**: 告警限，对应 `ControlIntervalConfigDTO.lowAlarm`
3. 通过 `measurePoint` 将 targetTemps 与 controlTargetPoints 的 key 匹配

## EK 参数结构

```json
{
  "TE202": {
    "EKL": 1.5,
    "EKH": 2,
    "EKS": 3
  },
  "TE206": {
    "EKL": 1.5,
    "EKH": 1.8,
    "EKS": 2.5
  }
}
```

## 变更范围

仅修改 `IntelligentDataServiceImpl.java`。

### 1. 更新 controlTargetPoints 解析（第 85-90 行）

- 泛型类型从 `(Map<String, String>)` 改为 `(Map<String, Map<String, Object>>)`
- 提取 dataCode 列表时遍历取每项的 `code` 字段
- `_SP` 参数逻辑保持不变

### 2. 新增 `buildEKParams` 辅助方法

签名：`private Map<String, Map<String, BigDecimal>> buildEKParams(Map<String, Map<String, Object>> cptData, List<ControlIntervalConfigDTO> targetTemps)`

逻辑：
1. 将 targetTemps 按 measurePoint 建立 Map 索引
2. 遍历 cptData 的每个 key
3. 取 ekl 值，通过 key 匹配 targetTemps 获取 lowControl（EKH）和 lowAlarm（EKS）
4. 组装为 `{EKL, EKH, EKS}` 子 Map 并返回

### 3. 主方法调用

将 `params.put("EK", "数据")` 替换为 `params.put("EK", buildEKParams(cptData, targetTemps))`。

## 不变的部分

- 原有的 `_SP` 温度设定参数逻辑
- Redis flag 逻辑
- 算法调用和响应处理逻辑