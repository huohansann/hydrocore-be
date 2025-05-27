package com.siact.module.forecast.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @desc:
 * @author: zhangwentao
 * @create: 2025-05-27 17:21
 */
@Data
@ApiModel(description = "折线图数据")
public class LineChartDataVO {
    @ApiModelProperty(value = "X轴数据")
    private List<String> xData;

    @ApiModelProperty(value = "Y轴数据")
    private SeriesDataVO seriesData;
}