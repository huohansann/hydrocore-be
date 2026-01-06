package com.siact.core.event.domain;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-05 14:26
 * @className : DomainEvent
 * @description : 领域事件接口
 */
public interface DomainEvent {
    /**
     * 事件唯一标识
     */
    default String getEventId() {
        return UUID.randomUUID().toString();
    }

    /**
     * 事件类型
     */
    String getEventType();

    /**
     * 事件数据
     */
    Object getData();

    /**
     * 事件发生时间
     */
    default LocalDateTime getOccurredAt() {
        return LocalDateTime.now();
    }

    /**
     * 事件元数据
     */
    Map<String, Object> getMetadata();

    /**
     * 添加元数据
     */
    default void addMetadata(String key, Object value) {
        getMetadata().put(key, value);
    }

    /**
     * 获取元数据
     */
    default Object getMetadata(String key) {
        return getMetadata().get(key);
    }
}
