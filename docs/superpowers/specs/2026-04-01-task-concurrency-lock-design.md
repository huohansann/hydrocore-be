---
name: 定时任务并发重叠防护
description: 为 IntelligentComputingTask 和 AlgorithmPredictionTask 添加 Redis 分布式锁，防止 docker restart 后任务并发重叠导致锁死
type: project
---

# 定时任务并发重叠防护设计

## 问题背景

当前定时任务使用 `@Scheduled` + `@Async` 异步执行模式，存在以下问题：

1. **并发重叠**：`docker restart` 重启容器时，多个任务实例可能同时启动
2. **行锁争抢**：并发任务操作同一批数据，争抢数据库行锁
3. **线程池阻塞**：线程池队列满后 `CallerRunsPolicy` 让调度线程也阻塞，导致整个调度卡死

## 解决方案

使用 Redis 分布式锁，确保同一时刻只有一个任务实例执行。

## 改动清单

### 1. RedisService 新增锁方法

**文件**：`src/main/java/com/siact/common/redis/RedisService.java`

```java
/**
 * 尝试获取分布式锁（SETNX）
 * @param key 锁的key
 * @param value 锁的值（用于安全释放）
 * @param timeout 锁超时时间（秒）
 * @return true=获取成功；false=锁已被占用
 */
public boolean tryLock(String key, String value, long timeout) {
    return Boolean.TRUE.equals(
        redisTemplate.opsForValue().setIfAbsent(key, value, timeout, TimeUnit.SECONDS)
    );
}

/**
 * 释放分布式锁（安全释放，只释放自己持有的锁）
 * @param key 锁的key
 * @param value 锁的值
 */
public void unlock(String key, String value) {
    Object current = redisTemplate.opsForValue().get(key);
    if (current != null && value.equals(String.valueOf(current))) {
        redisTemplate.delete(key);
    }
}
```

### 2. IntelligentComputingTask 改造

**文件**：`src/main/java/com/siact/module/algorithm/task/IntelligentComputingTask.java`

**改动**：
- 移除 `@Async("intelligentComputingExecutor")` 和 `executeGetIntelligentComputing()` 方法
- 在 `triggerGetIntelligentComputing()` 中添加锁逻辑
- 新增 `RedisService` 依赖和 `LOCK_KEY` 常量

**改造后代码**：
```java
@Slf4j
@RequiredArgsConstructor
@Component
public class IntelligentComputingTask {

    private static final String LOCK_KEY = "lock:intelligent_computing";
    private static final long LOCK_TIMEOUT = 300; // 5分钟

    private final IntelligentDataService intelligentDataService;
    private final RedisService redis;

    @Scheduled(fixedRateString = "#{${spring.kiln.algorithm.intelligent-interval} * 60 * 1000}")
    public void triggerGetIntelligentComputing() {
        String lockValue = UUID.randomUUID().toString();
        if (!redis.tryLock(LOCK_KEY, lockValue, LOCK_TIMEOUT)) {
            log.info("智能计算任务正在执行，跳过本次触发");
            return;
        }
        try {
            intelligentDataService.callIntelligentInterface();
        } finally {
            redis.unlock(LOCK_KEY, lockValue);
        }
    }
}
```

### 3. AlgorithmPredictionTask 改造

**文件**：`src/main/java/com/siact/module/algorithm/task/AlgorithmPredictionTask.java`

**改动**：
- 移除 `@Async("algorithmPredictionExecutor")` 和 `executeAlgorithmInference()` / `executeDeleteAlgorithmCallInfo()` 方法
- 在 `triggerAlgorithmInference()` 和 `triggerDeleteAlgorithmCallInfo()` 中添加锁逻辑
- 新增 `RedisService` 依赖和两个 `LOCK_KEY` 常量

**改造后代码**：
```java
@Slf4j
@RequiredArgsConstructor
@Component
@RefreshScope
public class AlgorithmPredictionTask {

    private static final String LOCK_KEY = "lock:algorithm_prediction";
    private static final String DELETE_LOCK_KEY = "lock:algorithm_prediction_delete";
    private static final long LOCK_TIMEOUT = 300; // 5分钟

    @Value("${algorithm.prediction.enable:false}")
    private Boolean algorithmPredictionEnable;

    private final AlgorithmPredictedService algorithmPredictedService;
    private final RedisService redis;

    @Scheduled(fixedDelayString = "#{${spring.kiln.algorithm.predicted-interval} * 60 * 1000}")
    public void triggerAlgorithmInference() {
        String lockValue = UUID.randomUUID().toString();
        if (!redis.tryLock(LOCK_KEY, lockValue, LOCK_TIMEOUT)) {
            log.info("算法预测任务正在执行，跳过本次触发");
            return;
        }
        try {
            if (!algorithmPredictionEnable) {
                log.info("算法预测配置未开启");
                return;
            }
            algorithmPredictedService.algorithmInference();
        } finally {
            redis.unlock(LOCK_KEY, lockValue);
        }
    }

    @Scheduled(cron = "0 0 0 1 * ?")
    public void triggerDeleteAlgorithmCallInfo() {
        String lockValue = UUID.randomUUID().toString();
        if (!redis.tryLock(DELETE_LOCK_KEY, lockValue, LOCK_TIMEOUT)) {
            log.info("删除算法调用记录任务正在执行，跳过本次触发");
            return;
        }
        try {
            algorithmPredictedService.deleteAlgorithmCallInfoBeforeTime("");
        } finally {
            redis.unlock(DELETE_LOCK_KEY, lockValue);
        }
    }
}
```

## 锁配置说明

| 任务 | 锁 Key | 超时时间 | 说明 |
|-----|--------|---------|-----|
| 智能计算 | `lock:intelligent_computing` | 300秒 | 正常执行很快，超时作为安全兜底 |
| 算法预测 | `lock:algorithm_prediction` | 300秒 | 同上 |
| 删除调用记录 | `lock:algorithm_prediction_delete` | 300秒 | 每月执行一次，加锁保护 |

## 注意事项

1. **移除 @Async**：锁在主线程获取，异步执行会导致锁在任务完成前就释放，失去保护作用
2. **锁超时兜底**：防止异常导致锁未释放，超时后自动解锁
3. **安全释放**：`unlock()` 只释放自己持有的锁（通过 UUID 值匹配），避免误释放其他实例的锁
4. **日志记录**：获取锁失败时记录日志，便于排查

## Why

用户反馈 `docker restart` 后定时任务卡死，原因是多个任务实例并发重叠争抢数据库行锁，导致线程池阻塞。

## How to apply

修改完成后，重启应用测试：多次快速重启容器，观察日志是否有"跳过本次触发"记录，确认无卡死现象。