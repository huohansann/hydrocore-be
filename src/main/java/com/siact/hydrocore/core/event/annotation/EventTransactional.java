package com.siact.hydrocore.core.event.annotation;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-05 17:18
 * @className : NoEventTransaction
 * @description : 事件订阅发布事务管理
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Transactional
public @interface EventTransactional {
    /**
     * 事务传播(默认 REQUIRES_NEW)
     */
    Propagation propagation() default Propagation.REQUIRES_NEW;

    /**
     * 是否只读
     */
    boolean readOnly() default false;

    /**
     * 事务超时(秒), -1 表示默认
     */
    int timeout() default -1;

    /**
     * 是否把处理延后到当前事务提交后执行
     * <ul>
     *     <li> 仅当发布线程有 Spring 事务上下文时生效 </li>
     *     <li> 如果发布线程没有事务, 则立即执行(并按照其它属性决定是否在事务中执行) </li>
     * </ul>
     */
    boolean runAfterCommit() default false;

    /**
     * 重试次数(失败后最多重试 N 次), 默认 0(不重试)
     */
    int retryAttempts() default 0;

    /**
     * 重试间隔(毫秒)
     */
    long retryBackoffMillis() default 1000L;

    /**
     * 触发回滚的异常类型(默认 RuntimeException)
     */
    Class<? extends Throwable>[] rollbackFor() default {RuntimeException.class};

    /**
     * 不会触发回滚的异常类型
     */
    Class<? extends Throwable>[] noRollbackFor() default {};
}
