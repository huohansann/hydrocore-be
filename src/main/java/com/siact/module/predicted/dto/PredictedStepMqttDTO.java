package com.siact.module.predicted.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel("Mqtt预测值步长数据(包含单步和多步,通过type进行区分)")
public class PredictedStepMqttDTO {
    @ApiModelProperty("dataCode")
    private String dataCode;
    @ApiModelProperty("步长类型,单步如:T20,T40,多步:MULTI")
    private String typeCode;
    @ApiModelProperty("预测的数据时间")
    private String time;
    @ApiModelProperty("预测值")
    private BigDecimal itemVal;
    @ApiModelProperty("单位")
    private String unit;
}
