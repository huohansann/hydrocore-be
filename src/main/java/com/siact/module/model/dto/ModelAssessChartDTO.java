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
public class ModelAssessChartDTO {
    @ApiModelProperty("数据List")
    private List<ModelAssessChartDetailDTO> dataList;

    @ApiModelProperty("x轴数据")
    private List<String> xAxis;
}
