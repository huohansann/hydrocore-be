package com.siact.core.event.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.transaction.annotation.Propagation;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-06 14:21
 * @className : EventProperties
 * @description : 事件配置属性
 */
@Data
@ConfigurationProperties(prefix = "spring.event")
public class EventProperties {
    /**
     * 线程池配置
     */
    private ThreadPool threadPool = new ThreadPool();

    /**
     * 事务配置
     */
    private Transaction transaction = new Transaction();

    /**
     * 监控配置
     */
    private Monitoring monitoring = new Monitoring();
    /**
     * 是否启用事件框架
     */
    private boolean enabled = true;

    /**
     * 事件处理超时时间(毫秒)
     */
    private long defaultTimeout = 30000L;

    /**
     * 是否启用事件存储
     */
    private boolean eventStoreEnabled = false;

    /**
     * 是否启用死信队列
     */
    private boolean deadLetterQueueEnabled = true;

    /**
     * 事件类型特定配置
     */
    private Map<String, EventTypeConfig> eventTypes = new HashMap<>();

    @Data
    public static class ThreadPool {
        private int coreSize = 10;
        private int maxSize = 50;
        private int queueCapacity = 1000;
        private String threadNamePrefix = "event-handler-";
        private int keepAliveSeconds = 60;
        private boolean allowCoreThreadTimeout = false;
        private boolean waitForTasksToCompleteOnShutdown = true;
        private int awaitTerminationSeconds = 60;
    }

    @Data
    public static class Transaction {
        private String defaultPropagation = "REQUIRES_NEW";
        private boolean defaultReadOnly = false;
        private int defaultTimeout = 30;
        private int defaultRetryAttempts = 0;
        private long defaultRetryBackoff = 1000L;
        private boolean defaultRunAfterCommit = false;

        public Propagation getPropagationEnum() {
            try {
                return Propagation.valueOf(defaultPropagation);
            } catch (IllegalArgumentException e) {
                return Propagation.REQUIRES_NEW;
            }
        }
    }

    @Data
    public static class Monitoring {
        private boolean enabled = true;
        private String metricsPrefix = "event";
        private boolean logSlowEvents = true;
        private long slowEventThreshold = 5000L; // 5秒
    }

    @Data
    public static class EventTypeConfig {
        private String propagation;
        private boolean async = true;
        private int retryAttempts;
        private long retryBackoff;
        private boolean runAfterCommit;
        private int timeout;
    }

    public @PostConstruct void validate() {
        if (threadPool.coreSize <= 0) throw new IllegalArgumentException("event.thread-pool.core-size must be greater than 0");
        if (threadPool.maxSize < threadPool.coreSize) throw new IllegalArgumentException("event.thread-pool.max-size must be greater than or equal to core-size");
    }

    /**
     * 获取事件类型的特定配置
     */
    public EventTypeConfig getEventTypeConfig(String eventType) {
        return eventTypes.get(eventType);
    }

    /**
     * 是否为异步处理的事件类型
     */
    public boolean isAsyncEventType(String eventType) {
        EventTypeConfig config = getEventTypeConfig(eventType);
        return config == null || config.isAsync();
    }
}
