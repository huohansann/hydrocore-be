package com.siact.hydrocore.core.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Default async executor reserved for future asynchronous work.
 */
@Configuration
@EnableAsync
public class ThreadPoolConfig implements AsyncConfigurer {
    private static final int ASYNC_CORE_POOL_SIZE = 8;
    private static final int ASYNC_MAX_POOL_SIZE = 32;
    private static final int ASYNC_QUEUE_CAPACITY = 1000;
    private static final int ASYNC_KEEP_ALIVE_SECONDS = 60;
    private static final String ASYNC_THREAD_NAME_PREFIX = "hydro-async-";
    private static final boolean ASYNC_WAIT_FOR_TASKS_ON_SHUTDOWN = true;
    private static final int ASYNC_AWAIT_TERMINATION_SECONDS = 60;

    @Override
    public Executor getAsyncExecutor() {
        return asyncTaskExecutor();
    }

    @Bean(name = "asyncTaskExecutor")
    public ThreadPoolTaskExecutor asyncTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(ASYNC_CORE_POOL_SIZE);
        executor.setMaxPoolSize(ASYNC_MAX_POOL_SIZE);
        executor.setQueueCapacity(ASYNC_QUEUE_CAPACITY);
        executor.setKeepAliveSeconds(ASYNC_KEEP_ALIVE_SECONDS);
        executor.setThreadNamePrefix(ASYNC_THREAD_NAME_PREFIX);
        executor.setWaitForTasksToCompleteOnShutdown(ASYNC_WAIT_FOR_TASKS_ON_SHUTDOWN);
        executor.setAwaitTerminationSeconds(ASYNC_AWAIT_TERMINATION_SECONDS);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
}
