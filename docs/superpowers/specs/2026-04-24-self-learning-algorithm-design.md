# 自学习算法调用功能设计

## 背景

自学习算法用于更新智控算法模型。流程：从 TDengine 查询历史时序数据，生成 JSON 数据文件，通过 SSH 调用远程 Python 自学习脚本进行模型训练。

## 数据流

```
1. 读取 SysConfig (SELF_LEARNING_DATACODE) → 获取 timeRange + points
2. 计算: endTime=当前时间, startTime=当前时间 - timeRange 天
3. 查询 TDengine 原始时序数据（所有 points code）
4. 按点位 name 分组，组装列式 JSON 结构
5. 写入容器映射目录的 JSON 文件
6. 将物理机路径作为参数，调用 PythonAlgorithmService 执行远程自学习脚本
```

## 配置扩展

### KilnProperty.Ssh 新增字段

```java
private String dataFileDir;        // 容器内路径（Java 写入），如 /app/shared/data
private String remoteDataFileDir;  // 物理机路径（Python 读取），如 /data/shared/data
```

### SysConfig 配置示例 (SELF_LEARNING_DATACODE)

```json
{
  "timeRange": 21,
  "points": [
    { "code": "dsfffffffffffffffffffffffffffffff", "name": "碹顶温度1" },
    { "code": "dsfffffffffffffffffffffffffffffff", "name": "碹顶温度2" }
  ]
}
```

## JSON 数据文件格式

列式结构，`time` 数组 + 每个点位的 `name` 对应一个值数组，按时间对齐：

```json
{
  "time": [
    "2026-4-21 6:40",
    "2026-4-21 6:41",
    "2026-4-21 6:42"
  ],
  "碹顶温度1": [1258.21, 1260.16, 1262.12],
  "碹顶温度2": [1124.5, 1111.97, 1106.89]
}
```

## TDengine 原始数据查询

现有 TaosDataService 只有聚合查询，需新增原始时序数据查询方法：

```java
List<Map<String, Object>> queryRawData(List<String> dataCodes, String startTime, String endTime);
```

底层 SQL：
```sql
SELECT ts, devproperty as datacode, itemvalue
FROM datasource
WHERE devproperty IN ('code1', 'code2')
  AND ts >= 'startTime' AND ts <= 'endTime'
ORDER BY ts
```

返回每行包含 `ts`（时间戳）、`datacode`（点位编码）、`itemvalue`（值）。

## IntelligentDataServiceImpl 新增方法

```java
public void callSelfLearningAlgorithm()
```

内部流程：
1. `sysConfigService.getByCode(SELF_LEARNING_DATACODE)` 获取配置
2. 解析 timeRange 和 points（code + name 映射）
3. 计算 startTime = now - timeRange 天，endTime = now
4. **分段查询**：若 timeRange > 5 天，按每 5 天一段拆分，逐段调用 `taosDataService.queryRawData(codes, segmentStart, segmentEnd)`，合并结果。避免单次查询数据量过大导致 OOM
5. 遍历结果，按时间戳分组，将 datacode 映射为 name，组装列式 JSON 结构
6. 用 JacksonUtils 将数据写入 `dataFileDir/self_learning_{timestamp}.json`
7. 构建物理机路径 `remoteDataFileDir/self_learning_{timestamp}.json` 作为参数
8. 调用 `pythonAlgorithmService.execute("self_learning.py", params, Map.class)`

## SelfLearningAlgorithmTask 更新

移除硬编码测试逻辑，改为调用 `intelligentDataService.callSelfLearningAlgorithm()`。

## 文件变更清单

| 操作 | 文件 |
|---|---|
| 修改 | `KilnProperty.java`（Ssh 新增 dataFileDir、remoteDataFileDir） |
| 修改 | `TaosDataService.java`（新增 queryRawData 接口方法） |
| 修改 | `TaosDataServiceImpl.java`（实现 queryRawData） |
| 修改 | `IntelligentDataService.java`（新增 callSelfLearningAlgorithm 接口方法） |
| 修改 | `IntelligentDataServiceImpl.java`（实现 callSelfLearningAlgorithm） |
| 修改 | `SelfLearningAlgorithmTask.java`（改为调用 intelligentDataService） |