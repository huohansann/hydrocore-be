package com.siact.hydrocore.core.common.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

import static org.assertj.core.api.Assertions.assertThat;

class ThreadPoolConfigTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(ThreadPoolConfig.class);

    @Test
    void exposesSingleDefaultAsyncExecutor() {
        contextRunner.run(context -> {
            assertThat(context).hasBean("asyncTaskExecutor");
            assertThat(context).doesNotHaveBean("threadIoPoolTaskExecutor");
            assertThat(context).doesNotHaveBean("threadCpuPoolTaskExecutor");
            assertThat(context).doesNotHaveBean("backgroundTaskExecutor");

            Executor executor = context.getBean("asyncTaskExecutor", Executor.class);
            assertThat(executor).isInstanceOf(ThreadPoolTaskExecutor.class);

            ThreadPoolTaskExecutor taskExecutor = (ThreadPoolTaskExecutor) executor;
            assertThat(taskExecutor.getCorePoolSize()).isEqualTo(8);
            assertThat(taskExecutor.getMaxPoolSize()).isEqualTo(32);
            assertThat(taskExecutor.getThreadPoolExecutor().getQueue().remainingCapacity()).isEqualTo(1000);
            assertThat(taskExecutor.getThreadNamePrefix()).isEqualTo("hydro-async-");
            assertThat(taskExecutor.getThreadPoolExecutor().getKeepAliveTime(java.util.concurrent.TimeUnit.SECONDS)).isEqualTo(60);
        });
    }
}
