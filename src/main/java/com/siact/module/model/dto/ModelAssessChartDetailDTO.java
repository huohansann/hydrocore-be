package com.siact.module.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("模型指标评价图表")
public class ModelAssessChartDetailDTO {

    @ApiModelProperty("模型名称(算法)")
    private String modelName;

    @ApiModelProperty("自定义模型名称")
    private String customModelName;

    @ApiModelProperty("预测类型 1:单步预测 2:多步预测")
    private Integer predictedType;

    @ApiModelProperty("预测类型Code,单步如:T20,T40,多步:MULTI")
    private String predictedTypeCode;

    @ApiModelProperty("数据List")
    private List<Object[]> values;
}
