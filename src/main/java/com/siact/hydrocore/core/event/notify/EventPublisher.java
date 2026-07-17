package com.siact.hydrocore.core.event.notify;

import com.siact.hydrocore.core.event.domain.DomainEvent;

import java.util.concurrent.CompletableFuture;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-05 14:42
 * @className : EventPublisher
 * @description : 事件发布器接口
 */
public interface EventPublisher {
    /**
     * 发布事件
     */
    void publish(DomainEvent event);

    /**
     * 发布事件并等待结果
     */
    void publishSync(DomainEvent event);

    /**
     * 发布事件（异步）
     */
    CompletableFuture<Void> publishAsync(DomainEvent event);

    /**
     * 发布Spring事件（兼容原有机制）
     */
    void publishSpringEvent(Object event);
}
