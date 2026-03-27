# 定时任务线程隔离设计

## 背景

`AlgorithmTask` 中的两个定时任务 `algorithmInference()` 和 `getIntelligentComputing()` 使用 Spring `@Scheduled` 默认单线程调度器。当 `algorithmInference` 调用外部算法服务阻塞时，`getIntelligentComputing` 也会被阻塞。

## 目标

将两个独立定时任务隔离到各自线程池，互不影响。

## 方案

### 线程池配置

在 `ThreadPoolConfig.java` 新增两个专用线程池：

| Bean 名称 | 核心线程数 | 最大线程数 | 用途 |
|-----------|-----------|-----------|------|
| `algorithmPredictionExecutor` | 2 | 4 | 算法预测任务 |
| `intelligentComputingExecutor` | 2 | 4 | 智能计算任务 |

### 任务类拆分

**AlgorithmPredictionTask.java**
- `algorithmInference()` — 算法预测
- `deleteAlgorithmCallInfoBeforeTime()` — 清理历史数据

**IntelligentComputingTask.java**
- `getIntelligentComputing()` — 智能计算

### 调度实现

每个任务使用 `@Scheduled` 触发 `@Async` 方法：

```java
@Scheduled(fixedDelayString = "...")
public void trigger() {
    execute();
}

@Async("algorithmPredictionExecutor")
public void execute() {
    // 业务逻辑
}
```

## 文件变更

| 操作 | 文件路径 |
|------|----------|
| 修改 | `src/main/java/com/siact/core/common/config/ThreadPoolConfig.java` |
| 新建 | `src/main/java/com/siact/module/algorithm/task/AlgorithmPredictionTask.java` |
| 新建 | `src/main/java/com/siact/module/algorithm/task/IntelligentComputingTask.java` |
| 删除 | `src/main/java/com/siact/module/algorithm/task/AlgorithmTask.java` |

## 风险

- 无。改动范围小，不影响业务逻辑。