package com.siact.module.base.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemperatureAlarmVO {
    private boolean alarmed;
    private String message;
    private List<AlarmPointVO> points;
}
