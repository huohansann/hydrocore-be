package com.siact.hydrocore.core.common.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "hydrocore.thread-pools")
public class RuntimeThreadPoolProperties {
    private Pool io = new Pool(16, 32, 1024, 60, "hydro-io-");
    private Pool cpu = new Pool(
            Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors() * 2,
            1024,
            60,
            "hydro-cpu-");
    private Pool background = new Pool(2, 4, 10, 60, "hydro-background-");
    private Pool event = new Pool(10, 50, 1000, 60, "event-handler-");

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pool {
        private int coreSize;
        private int maxSize;
        private int queueCapacity;
        private int keepAliveSeconds;
        private String threadNamePrefix;
        private boolean allowCoreThreadTimeout;
        private boolean waitForTasksToCompleteOnShutdown = true;
        private int awaitTerminationSeconds = 60;
        private String rejectionPolicy = "caller-runs";

        public Pool(int coreSize, int maxSize, int queueCapacity, int keepAliveSeconds, String threadNamePrefix) {
            this.coreSize = coreSize;
            this.maxSize = maxSize;
            this.queueCapacity = queueCapacity;
            this.keepAliveSeconds = keepAliveSeconds;
            this.threadNamePrefix = threadNamePrefix;
        }
    }
}
