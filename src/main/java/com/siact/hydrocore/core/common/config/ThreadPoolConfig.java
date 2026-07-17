package com.siact.hydrocore.core.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;

/**
 * 默认情况下，在创建了线程池后，线程池中的线程数为0，当有任务来之后，就会创建一个线程去执行任务，
 * 当线程池中的线程数目达到corePoolSize后，就会把到达的任务放到缓存队列当中；
 * 当队列满了，就继续创建线程，当线程数量大于等于maxPoolSize后，开始使用拒绝策略拒绝
 */
@Configuration
@EnableAsync
@RequiredArgsConstructor
@EnableConfigurationProperties(RuntimeThreadPoolProperties.class)
public class ThreadPoolConfig {
    private final RuntimeThreadPoolProperties properties;
    private final ThreadPoolTaskExecutorBuilder builder;

    public static final int BEYOND_TIME = 5;

    /**
     * IO 密集类型线程池 （corePoolSize 核心线程 和 maxPoolSize最大线程数比cpu核数翻4倍）
     */
    @Bean(name = "threadIoPoolTaskExecutor")
    public Executor threadIoPoolTaskExecutor() {
        return builder.build(properties.getIo());
    }

    /**
     * cpu 密集类型线程池
     *
     */
    @Bean(name = "threadCpuPoolTaskExecutor")
    public Executor threadCpuPoolTaskExecutor() {
        return builder.build(properties.getCpu());
    }
    /**
     * 通用后台任务线程池
     */
    @Bean(name = "backgroundTaskExecutor")
    public Executor backgroundTaskExecutor() {
        return builder.build(properties.getBackground());
    }
}
