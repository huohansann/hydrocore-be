package com.siact.module.predicted.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("Mqtt预测数据")
public class PredictedMqttDTO {
    @ApiModelProperty("dataCode")
    private String dataCode;
    @ApiModelProperty("步长")
    private List<PredictedStepMqttDTO> stepList;
}
