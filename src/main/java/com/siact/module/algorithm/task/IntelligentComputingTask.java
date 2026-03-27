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