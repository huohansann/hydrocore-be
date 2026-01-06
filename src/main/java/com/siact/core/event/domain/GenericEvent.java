package com.siact.core.event.domain;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-05 14:44
 * @className : GenericEvent
 * @description : 通用事件
 */
@Getter
@SuperBuilder
public class GenericEvent extends BaseEvent {
    private final String eventType;
    private final Object data;

    public static GenericEvent of(String eventType, Object data) {
        return GenericEvent.builder().eventType(eventType).data(data).build();
    }
}
