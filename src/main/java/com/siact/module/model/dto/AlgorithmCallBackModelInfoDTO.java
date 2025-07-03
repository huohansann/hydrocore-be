package com.siact.module.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("算法回调模型信息")
public class AlgorithmCallBackModelInfoDTO {
    @ApiModelProperty(value ="模型id")
    private String model_id;
    @ApiModelProperty(value ="模型名称")
    private String model_name;
    @ApiModelProperty(value ="评估指标")
    private AlgorithmCallBackModelEvaluationInfoDTO evaluation;
    @ApiModelProperty(value ="训练数据")
    private AlgorithmCallBackModelDataInfoDTO data;
}
