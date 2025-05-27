package com.siact.module.forecast.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @desc: 公共组件：柱状图
 * @author: zhangwentao
 * @create: 2025-04-15 11:15
 */
@Data
@ApiModel(description = "柱状图数据")
public class KilnLineChartVO {
    @ApiModelProperty(value = "数据")
    private List<KilnDetailVO> data;

    @ApiModelProperty(value = "x轴")
    private List<String> xAxis;
}