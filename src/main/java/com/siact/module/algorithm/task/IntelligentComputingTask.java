package com.siact.module.algorithm.task;

import com.siact.common.redis.RedisService;
import com.siact.module.algorithm.services.IntelligentDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 智能计算定时任务
 * 使用 Redis 分布式锁防止并发重叠
 */
@Slf4j
@Component
public class IntelligentComputingTask {

    private static final String LOCK_KEY = "lock:intelligent_computing";
    private static final long LOCK_TIMEOUT = 300; // 5分钟

    private final IntelligentDataService intelligentDataService;
    private final RedisService redis;

    public IntelligentComputingTask(IntelligentDataService intelligentDataService, RedisService redis) {
        this.intelligentDataService = intelligentDataService;
        this.redis = redis;
    }

    /**
     * 每 N 分钟调用一次算法 获取智能计算值
     */
    @Scheduled(fixedDelayString = "#{${spring.kiln.algorithm.intelligent-interval} * 60 * 1000}")
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