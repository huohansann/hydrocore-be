package com.siact.core.event.handler;

import com.siact.core.event.domain.DomainEvent;
import com.siact.core.event.exception.EventHandleException;

import java.util.Collections;
import java.util.List;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-05 14:30
 * @className : EventHandler
 * @description : 事件处理器接口
 */
public interface EventHandler<T extends DomainEvent> {
    /**
     * 判断是否支持处理此事件
     */
    boolean supports(String eventType);

    /**
     * 处理事件
     */
    void handle(T event) throws EventHandleException;

    /**
     * 执行优先级(数值越小优先级越高)
     */
    default int getOrder() {
        return 0;
    }

    /**
     * 是否异步执行
     */
    default boolean isAsync() {
        return true;
    }

    /**
     * 是否在事务提交后执行
     */
    default boolean runAfterCommit() {
        return false;
    }

    /**
     * 获取支持的多个事件类型
     */
    default List<String> getSupportedEvents() {
        return Collections.emptyList();
    }
}
