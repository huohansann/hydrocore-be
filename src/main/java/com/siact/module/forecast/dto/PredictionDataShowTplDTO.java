package com.siact.module.forecast.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "预测数据展示tpl配置DTO")
public class PredictionDataShowTplDTO {
    @ApiModelProperty("dataCode")
    private String dataCode;
    @ApiModelProperty("测点Code")
    private String forecastCode;
    @ApiModelProperty("是否展示实际值")
    private Boolean showActual;
    @ApiModelProperty("是否展示单步预测值")
    private Boolean showSingleForecast;
    @ApiModelProperty("是否展示多步预测值")
    private Boolean showMultiForecast;
    @ApiModelProperty("是否展示上控制线")
    private Boolean showUpControl;
    @ApiModelProperty("是否展示下控制线")
    private Boolean showLowControl;
    @ApiModelProperty("是否展示上告警值")
    private Boolean showUpAlarm;
    @ApiModelProperty("是否展示下告警值")
    private Boolean showLowAlarm;
    @ApiModelProperty("是否展示温度设定线")
    private Boolean showTemperatureSet;
}
