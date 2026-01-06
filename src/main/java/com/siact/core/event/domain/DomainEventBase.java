package com.siact.core.event.domain;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-05 14:45
 * @className : DomainEventBase
 * @description : 领域事件基础
 */
@Getter
@SuperBuilder
public abstract class DomainEventBase extends BaseEvent {
    private final String aggregateId;
    private final String aggregateType;
    private final int version;

    @Override
    public abstract String getEventType();

    @Override
    public abstract Object getData();
}
