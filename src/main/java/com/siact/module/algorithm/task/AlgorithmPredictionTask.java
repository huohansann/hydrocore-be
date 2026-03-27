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