package com.siact.hydrocore.core.event.config;

import com.siact.hydrocore.core.common.config.RuntimeThreadPoolProperties;
import com.siact.hydrocore.core.common.config.ThreadPoolTaskExecutorBuilder;
import com.siact.hydrocore.core.event.interceptor.CompositeEventInterceptor;
import com.siact.hydrocore.core.event.interceptor.EventInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.List;
import java.util.concurrent.Executor;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-05 15:03
 * @className : EventConfiguration
 * @description : 事件配置
 */
@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties({EventProperties.class, RuntimeThreadPoolProperties.class})
@EnableAsync
@Configuration
public class EventConfiguration {
    private final EventProperties properties;
    private final RuntimeThreadPoolProperties runtimeThreadPoolProperties;
    private final ThreadPoolTaskExecutorBuilder builder;

    /**
     * 事件处理器线程池
     */
    public @Bean("eventTaskExecutor") Executor eventTaskExecutor() {
        if (!properties.isEnabled()) {
            log.warn("Event Framework is disabled and uses synchronous executors");
            return Runnable::run;
        }
        RuntimeThreadPoolProperties.Pool poolConfig = runtimeThreadPoolProperties.getEvent();
        properties.applyThreadPoolTo(poolConfig);
        log.info("The event thread pool is initialized: core={}, max={}, queue={}", poolConfig.getCoreSize(), poolConfig.getMaxSize(), poolConfig.getQueueCapacity());
        return builder.build(poolConfig);
    }

    /**
     * 组合拦截器
     */
    @Primary
    public @Bean EventInterceptor compositeEventInterceptor(List<EventInterceptor> interceptors) {
        return new CompositeEventInterceptor(interceptors);
    }

    /**
     * 配置验证器
     */
    public @Bean EventConfigValidator eventConfigValidator() {
        return new EventConfigValidator(properties);
    }
}
