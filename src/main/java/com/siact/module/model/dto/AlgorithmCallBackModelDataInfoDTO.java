package com.siact.module.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("算法回调模型数据")
public class AlgorithmCallBackModelDataInfoDTO {
    @ApiModelProperty("训练集数据")
    private AlgorithmCallBackModelDataDetailInfoDTO train;
    @ApiModelProperty("验证集数据")
    private AlgorithmCallBackModelDataDetailInfoDTO val;
    @ApiModelProperty("测试集数据")
    private AlgorithmCallBackModelDataDetailInfoDTO test;
}
