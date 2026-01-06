package com.siact.core.event.interceptor;

import com.siact.core.event.domain.DomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-05 14:59
 * @className : LoggingEventInterceptor
 * @description : 日志拦截器
 */
@Slf4j
@Component
public class LoggingEventInterceptor implements EventInterceptor {
    @Override
    public void beforeHandle(DomainEvent event) {
        log.info("Start processing events: {}, type: {}", event.getEventId(), event.getEventType());
    }

    @Override
    public void afterHandle(DomainEvent event) {
        log.info("Event processing complete: {}", event.getEventId());
    }

    @Override
    public void onError(DomainEvent event, Throwable throwable) {
        log.error("Event processing failed: {}, exception: {}", event.getEventId(), throwable.getMessage(), throwable);
    }
}
