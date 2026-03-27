# 定时任务线程隔离实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将两个独立定时任务隔离到各自线程池，解决阻塞问题。

**Architecture:** 在 ThreadPoolConfig 新增两个专用线程池，拆分 AlgorithmTask 为两个独立任务类，每个任务使用 @Scheduled 触发 @Async 方法执行。

**Tech Stack:** Spring Boot, Spring @Scheduled, Spring @Async, ThreadPoolTaskExecutor

---

## 文件结构

| 文件 | 操作 | 职责 |
|------|------|------|
| `ThreadPoolConfig.java` | 修改 | 新增两个专用线程池 |
| `AlgorithmPredictionTask.java` | 新建 | 算法预测任务 |
| `IntelligentComputingTask.java` | 新建 | 智能计算任务 |
| `AlgorithmTask.java` | 删除 | 已拆分，不再需要 |

---

### Task 1: 新增专用线程池

**Files:**
- Modify: `src/main/java/com/siact/core/common/config/ThreadPoolConfig.java`

- [ ] **Step 1: 在 ThreadPoolConfig 中添加两个线程池 Bean**

在 `threadCpuPoolTaskExecutor()` 方法后添加：

```java
/**
 * 算法预测任务专用线程池
 */
@Bean(name = "algorithmPredictionExecutor")
public Executor algorithmPredictionExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(4);
    executor.setQueueCapacity(10);
    executor.setKeepAliveSeconds(60);
    executor.setThreadNamePrefix("Algorithm-Prediction-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.initialize();
    return executor;
}

/**
 * 智能计算任务专用线程池
 */
@Bean(name = "intelligentComputingExecutor")
public Executor intelligentComputingExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(2);
    executor.setMaxPoolSize(4);
    executor.setQueueCapacity(10);
    executor.setKeepAliveSeconds(60);
    executor.setThreadNamePrefix("Intelligent-Computing-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.initialize();
    return executor;
}
```

- [ ] **Step 2: 提交**

```bash
git add src/main/java/com/siact/core/common/config/ThreadPoolConfig.java
git commit -m "feat: 新增算法预测和智能计算专用线程池"
```

---

### Task 2: 创建 AlgorithmPredictionTask

**Files:**
- Create: `src/main/java/com/siact/module/algorithm/task/AlgorithmPredictionTask.java`

- [ ] **Step 1: 创建 AlgorithmPredictionTask 类**

```java
package com.siact.module.algorithm.task;

import com.siact.module.predicted.service.AlgorithmPredictedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 算法预测定时任务
 * 使用独立线程池执行，避免与其他定时任务互相阻塞
 */
@Slf4j
@RequiredArgsConstructor
@Component
@RefreshScope
public class AlgorithmPredictionTask {

    @Value("${algorithm.prediction.enable:false}")
    private Boolean algorithmPredictionEnable;

    private final AlgorithmPredictedService algorithmPredictedService;

    /**
     * 每分钟调用一次算法 获取预测数据
     */
    @Scheduled(fixedDelayString = "#{${spring.kiln.algorithm.predicted-interval} * 60 * 1000}")
    public void triggerAlgorithmInference() {
        executeAlgorithmInference();
    }

    @Async("algorithmPredictionExecutor")
    public void executeAlgorithmInference() {
        if (!algorithmPredictionEnable) {
            log.info("算法预测配置未开启");
            return;
        }
        algorithmPredictedService.algorithmInference();
    }

    /**
     * 每月1号定时删除之前的调用记录
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    public void triggerDeleteAlgorithmCallInfo() {
        executeDeleteAlgorithmCallInfo();
    }

    @Async("algorithmPredictionExecutor")
    public void executeDeleteAlgorithmCallInfo() {
        algorithmPredictedService.deleteAlgorithmCallInfoBeforeTime("");
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add src/main/java/com/siact/module/algorithm/task/AlgorithmPredictionTask.java
git commit -m "feat: 新建 AlgorithmPredictionTask，使用独立线程池"
```

---

### Task 3: 创建 IntelligentComputingTask

**Files:**
- Create: `src/main/java/com/siact/module/algorithm/task/IntelligentComputingTask.java`

- [ ] **Step 1: 创建 IntelligentComputingTask 类**

```java
package com.siact.module.algorithm.task;

import com.siact.module.algorithm.services.IntelligentDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 智能计算定时任务
 * 使用独立线程池执行，避免与其他定时任务互相阻塞
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class IntelligentComputingTask {

    private final IntelligentDataService intelligentDataService;

    /**
     * 每 N 分钟调用一次算法 获取智能计算值
     */
    @Scheduled(fixedRateString = "#{${spring.kiln.algorithm.intelligent-interval} * 60 * 1000}")
    public void triggerGetIntelligentComputing() {
        executeGetIntelligentComputing();
    }

    @Async("intelligentComputingExecutor")
    public void executeGetIntelligentComputing() {
        intelligentDataService.callIntelligentInterface();
    }
}
```

- [ ] **Step 2: 提交**

```bash
git add src/main/java/com/siact/module/algorithm/task/IntelligentComputingTask.java
git commit -m "feat: 新建 IntelligentComputingTask，使用独立线程池"
```

---

### Task 4: 删除旧的 AlgorithmTask

**Files:**
- Delete: `src/main/java/com/siact/module/algorithm/task/AlgorithmTask.java`

- [ ] **Step 1: 删除 AlgorithmTask.java**

```bash
git rm src/main/java/com/siact/module/algorithm/task/AlgorithmTask.java
```

- [ ] **Step 2: 提交**

```bash
git commit -m "refactor: 删除已拆分的 AlgorithmTask"
```

---

### Task 5: 验证编译

- [ ] **Step 1: 编译验证**

```bash
cd /path/to/project && mvn compile -q
```

Expected: BUILD SUCCESS

- [ ] **Step 2: 最终提交（如有遗漏）**

如果编译通过，任务完成。