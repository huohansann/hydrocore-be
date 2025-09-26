package com.siact.module.forecast.vo;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @desc:
 * @author: zhangwentao
 * @create: 2025-05-27 17:21
 */
@Data
@ApiModel(description = "折线图数据")
@JsonPropertyOrder({"xData", "seriesData", "maxUpControlVal", "minLowControlVal", "maxUpAlarmVal", "minLowAlarmVal", "maxTemperatureSetVal", "minTemperatureSetVal"})
public class LineChartDataVO {
    @ApiModelProperty(value = "X轴数据")
    private List<String> xData;

    @ApiModelProperty(value = "Y轴数据")
    private SeriesDataVO seriesData;

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