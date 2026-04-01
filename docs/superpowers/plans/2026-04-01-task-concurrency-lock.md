# 定时任务并发重叠防护实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为定时任务添加 Redis 分布式锁，防止 docker restart 后并发重叠导致任务卡死

**Architecture:** 在 RedisService 中新增 tryLock/unlock 方法，移除定时任务的 @Async 注解，在调度方法中加锁保护

**Tech Stack:** Spring @Scheduled, Redis SETNX, RedisTemplate

---

## 文件结构

| 文件 | 改动类型 | 说明 |
|-----|---------|-----|
| `src/main/java/com/siact/common/redis/RedisService.java` | 修改 | 新增 tryLock/unlock 方法 |
| `src/main/java/com/siact/module/algorithm/task/IntelligentComputingTask.java` | 修改 | 移除 @Async，加锁逻辑 |
| `src/main/java/com/siact/module/algorithm/task/AlgorithmPredictionTask.java` | 修改 | 移除 @Async，加锁逻辑 |

---

### Task 1: RedisService 新增分布式锁方法

**Files:**
- Modify: `src/main/java/com/siact/common/redis/RedisService.java`

- [ ] **Step 1: 添加 import 语句**

在文件顶部添加必要的 import：

```java
// 已有 import，无需新增（java.util.concurrent.TimeUnit 已存在）
```

- [ ] **Step 2: 添加 tryLock 方法**

在类的末尾（第335行之前，`keys` 方法之后）添加：

```java
    /**
     * 尝试获取分布式锁（SETNX）
     *
     * @param key 锁的key
     * @param value 锁的值（用于安全释放，建议使用 UUID）
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
     *
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

- [ ] **Step 3: 验证代码编译**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/com/siact/common/redis/RedisService.java
git commit -m "feat: RedisService 新增 tryLock/unlock 分布式锁方法"
```

---

### Task 2: IntelligentComputingTask 添加锁保护

**Files:**
- Modify: `src/main/java/com/siact/module/algorithm/task/IntelligentComputingTask.java`

- [ ] **Step 1: 添加 import 和依赖**

在文件顶部添加 import：

```java
import com.siact.common.redis.RedisService;
import java.util.UUID;
```

在类中添加 RedisService 依赖和常量：

```java
    private static final String LOCK_KEY = "lock:intelligent_computing";
    private static final long LOCK_TIMEOUT = 300; // 5分钟

    private final IntelligentDataService intelligentDataService;
    private final RedisService redis;  // 新增
```

- [ ] **Step 2: 重写 triggerGetIntelligentComputing 方法**

将原有方法替换为：

```java
    /**
     * 每 N 分钟调用一次算法 获取智能计算值
     */
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
```

- [ ] **Step 3: 删除 executeGetIntelligentComputing 方法**

删除以下方法：

```java
    @Async("intelligentComputingExecutor")
    public void executeGetIntelligentComputing() {
        intelligentDataService.callIntelligentInterface();
    }
```

- [ ] **Step 4: 验证代码编译**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/com/siact/module/algorithm/task/IntelligentComputingTask.java
git commit -m "fix: IntelligentComputingTask 添加 Redis 分布式锁防止并发重叠"
```

---

### Task 3: AlgorithmPredictionTask 添加锁保护

**Files:**
- Modify: `src/main/java/com/siact/module/algorithm/task/AlgorithmPredictionTask.java`

- [ ] **Step 1: 添加 import 和依赖**

在文件顶部添加 import：

```java
import com.siact.common.redis.RedisService;
import java.util.UUID;
```

在类中添加 RedisService 依赖和常量：

```java
    private static final String LOCK_KEY = "lock:algorithm_prediction";
    private static final String DELETE_LOCK_KEY = "lock:algorithm_prediction_delete";
    private static final long LOCK_TIMEOUT = 300; // 5分钟

    @Value("${algorithm.prediction.enable:false}")
    private Boolean algorithmPredictionEnable;

    private final AlgorithmPredictedService algorithmPredictedService;
    private final RedisService redis;  // 新增
```

- [ ] **Step 2: 重写 triggerAlgorithmInference 方法**

将原有方法替换为：

```java
    /**
     * 每分钟调用一次算法 获取预测数据
     */
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
```

- [ ] **Step 3: 重写 triggerDeleteAlgorithmCallInfo 方法**

将原有方法替换为：

```java
    /**
     * 每月1号定时删除之前的调用记录
     */
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
```

- [ ] **Step 4: 删除原有 execute 方法**

删除以下两个方法：

```java
    @Async("algorithmPredictionExecutor")
    public void executeAlgorithmInference() {
        if (!algorithmPredictionEnable) {
            log.info("算法预测配置未开启");
            return;
        }
        algorithmPredictedService.algorithmInference();
    }

    @Async("algorithmPredictionExecutor")
    public void executeDeleteAlgorithmCallInfo() {
        algorithmPredictedService.deleteAlgorithmCallInfoBeforeTime("");
    }
```

- [ ] **Step 5: 验证代码编译**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/siact/module/algorithm/task/AlgorithmPredictionTask.java
git commit -m "fix: AlgorithmPredictionTask 添加 Redis 分布式锁防止并发重叠"
```

---

### Task 4: 最终验证

- [ ] **Step 1: 完整编译测试**

Run: `mvn compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 2: 检查 git 状态**

Run: `git status`
Expected: 工作区干净，所有改动已提交

- [ ] **Step 3: 查看提交历史确认**

Run: `git log --oneline -5`
Expected: 显示 3 个新提交（RedisService、IntelligentComputingTask、AlgorithmPredictionTask）

---

## Self-Review 检查清单

**1. Spec Coverage:**
- ✅ RedisService 新增 tryLock/unlock → Task 1
- ✅ IntelligentComputingTask 加锁 → Task 2
- ✅ AlgorithmPredictionTask 加锁 → Task 3
- ✅ 移除 @Async → Task 2, Task 3
- ✅ 锁超时 300 秒 → Task 2, Task 3

**2. Placeholder Scan:**
- ✅ 无 TBD/TODO
- ✅ 所有代码步骤都有完整代码块
- ✅ 所有命令都有具体内容

**3. Type Consistency:**
- ✅ tryLock(key: String, value: String, timeout: long) → unlock(key: String, value: String)
- ✅ LOCK_KEY 使用 String 类型
- ✅ lockValue 使用 UUID.randomUUID().toString()