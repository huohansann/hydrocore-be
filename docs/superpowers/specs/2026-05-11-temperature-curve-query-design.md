# 温度实际值+预测值曲线查询接口设计文档

**日期**: 2026-05-11
**状态**: 待实现

## 背景

需要新增一个查询接口，同时返回温度实际值曲线和温度预测值曲线数据。实际值从 TDengine（TaosService）查询，预测值从新建的 `temperature_predict` 表查询。参考 `queryTemperature` 接口的模式。

## 需求

1. 新增 `POST /forecast/queryActualAndForecast` 接口
2. 返回 `TempForecastVO`（不使用 `R` 类包装，由 `ResponseBodyAdvice` 统一管理）
3. 包含：实际值曲线、预测值曲线、上下控制限/告警限/温度设定值

## 变更范围

### 1. 新建 TempActualForecastQuery

**文件**: `com.siact.module.forecast.query.TempActualForecastQuery`

字段：
- `dataCodes: List<String>` (@NotEmpty) — 点位编码
- `startTime: String` (@NotBlank) — 开始时间
- `endTime: String` (@NotBlank) — 结束时间
- `ts: Integer` (@NotNull) — 步长
- `tsUnit: String` (@StringContains Y/M/D/H/MIN) — 步长单位
- `formatVal: String` — 时间格式
- `calcType: String` (@StringContains AVG/MAX/MIN/LAST 等) — 聚合方式
- `names: List<String>` — 显示名称

### 2. 扩展 TemperaturePredictRepository

新增方法 `List<TemperaturePredictEntity> queryByPropCodesAndTimeRange(List<String> propCodes, String startTime, String endTime)`

在 Repository 接口和 Impl 中实现，使用 MyBatis-Plus `LambdaQueryWrapper` 按 `propCode` IN + `time` BETWEEN 查询。

### 3. ForecastKilnService 新增方法

方法签名：`TempForecastVO queryActualAndForecast(TempActualForecastQuery query)`

逻辑流程：
1. 查询实际值：使用 `TaosDataService.queryIntervalVal` + `ForecastSupport.buildForecastValueMap`
2. 查询预测值：使用 `TemperaturePredictRepository.queryByPropCodesAndTimeRange`，按 `propCode` 分组转换为 `Map<String, List<Object[]>>`
3. 查询控制限配置：`ControlIntervalConfigService.queryHistoryConfigChart`
4. 查询显示配置：`TplService.getListByCode("kilnPredictionDataShow")`
5. 生成时间轴：`IntervalTimeUtil.getIntervalTimeList`
6. 组装 `TempForecastVO`（参考 `queryTemperature` 的组装逻辑）

返回数据 map 中包含的 key：
- `dcs` — 实际运行值
- `predict` — 温度预测值
- `upControl` / `lowControl` — 上下控制限
- `upAlarm` / `lowAlarm` — 上下告警限
- `temperatureSet` — 温度设定值

### 4. ForecastKilnController 新增端点

```java
@PostMapping("/queryActualAndForecast")
@ApiOperation("查询温度实际值与预测值曲线")
@ApiOperationSupport(order = 60)
public TempForecastVO queryActualAndForecast(@RequestBody @Validated TempActualForecastQuery query) {
    return forecastKilnService.queryActualAndForecast(query);
}
```

### 5. 新增 Repository 注入

`ForecastKilnServiceImpl` 注入 `TemperaturePredictRepository`。

## 不变的部分

- `queryTemperature` 等现有接口
- `TempForecastVO` / `TempForecastInfoVO` / `TempForecastInfoValueVO` 结构
- `TemperaturePredictEntity` 结构