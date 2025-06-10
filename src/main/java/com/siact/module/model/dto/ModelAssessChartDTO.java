package com.siact.module.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("模型指标评价图表")
public class ModelAssessChartDTO {
    @ApiModelProperty("数据List")
    private List<Object[]> dataList;

    @ApiModelProperty("x轴数据")
    private List<String> xAxis;
}
