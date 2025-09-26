package com.siact.module.forecast.vo;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @desc:
 * @author: zhangwentao
 * @create: 2025-05-27 17:24
 */
@Data
@ApiModel(description = "系列数据")
@JsonPropertyOrder({"actual", "singleForecast", "multiForecast", "upControl", "lowControl", "upAlarm", "lowAlarm", "temperatureSet"})
public class SeriesDataVO {
    @ApiModelProperty(value = "实际值")
    private AttributeInfoVO actual;
    @ApiModelProperty(value = "单步预测值")
    private AttributeInfoVO singleForecast;
    @ApiModelProperty(value = "多步预测值")
    private AttributeInfoVO multiForecast;

    @ApiModelProperty(value = "上控制值")
    private AttributeInfoVO upControl;
    @ApiModelProperty(value = "下控制值")
    private AttributeInfoVO lowControl;
    @ApiModelProperty(value = "上告警值")
    private AttributeInfoVO upAlarm;
    @ApiModelProperty(value = "下告警值")
    private AttributeInfoVO lowAlarm;
    @ApiModelProperty(value = "温度设定线")
    private AttributeInfoVO temperatureSet;
}