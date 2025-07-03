package com.siact.module.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("算法指标评价")
public class AlgorithmCallBackModelEvaluationInfoDTO {
    @ApiModelProperty("训练集评价")
    private AlgorithmCallBackModelEvaluationInfoDetailDTO train;
    @ApiModelProperty("验证集评价")
    private AlgorithmCallBackModelEvaluationInfoDetailDTO val;
    @ApiModelProperty("测试集评价")
    private AlgorithmCallBackModelEvaluationInfoDetailDTO test;
}
