# 自学习算法参数简化设计

## 背景

数据查询职责从 Java 端移到 Python 算法端。Java 只负责读取点位配置并传递给 Python，不再查询 TDengine 或生成数据文件。

## 变更前

```
Java: 读 sysconfig → 计算 timeRange → 分段查 TDengine → 写 JSONL 文件 → SSH 调用 Python（传文件路径）
```

## 变更后

```
Java: 读 sysconfig → 构建 name→dataCode 映射 → SSH 调用 Python（传 data JSON）
```

## SysConfig 配置格式 (SELF_LEARNING_DATACODE)

配置 data 字段为纯数组，每项包含 dataCode 和 name：

```json
[
  { "dataCode": "PGY02037_SYL01001_..._MPW132001", "name": "TE213" },
  { "dataCode": "PGY02037_SYL01001_..._MPWD62001", "name": "TE206" }
]
```

## Python 调用参数

通过 sys.argv 传递单个 `data` 参数，值为 JSON 字符串，name 作 key、dataCode 作 value：

```bash
python self_learning.py data='{"TE213":"PGY02037_...","TE206":"PGY02037_..."}'
```

Python 端通过 `json.loads(sys.argv[1].split('=', 1)[1])` 解析。

## callSelfLearningAlgorithm() 新流程

1. `sysConfigService.getByCode(SELF_LEARNING_DATACODE)` 获取配置
2. 解析为 `List<Map>`，遍历构建 `Map<String, String>`（name → dataCode）
3. 将映射序列化为 JSON 字符串，放入 params 的 `data` 键
4. 调用 `pythonAlgorithmService.execute("self_learning.py", params, Map.class)`

## 删除的内容

- TDengine 分段查询逻辑（taosDataService.queryRawData 调用）
- JSONL 文件写入逻辑
- 时间计算（segmentDays、dtf、outFmt 等）
- TaosDataService 依赖注入（如果该类无其他使用）
- KilnProperty.Ssh 中 dataFileDir / remoteDataFileDir 字段（如果无其他使用）

## 文件变更清单

| 操作 | 文件 |
|---|---|
| 修改 | `IntelligentDataServiceImpl.java`（简化 callSelfLearningAlgorithm） |