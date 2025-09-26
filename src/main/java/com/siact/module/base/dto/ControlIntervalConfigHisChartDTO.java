package com.siact.module.base.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ControlIntervalConfigHisChartDTO {

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

    @ApiModelProperty(value = "控制值图表数据")
    private Map<String, ControlIntervalConfigHisChartDataDTO> configChartDataMap;

}
