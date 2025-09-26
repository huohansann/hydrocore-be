package com.siact.module.base.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
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

    @ApiModelProperty(value = "上控制值(最大)")
    private String maxUpControlVal;

    @ApiModelProperty(value = "下控制值(最小)")
    private String minLowControlVal;

    @ApiModelProperty(value = "上告警值(最大)")
    private String maxUpAlarmVal;

    @ApiModelProperty(value = "下告警值(最小)")
    private String minLowAlarmVal;

    @ApiModelProperty(value = "温度设定线(最大值)")
    private String maxTemperatureSetVal;

    @ApiModelProperty(value = "温度设定线(最小值)")
    private String minTemperatureSetVal;
}
