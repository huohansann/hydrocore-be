package com.siact.core.event.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-05 14:43
 * @className : BaseEvent
 * @description : 抽象基础事件
 */
@Getter
@SuperBuilder
public abstract class BaseEvent implements DomainEvent {
    @Builder.Default
    private String eventId = UUID.randomUUID().toString();

    @Builder.Default
    private LocalDateTime occurredAt = LocalDateTime.now();

    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    @Override
    public abstract String getEventType();

    @Override
    public abstract Object getData();

    @Override
    public Map<String, Object> getMetadata() {
        return new HashMap<>();
    }
}
