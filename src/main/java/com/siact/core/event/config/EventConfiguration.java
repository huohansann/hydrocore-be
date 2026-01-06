package com.siact.core.event.config;

import com.siact.core.event.interceptor.CompositeEventInterceptor;
import com.siact.core.event.interceptor.EventInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-05 15:03
 * @className : EventConfiguration
 * @description : 事件配置
 */
@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties(EventProperties.class)
@EnableAsync
@Configuration
public class EventConfiguration {
    private final EventProperties properties;

    /**
     * 事件处理器线程池
     */
    public @Bean("eventTaskExecutor") Executor eventTaskExecutor() {
        if (!properties.isEnabled()) {
            log.warn("Event Framework is disabled and uses synchronous executors");
            return Runnable::run;
        }
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        EventProperties.ThreadPool poolConfig = properties.getThreadPool();

        executor.setCorePoolSize(poolConfig.getCoreSize());
        executor.setMaxPoolSize(poolConfig.getMaxSize());
        executor.setQueueCapacity(poolConfig.getQueueCapacity());
        executor.setThreadNamePrefix(poolConfig.getThreadNamePrefix());
        executor.setKeepAliveSeconds(poolConfig.getKeepAliveSeconds());
        executor.setAllowCoreThreadTimeOut(poolConfig.isAllowCoreThreadTimeout());
        executor.setWaitForTasksToCompleteOnShutdown(poolConfig.isWaitForTasksToCompleteOnShutdown());
        executor.setAwaitTerminationSeconds(poolConfig.getAwaitTerminationSeconds());
        // 拒绝策略: 由调用线程处理
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        log.info("The event thread pool is initialized: core={}, max={}, queue={}", poolConfig.getCoreSize(), poolConfig.getMaxSize(), poolConfig.getQueueCapacity());
        return executor;
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
