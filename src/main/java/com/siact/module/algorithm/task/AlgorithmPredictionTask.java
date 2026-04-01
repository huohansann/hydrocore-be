package com.siact.module.algorithm.task;

import com.siact.common.redis.RedisService;
import com.siact.module.predicted.service.AlgorithmPredictedService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 算法预测定时任务
 * 使用 Redis 分布式锁防止并发重叠
 */
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
}