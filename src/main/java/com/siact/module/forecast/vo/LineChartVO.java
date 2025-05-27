package com.siact.module.forecast.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @desc:
 * @author: zhangwentao
 * @create: 2025-05-27 17:20
 */
@Data
@ApiModel(description = "折线图数据")
public class LineChartVO {
    @ApiModelProperty(value = "名称")
    private String name;

    @ApiModelProperty(value = "数据")
    private LineChartDataVO data;
}