package com.siact.module.base.service;

import com.siact.module.base.vo.TemperatureAlarmVO;

public interface TemperatureAlarmService {
    TemperatureAlarmVO checkAndAlarm();
    void stopRinging();
    void playTone(String toneType);
    void stopTone();
    boolean isTonePlaying();
}
