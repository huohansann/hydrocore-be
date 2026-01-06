package com.siact.core.event.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PostConstruct;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-06 14:46
 * @className : EventConfigValidator
 * @description : 配置验证器
 */
@Slf4j
@RequiredArgsConstructor
public class EventConfigValidator {
    private final EventProperties eventProperties;

    public @PostConstruct void validateConfig() {
        log.info("Event Framework Configuration: enabled={}, thread pool size={}{}, queue capacity={}",
                eventProperties.isEnabled(),
                eventProperties.getThreadPool().getCoreSize(),
                eventProperties.getThreadPool().getMaxSize(),
                eventProperties.getThreadPool().getQueueCapacity()
        );
        if (eventProperties.isEnabled()) log.info("Event Framework is enabled");
        else log.warn("The event framework is disabled and the event will execute synchronously");
    }
}
