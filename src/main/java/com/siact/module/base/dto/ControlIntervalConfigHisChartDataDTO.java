package com.siact.module.base.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ControlIntervalConfigHisChartDataDTO {
    /**
     * 上控制值
     */
    private List<Object[]> upControlChart;

    /**
     * 下控制值
     */
    private List<Object[]> lowControlChart;

    /**
     * 上告警值
     */
    private List<Object[]> upAlarmChart;

    /**
     * 下告警值
     */
    private List<Object[]> lowAlarmChart;

    /**
     * 温度设定线
     */
    private List<Object[]> temperatureSetChart;
}
