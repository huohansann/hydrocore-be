package com.siact.hydrocore.core.event.registry;

import com.siact.hydrocore.core.event.handler.EventHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-05 14:46
 * @className : EventHandlerRegistry
 * @description : 事件处理器注册中心
 */
@Slf4j
@Component
public class EventHandlerRegistry {
    private final Map<String, List<EventHandler<?>>> handlers = new ConcurrentHashMap<>();
    private final List<EventHandler<?>> allHandlers = new CopyOnWriteArrayList<>();

    /**
     * 注册事件处理器
     */
    public void registerHandler(EventHandler<?> handler) {
        allHandlers.add(handler);
        // 根据处理器支持的 eventType 分类存储
        if (handler instanceof SupportsMultipleEvents) {
            SupportsMultipleEvents multiHandler = (SupportsMultipleEvents) handler;
            multiHandler.getSupportedEvents().forEach(eventType -> {
                handlers.computeIfAbsent(eventType, k -> new ArrayList<>()).add(handler);
            });
        } else {
            // 需要处理器实现 supports 方法来确定支持的 eventType
            log.warn("Handler {} should implement supports() or SupportsMultipleEvents", handler.getClass().getSimpleName());
        }
    }

    /**
     * 获取事件对应的所有处理器
     */
    public List<EventHandler<?>> getHandlers(String eventType) {
        List<EventHandler<?>> matchedHandlers = handlers.getOrDefault(eventType, new ArrayList<>());

        // 如果没有明确注册，则动态匹配
        if (matchedHandlers.isEmpty()) {
            matchedHandlers = allHandlers.stream().filter(handler -> handler.supports(eventType)).collect(Collectors.toList());
        }

        // 按优先级排序
        matchedHandlers.sort(Comparator.comparingInt(EventHandler::getOrder));
        return matchedHandlers;
    }

    /**
     * 获取所有处理器
     */
    public List<EventHandler<?>> getAllHandlers() {
        return new ArrayList<>(allHandlers);
    }

    /**
     * 支持多事件类型的处理器接口
     */
    public interface SupportsMultipleEvents {
        List<String> getSupportedEvents();
    }
}
