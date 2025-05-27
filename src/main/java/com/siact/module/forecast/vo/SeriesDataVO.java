package com.siact.module.forecast.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @desc:
 * @author: zhangwentao
 * @create: 2025-05-27 17:24
 */
@Data
@ApiModel(description = "系列数据")
public class SeriesDataVO {
    @ApiModelProperty(value = "实际值")
    private AttributeInfoVO actual;
    @ApiModelProperty(value = "单步预测值")
    private AttributeInfoVO singleForecast;
    @ApiModelProperty(value = "多步预测值")
    private AttributeInfoVO multiForecast;
}