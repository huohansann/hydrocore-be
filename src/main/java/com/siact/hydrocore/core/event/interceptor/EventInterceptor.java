package com.siact.hydrocore.core.event.interceptor;

import com.siact.hydrocore.core.event.domain.DomainEvent;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-05 14:58
 * @className : EventInterceptor
 * @description : 事件拦截器
 */
public interface EventInterceptor {
    /**
     * 处理前调用
     */
    default void beforeHandle(DomainEvent event) {
    }

    /**
     * 处理后调用
     */
    default void afterHandle(DomainEvent event) {
    }

    /**
     * 异常时调用
     */
    default void onError(DomainEvent event, Throwable throwable) {
    }

    /**
     * 最终调用(类似 finally)
     */
    default void afterCompletion(DomainEvent event) {
    }
}
