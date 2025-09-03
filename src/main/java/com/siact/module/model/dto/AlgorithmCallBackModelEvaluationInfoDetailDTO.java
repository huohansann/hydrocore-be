package com.siact.module.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("算法指标评价详情")
public class AlgorithmCallBackModelEvaluationInfoDetailDTO {
    @ApiModelProperty("r2")
    private String r2;
    @ApiModelProperty("MSE")
    private String MSE;
    @ApiModelProperty("MAE")
    private String MAE;
    @ApiModelProperty("accuracy")
    private String accuracy;
}
