package com.siact.module.base.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class ControlIntervalConfigChartDTO {

    @ApiModelProperty(value = "上控制值数据")
    private List<Object[]> upControlData;

    @ApiModelProperty(value = "下控制值数据")
    private List<Object[]> lowControlData;

    @ApiModelProperty(value = "上告警值数据")
    private List<Object[]> upAlarmData;

    @ApiModelProperty(value = "下告警值数据")
    private List<Object[]> lowAlarmData;

    @ApiModelProperty(value = "温度设定值数据")
    private List<Object[]> temperatureSetData;

    @ApiModelProperty(value = "x轴")
    private List<String> xAxis;
}
