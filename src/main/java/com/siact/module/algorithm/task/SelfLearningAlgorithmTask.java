package com.siact.module.algorithm.task;

import com.siact.module.algorithm.services.IntelligentDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SelfLearningAlgorithmTask {

    private final IntelligentDataService intelligentDataService;

    @EventListener(ApplicationReadyEvent.class)
    public void runOnceOnStartup() {
        log.info("===== 自学习算法模型调用开始 =====");
        try {
            intelligentDataService.callSelfLearningAlgorithm();
        } catch (Exception e) {
            log.error("自学习算法模型调用失败", e);
        }
        log.info("===== 自学习算法模型调用结束 =====");
    }
}