package com.siact.hydrocore.core.event.registry;

import com.siact.hydrocore.core.event.handler.EventHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-05 14:47
 * @className : EventHandlerAutoRegister
 * @description : 事件处理自动注册
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class EventHandlerAutoRegister implements ApplicationContextAware {
    private final EventHandlerRegistry registry;
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @SuppressWarnings("rawtypes")
    public @PostConstruct void autoRegister() {
        Map<String, EventHandler> beans = applicationContext.getBeansOfType(EventHandler.class);
        beans.values().forEach(handler -> {
            registry.registerHandler(handler);
            log.info("Registered event handler: {} for events: {}", handler.getClass().getSimpleName(), getSupportedEvents(handler));
        });
    }

    private String getSupportedEvents(EventHandler<?> handler) {
        if (handler instanceof EventHandlerRegistry.SupportsMultipleEvents) {
            return String.join(", ", ((EventHandlerRegistry.SupportsMultipleEvents) handler).getSupportedEvents());
        }
        return "dynamic";
    }
}
