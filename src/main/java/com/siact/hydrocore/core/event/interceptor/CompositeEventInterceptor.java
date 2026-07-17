package com.siact.hydrocore.core.event.interceptor;

import com.siact.hydrocore.core.event.domain.DomainEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-05 15:07
 * @className : CompositeEventInterceptor
 * @description : 组合拦截器
 */
@Slf4j
public class CompositeEventInterceptor implements EventInterceptor {
    private final List<EventInterceptor> interceptors;

    public CompositeEventInterceptor(List<EventInterceptor> interceptors) {
        this.interceptors = interceptors;
    }

    @Override
    public void beforeHandle(DomainEvent event) {
        interceptors.forEach(interceptor -> safeExecute(() -> interceptor.beforeHandle(event), interceptor, "beforeHandle"));
    }

    @Override
    public void afterHandle(DomainEvent event) {
        interceptors.forEach(interceptor -> safeExecute(() -> interceptor.afterHandle(event), interceptor, "afterHandle"));
    }

    @Override
    public void onError(DomainEvent event, Throwable throwable) {
        interceptors.forEach(interceptor -> safeExecute(() -> interceptor.onError(event, throwable), interceptor, "onError"));
    }

    @Override
    public void afterCompletion(DomainEvent event) {
        interceptors.forEach(interceptor -> safeExecute(() -> interceptor.afterCompletion(event), interceptor, "afterCompletion"));
    }

    private void safeExecute(Runnable task, EventInterceptor interceptor, String method) {
        try {
            task.run();
        } catch (Exception e) {
            log.error("The {} method of the event blocker {} fails to execute", interceptor.getClass().getSimpleName(), method, e);
        }
    }
}
