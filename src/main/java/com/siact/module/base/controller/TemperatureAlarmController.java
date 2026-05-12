package com.siact.module.base.controller;

import com.siact.module.base.service.TemperatureAlarmService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

@Api(tags = "温度告警")
@RestController
@RequestMapping("/alarm/temperature")
public class TemperatureAlarmController {

    private final TemperatureAlarmService alarmService;

    public TemperatureAlarmController(TemperatureAlarmService alarmService) {
        this.alarmService = alarmService;
    }

    @ApiOperation("确认告警，停止响铃")
    @PostMapping("/confirm")
    public Boolean confirm() {
        alarmService.stopRinging();
        return true;
    }

    @ApiOperation("测试音频播放（toneType: NOTICE / STATUS / WARNING / ALARM / CRITICAL）")
    @PostMapping("/tone/play")
    public Boolean playTone(@RequestParam String toneType) {
        alarmService.playTone(toneType);
        return true;
    }

    @ApiOperation("停止音频播放")
    @PostMapping("/tone/stop")
    public Boolean stopTone() {
        alarmService.stopTone();
        return true;
    }

    @ApiOperation("查询音频是否正在播放")
    @GetMapping("/tone/playing")
    public Boolean isPlaying() {
        return alarmService.isTonePlaying();
    }
}