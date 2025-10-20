package com.siact.module.predicted.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class AlgorithmPredictionDataParamsDTO {

    @ApiModelProperty(value ="采样时间")
    private String sample;
}
