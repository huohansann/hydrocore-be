package com.siact.hydrocore.core.event.exception;

import lombok.Getter;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-05 14:32
 * @className : EventHandleException
 * @description : 事件处理异常
 */
@Getter
public class EventHandleException extends RuntimeException {
    private final String eventId;
    private final String eventType;

    public EventHandleException(String message) {
        super(message);
        this.eventId = null;
        this.eventType = null;
    }

    public EventHandleException(String message, Throwable cause) {
        super(message, cause);
        this.eventId = null;
        this.eventType = null;
    }

    public EventHandleException(String message, String eventId, String eventType) {
        super(message);
        this.eventId = eventId;
        this.eventType = eventType;
    }

    public EventHandleException(String message, Throwable cause, String eventId, String eventType) {
        super(message, cause);
        this.eventId = eventId;
        this.eventType = eventType;
    }
}
