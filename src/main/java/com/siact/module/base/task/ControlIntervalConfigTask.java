package com.siact.module.base.task;

import com.siact.module.base.service.ControlIntervalConfigService;
import lombok.AllArgsConstructor;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-12-18 9:25
 * @className : ControlIntervalConfigTask
 * @description : 控制区间设置定时任务
 */
@AllArgsConstructor
@Component
@RefreshScope
public class ControlIntervalConfigTask {

    private final ControlIntervalConfigService service;

    /**
     * 三个换火周期更新一次非关键点位数据
     */
    @Scheduled(fixedRateString = "#{${spring.kiln.config.fire-change-cycle} * 3 * 60 * 1000}", initialDelay = 60_000)
    public void syncNonCriticalPoints() {
        service.sync();
    }
}
