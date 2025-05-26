package com.siact.module.forecast.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @desc:
 * @author: zhangwentao
 * @create: 2025-05-26 10:35
 */
@ApiModel(description = "窑炉预测数据查询参数")
@Data
public class ForecastKilnInfoVO {
    @ApiModelProperty(value = "模板编码")
    private String tplCode;

    @ApiModelProperty(value = "预测编码")
    private String predictionCode;
}