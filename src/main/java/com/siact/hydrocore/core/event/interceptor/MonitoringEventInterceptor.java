// package com.siact.hydrocore.core.event.interceptor;
//
//
// import com.siact.hydrocore.core.event.domain.DomainEvent;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.stereotype.Component;
//
// import java.util.concurrent.TimeUnit;
//
// /**
//  * @author : kzuo
//  * @version 1.0
//  * @date : 2026-01-05 15:01
//  * @className : MonitoringEventInterceptor
//  * @description : 监控拦截器
//  */
// @Slf4j
// @Component
// public class MonitoringEventInterceptor implements EventInterceptor {
//
//     private final MeterRegistry meterRegistry;
//
//     public MonitoringEventInterceptor(MeterRegistry meterRegistry) {
//         this.meterRegistry = meterRegistry;
//     }
//
//     @Override
//     public void beforeHandle(DomainEvent event) {
//         event.addMetadata("startTime", System.nanoTime());
//     }
//
//     @Override
//     public void afterHandle(DomainEvent event) {
//         Long startTime = (Long) event.getMetadata("startTime");
//         if (startTime != null) {
//             long duration = System.nanoTime() - startTime;
//
//             // 记录处理时间
//             Timer.builder("event.processing.duration")
//                     .tag("eventType", event.getEventType())
//                     .tag("status", "success")
//                     .register(meterRegistry)
//                     .record(duration, TimeUnit.NANOSECONDS);
//
//             // 记录处理计数
//             Counter.builder("event.processing.count")
//                     .tag("eventType", event.getEventType())
//                     .tag("status", "success")
//                     .register(meterRegistry)
//                     .increment();
//         }
//     }
//
//     @Override
//     public void onError(DomainEvent event, Throwable throwable) {
//         Long startTime = (Long) event.getMetadata("startTime");
//         if (startTime != null) {
//             long duration = System.nanoTime() - startTime;
//
//             Timer.builder("event.processing.duration")
//                     .tag("eventType", event.getEventType())
//                     .tag("status", "error")
//                     .register(meterRegistry)
//                     .record(duration, TimeUnit.NANOSECONDS);
//
//             Counter.builder("event.processing.count")
//                     .tag("eventType", event.getEventType())
//                     .tag("status", "error")
//                     .register(meterRegistry)
//                     .increment();
//         }
//     }
// }
