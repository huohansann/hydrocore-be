package com.siact.hydrocore.core.event.notify;

import com.siact.hydrocore.core.event.annotation.EventTransactional;
import com.siact.hydrocore.core.event.config.EventProperties;
import com.siact.hydrocore.core.event.domain.DomainEvent;
import com.siact.hydrocore.core.event.exception.EventHandleException;
import com.siact.hydrocore.core.event.handler.EventHandler;
import com.siact.hydrocore.core.event.interceptor.EventInterceptor;
import com.siact.hydrocore.core.event.registry.EventHandlerRegistry;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.interceptor.NoRollbackRuleAttribute;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-05 14:54
 * @className : AsyncEventPublisher
 * @description : 事件发布器实现
 */
@Slf4j
@Component
public class AsyncEventPublisher implements EventPublisher {
    private final ApplicationEventPublisher publisher;
    private final Executor executor;
    private final EventHandlerRegistry registry;
    private final @Nullable EventInterceptor interceptor;
    private final PlatformTransactionManager manager;
    private final EventProperties properties;

    public AsyncEventPublisher(
            ApplicationEventPublisher publisher,
            @Qualifier("eventTaskExecutor") Executor executor,
            EventHandlerRegistry registry,
            @Nullable EventInterceptor interceptor,
            PlatformTransactionManager manager,
            EventProperties properties
    ) {
        this.registry = registry;
        this.publisher = publisher;
        this.executor = executor;
        this.interceptor = interceptor;
        this.manager = manager;
        this.properties = properties;
    }

    /**
     * 发布Spring事件(兼容原有Spring事件机制)
     */
    public void publishSpringEvent(Object event) {
        publisher.publishEvent(event);
    }

    @Override
    public void publish(DomainEvent event) {
        if (!properties.isEnabled()) {
            // 如果事件框架被禁用, 同步执行
            publishSync(event);
            return;
        }
        // 检查事件类型配置
        EventProperties.EventTypeConfig eventTypeConfig = properties.getEventTypeConfig(event.getEventType());
        boolean isAsync = eventTypeConfig == null || eventTypeConfig.isAsync();

        if (isAsync) {
            publishAsync(event).exceptionally(throwable -> {
                log.error("Asynchronous publishing event failed: {}", event.getEventId(), throwable);
                return null;
            });
        } else publishSync(event);
    }

    @Override
    public void publishSync(DomainEvent event) {
        if (!properties.isEnabled()) {
            log.debug("Event Framework is disabled to synchronize events: {}", event.getEventType());
        }

        long startTime = System.currentTimeMillis();

        try {
            // 前置拦截
            if (interceptor != null) interceptor.beforeHandle(event);

            // 获取并执行处理器
            List<EventHandler<?>> handlers = registry.getHandlers(event.getEventType());
            if (CollectionUtils.isNotEmpty(handlers)) {
                // 只执行同步处理器
                handlers.stream().filter(handler -> !handler.isAsync()).forEach(handler -> executeHandler(event, handler));
            }
            // 后置拦截
            if (interceptor != null) interceptor.afterHandle(event);

            logEventDuration(event, startTime, true);
        } catch (Exception e) {
            logEventDuration(event, startTime, false);
            log.error("Synchronous publishing events failed: {}", event.getEventId(), e);
            if (interceptor != null) interceptor.onError(event, e);
            throw new EventHandleException("Synchronization event processing failed", e);
        }
    }

    @Override
    public CompletableFuture<Void> publishAsync(DomainEvent event) {
        return CompletableFuture.runAsync(() -> {
            long startTime = System.currentTimeMillis();
            try {
                // 前置拦截
                if (interceptor != null) interceptor.beforeHandle(event);

                // 获取事件处理器
                List<EventHandler<?>> handlers = registry.getHandlers(event.getEventType());

                if (CollectionUtils.isEmpty(handlers)) {
                    log.warn("No processor found for event {}", event.getEventType());
                    return;
                }

                // 执行处理器
                executeHandlers(event, handlers);

                // 后置拦截
                if (interceptor != null) interceptor.afterHandle(event);
            } catch (Exception e) {
                logEventDuration(event, startTime, false);
                log.error("Event processing failed: {}", event.getEventId(), e);
                if (interceptor != null) interceptor.onError(event, e);
                throw new EventHandleException("Event processing failed", e);
            }
        }, executor).whenComplete((result, throwable) -> {
            if (throwable != null) {
                log.error("Event handling exceptions: {}", event.getEventId(), throwable);
            } else {
                log.debug("Event processing is complete: {}", event.getEventId());
            }
        });
    }

    private void executeHandlers(DomainEvent event, List<EventHandler<?>> handlers) {
        // 分组：异步和同步处理器
        Map<Boolean, List<EventHandler<?>>> groupedHandlers = handlers.stream().collect(Collectors.groupingBy(EventHandler::isAsync));

        // 同步处理器立即执行
        List<EventHandler<?>> syncHandlers = groupedHandlers.getOrDefault(false, new ArrayList<>());
        syncHandlers.sort(Comparator.comparingInt(EventHandler::getOrder));
        syncHandlers.forEach(handler -> executeHandler(event, handler));

        // 异步处理器并行执行
        List<EventHandler<?>> asyncHandlers = groupedHandlers.getOrDefault(true, new ArrayList<>());
        asyncHandlers.sort(Comparator.comparingInt(EventHandler::getOrder));
        if (CollectionUtils.isNotEmpty(asyncHandlers)) {
            // 等待所有异步处理器完成
            List<CompletableFuture<Void>> futures = asyncHandlers.stream().map(handler -> CompletableFuture.runAsync(() -> executeHandler(event, handler), executor)).collect(Collectors.toList());

            try {
                CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
                // 设置超时
                allFutures.get(properties.getDefaultTimeout(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error("Event processing timeout: {}ms, event ID: {}", properties.getDefaultTimeout(), event.getEventId());
                throw new EventHandleException("Event processing timeout", e);
            } catch (Exception throwable) {
                log.error("Asynchronous processor execution failed", throwable);
                throw new EventHandleException("Asynchronous processor execution failed", throwable);
            }
        }
    }

    private void executeHandler(DomainEvent event, EventHandler<?> handler) {
        try {
            EventTransactional et = findEventTransactionalAnnotation(handler, event);

            // 检查是否需要在事务提交后执行
            if (shouldRunAfterCommit(et, handler)) {
                // 注册事务提交后执行的回调
                registerAfterCommitExecution(event, handler, et);
                return;
            }
            // 立即执行处理器
            executeHandlerWithTransaction(event, handler, et);
        } catch (Exception e) {
            log.error("The execution event handler failed: {}", handler.getClass().getSimpleName(), e);
            throw new EventHandleException("Processor execution failed: " + handler.getClass().getName(), e);
        }
    }

    /**
     * 注册事务提交后执行的回调
     */
    private void registerAfterCommitExecution(DomainEvent event, EventHandler<?> handler, EventTransactional tx) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                // 事务提交后异步执行
                CompletableFuture.runAsync(() -> {
                    try {
                        executeHandlerWithTransaction(event, handler, tx);
                    } catch (Exception e) {
                        log.error("The execution processor fails after the transaction is committed: {}", handler.getClass().getSimpleName(), e);
                        // 记录到死信队列或重试队列
                        handleFailedEvent(event, handler, e);
                    }
                }, executor);
            }

            @Override
            public void afterCompletion(int status) {
                if (status == TransactionSynchronization.STATUS_ROLLED_BACK) {
                    log.warn("Transaction rollback, event {} for processor {} will not be executed", event.getEventId(), handler.getClass().getSimpleName());
                }
            }
        });
        log.debug("Event processor {} is registered as a transaction commit and executes", handler.getClass().getSimpleName());
    }

    /**
     * 根据注解属性决定是否在事务中执行, 支持重试/回滚/超时
     */
    private void executeHandlerWithTransaction(DomainEvent event, EventHandler<?> handler, @Nullable EventTransactional tx) {
        // 获取最终的事务配置(合并注解、配置文件和默认值)
        tx = mergeTransactionConfig(tx, event.getEventType());
        // 执行带重试的事务逻辑
        int attempts = Math.max(1, tx.retryAttempts() + 1);
        for (int attempt = 1; attempt <= attempts; attempt++) {
            try {
                if (isNoTransactionPropagation(tx.propagation())) {
                    // 明确不使用事务(NOT_SUPPORTED / NEVER)
                    invokeHandler(handler, event);
                } else {
                    TransactionTemplate template = buildTransactionTemplate(tx);
                    template.execute(status -> {
                        invokeHandler(handler, event);
                        return null;
                    });
                }
                // 成功则跳出重试循环
                break;
            } catch (TransactionException ex) {
                // 如果最后一次仍失败, 记录并抛出
                boolean isLastAttempt = attempt >= attempts;
                if (isLastAttempt) {
                    // 最后一轮失败, 抛出到上层以便被外部捕获/记录
                    log.error("Event handler {} fails for the {}th execution, reaching the maximum number of retries, exception={}", handler.getClass().getSimpleName(), attempt, ex.getMessage(), ex);
                    throw new EventHandleException("Event processing failed: " + handler.getClass().getName(), ex);
                } else {
                    log.warn("Event handler {} fails to execute {} times and is ready to try again, exception={}", handler.getClass().getSimpleName(), attempt, ex.getMessage(), ex);
                    // 退避等待
                    if (tx.retryBackoffMillis() > 0) {
                        try {
                            Thread.sleep(tx.retryBackoffMillis());
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new EventHandleException("Event processing is interrupted", ie);
                        }
                    }
                    // 继续重试
                }
            }
        }
    }

    private @SuppressWarnings("unchecked") void invokeHandler(EventHandler<?> handler, DomainEvent event) {
        long startTime = System.currentTimeMillis();
        try {
            ((EventHandler<DomainEvent>) handler).handle(event);
            long duration = System.currentTimeMillis() - startTime;
            // 超过 5 秒记录警告
            if (duration > 5000) {
                log.warn("Event handlers are slower to execute: {}ms, handler: {}", duration, handler.getClass().getSimpleName());
            }
        } catch (RuntimeException re) {
            throw re; // 运行时异常直接抛出(事务会回滚)
        } catch (Exception ex) {
            // 把 checked exception 包为 EventHandleException 以触发回滚
            throw new EventHandleException("Inspection exceptions occur in event processing", ex);
        }
    }

    /**
     * 判断是否为非事务传播行为
     */
    private boolean isNoTransactionPropagation(Propagation propagation) {
        return propagation == Propagation.NOT_SUPPORTED || propagation == Propagation.NEVER;
    }

    /**
     * 构建事务模板
     */
    private TransactionTemplate buildTransactionTemplate(EventTransactional et) {
        RuleBasedTransactionAttribute txAttr = new RuleBasedTransactionAttribute();
        // 传播级别映射
        txAttr.setPropagationBehavior(mapPropagationToDefinition(et.propagation()));
        txAttr.setReadOnly(et.readOnly());
        if (et.timeout() > 0) txAttr.setTimeout(et.timeout());
        // 设置回滚规则
        List<RollbackRuleAttribute> rollbackRules = new ArrayList<>();
        if (ArrayUtils.isNotEmpty(et.rollbackFor())) {
            for (Class<? extends Throwable> rollback : et.rollbackFor()) {
                rollbackRules.add(new RollbackRuleAttribute(rollback));
            }
        }
        // 设置不回滚规则
        if (ArrayUtils.isNotEmpty(et.noRollbackFor())) {
            for (Class<? extends Throwable> noRollback : et.noRollbackFor()) {
                rollbackRules.add(new NoRollbackRuleAttribute(noRollback));
            }
        }
        // 没有规则设置默认规则
        if (CollectionUtils.isNotEmpty(rollbackRules)) rollbackRules.add(new RollbackRuleAttribute(EventHandleException.class));
        txAttr.setRollbackRules(rollbackRules);

        log.debug("Build a transaction template: propagation={}, timeout={}, rollbackRules={}, noRollbackRules={}", et.propagation(), et.timeout(), Arrays.toString(et.rollbackFor()), Arrays.toString(et.noRollbackFor()));

        return new TransactionTemplate(manager, txAttr);
    }

    /**
     * 映射传播行为
     */
    private int mapPropagationToDefinition(Propagation p) {
        switch (p) {
            case REQUIRES_NEW:
                return TransactionDefinition.PROPAGATION_REQUIRES_NEW;
            case SUPPORTS:
                return TransactionDefinition.PROPAGATION_SUPPORTS;
            case NOT_SUPPORTED:
                return TransactionDefinition.PROPAGATION_NOT_SUPPORTED;
            case MANDATORY:
                return TransactionDefinition.PROPAGATION_MANDATORY;
            case NEVER:
                return TransactionDefinition.PROPAGATION_NEVER;
            case NESTED:
                return TransactionDefinition.PROPAGATION_NESTED;
            case REQUIRED:
            default:
                return TransactionDefinition.PROPAGATION_REQUIRED;
        }
    }

    /**
     * 合并事务配置(注解 > 事件类型配置 > 默认配置)
     */
    private EventTransactional mergeTransactionConfig(@Nullable EventTransactional tx, String eventType) {
        // 注解配置优先级最高
        if (tx != null) return tx;

        // 检查事件类型特定配置
        EventProperties.EventTypeConfig eventTypeConfig = properties.getEventTypeConfig(eventType);
        if (eventTypeConfig != null) return createEventTransactionalFromConfig(eventTypeConfig);

        // 使用默认配置
        return defaultEventTransactional();
    }

    /**
     * 从配置创建 EventTransactional
     */
    private EventTransactional createEventTransactionalFromConfig(EventProperties.EventTypeConfig config) {
        return new EventTransactional() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return EventTransactional.class;
            }

            @Override
            public Propagation propagation() {
                if (config.getPropagation() != null) {
                    try {
                        return Propagation.valueOf(config.getPropagation());
                    } catch (IllegalArgumentException e) {
                        return properties.getTransaction().getPropagationEnum();
                    }
                }
                return properties.getTransaction().getPropagationEnum();
            }

            @Override
            public boolean readOnly() {
                return properties.getTransaction().isDefaultReadOnly();
            }

            @Override
            public int timeout() {
                return config.getTimeout() > 0 ? config.getTimeout() : properties.getTransaction().getDefaultTimeout();
            }

            @Override
            public boolean runAfterCommit() {
                return config.isRunAfterCommit();
            }

            @Override
            public int retryAttempts() {
                return config.getRetryAttempts() > 0 ? config.getRetryAttempts() : properties.getTransaction().getDefaultRetryAttempts();
            }

            @Override
            public long retryBackoffMillis() {
                return config.getRetryBackoff() > 0 ? config.getRetryBackoff() : properties.getTransaction().getDefaultRetryBackoff();
            }

            @Override
            public @SuppressWarnings("unchecked") Class<? extends Throwable>[] rollbackFor() {
                return new Class[]{RuntimeException.class};
            }

            @Override
            public @SuppressWarnings("unchecked") Class<? extends Throwable>[] noRollbackFor() {
                return new Class[]{EventHandleException.class};
            }
        };
    }

    /**
     * 获取默认事务配置
     */
    private EventTransactional defaultEventTransactional() {
        // 默认行为: REQUIRES_NEW、不重试
        return new EventTransactional() {
            @Override
            public Class<? extends Annotation> annotationType() {
                return EventTransactional.class;
            }

            @Override
            public Propagation propagation() {
                return properties.getTransaction().getPropagationEnum();
            }

            @Override
            public boolean readOnly() {
                return properties.getTransaction().isDefaultReadOnly();
            }

            @Override
            public int timeout() {
                return properties.getTransaction().getDefaultTimeout();
            }

            @Override
            public boolean runAfterCommit() {
                return properties.getTransaction().isDefaultRunAfterCommit();
            }

            @Override
            public int retryAttempts() {
                return properties.getTransaction().getDefaultRetryAttempts();
            }

            @Override
            public long retryBackoffMillis() {
                return properties.getTransaction().getDefaultRetryBackoff();
            }

            @Override
            public @SuppressWarnings("unchecked") Class<? extends Throwable>[] rollbackFor() {
                return new Class[]{RuntimeException.class};
            }

            @Override
            public @SuppressWarnings("unchecked") Class<? extends Throwable>[] noRollbackFor() {
                return new Class[]{
                        EventHandleException.class,  // 事件处理异常通常不需要回滚整个事务
                        IllegalArgumentException.class // 参数异常通常也不需要回滚
                };
            }
        };
    }

    /**
     * 查找事务注解(方法优先、类级次之)
     */
    private EventTransactional findEventTransactionalAnnotation(EventHandler<?> handler, DomainEvent event) {
        Class<?> cls = handler.getClass();

        // 先找方法级别的 handle(...) 注解(支持重载时取第一个匹配一个参数的方法)
        Method handleMethod = Arrays.stream(cls.getMethods()).filter(m -> "handle".equals(m.getName()) && m.getParameterCount() == 1).filter(m -> {
            Class<?> paramType = m.getParameterTypes()[0];
            return paramType.isAssignableFrom(event.getClass());
        }).findFirst().orElse(null);

        if (handleMethod != null) {
            EventTransactional annotation = AnnotationUtils.findAnnotation(handleMethod, EventTransactional.class);
            if (annotation != null) return annotation;
        }
        return AnnotationUtils.findAnnotation(cls, EventTransactional.class);
    }

    /**
     * 处理失败的事件
     */
    private void handleFailedEvent(DomainEvent event, EventHandler<?> handler, Exception e) {
        if (properties.isDeadLetterQueueEnabled()) {
            // 这里可以实现死信队列、重试队列等机制
            log.error("The event processing failed, and it entered the dead letter queue, and the event ID: {}, exception: {}", event.getEventId(), e.getMessage());
            // 死信队列、重试队列实现, 暂无实现逻辑, 后续有时间再实现
            // ...
        }
        log.error("Event processing failed, event ID: {}, processor: {}, exception: {}", event.getEventId(), handler.getClass().getSimpleName(), e.getMessage());
    }

    /**
     * 判断是否需要在事务提交后执行
     */
    private boolean shouldRunAfterCommit(@Nullable EventTransactional tx, EventHandler<?> handler) {
        // 1. 检查注解配置
        boolean runAfterCommit = tx != null && tx.runAfterCommit();

        // 2. 检查处理器自身的配置
        if (!runAfterCommit) runAfterCommit = handler.runAfterCommit();

        // 3. 检查事件类型配置
        if (!runAfterCommit) {
            EventProperties.EventTypeConfig eventTypeConfig = properties.getEventTypeConfig(getEventTypeFromHandler(handler));
            if (eventTypeConfig != null) runAfterCommit = eventTypeConfig.isRunAfterCommit();
        }

        return runAfterCommit && TransactionSynchronizationManager.isSynchronizationActive();
    }

    /**
     * 从处理器获取事件类型
     */
    private String getEventTypeFromHandler(EventHandler<?> handler) {
        return handler.getSupportedEvents().isEmpty() ? "" : handler.getSupportedEvents().get(0);
    }

    /**
     * 记录事件处理时长
     */
    private void logEventDuration(DomainEvent event, long startTime, boolean success) {
        long duration = System.currentTimeMillis() - startTime;

        // 记录慢事件
        if (properties.getMonitoring().isLogSlowEvents() && duration > properties.getMonitoring().getSlowEventThreshold()) {
            log.warn("Slower event processing: {}ms, event type: {}, event ID: {}", duration, event.getEventType(), event.getEventId());
        }

        // 添加到事件元数据
        event.addMetadata("processingDuration", duration);
        event.addMetadata("processingSuccess", success);
        event.addMetadata("processingEndTime", System.currentTimeMillis());
    }
}
