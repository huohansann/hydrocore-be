package com.siact.module.base.task;

import com.siact.common.redis.RedisService;
import com.siact.module.base.service.TemperatureAlarmService;
import lombok.extern.slf4j.Slf4j;
// import org.springframework.scheduling.annotation.Scheduled;
// import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
// @Component
public class TemperatureAlarmTask {

    private static final String LOCK_KEY = "lock:temperature_alarm";
    private static final long LOCK_TIMEOUT = 120;

    private final TemperatureAlarmService alarmService;
    private final RedisService redis;

    public TemperatureAlarmTask(TemperatureAlarmService alarmService, RedisService redis) {
        this.alarmService = alarmService;
        this.redis = redis;
    }

    // @Scheduled(fixedDelay = 60000)
    public void checkTemperatureAlarm() {
        String lockValue = UUID.randomUUID().toString();
        if (!redis.tryLock(LOCK_KEY, lockValue, LOCK_TIMEOUT)) {
            log.info("温度告警任务正在执行，跳过本次触发");
            return;
        }
        try {
            alarmService.checkAndAlarm();
        } catch (Exception e) {
            log.error("温度告警检查异常: {}", e.getMessage(), e);
        } finally {
            redis.unlock(LOCK_KEY, lockValue);
        }
    }
}
