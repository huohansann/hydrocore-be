package com.siact.module.forecast.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @desc:
 * @author: zhangwentao
 * @create: 2025-05-26 10:35
 */
@ApiModel(description = "窑炉预测数据查询参数")
@Data
public class KilnForecastDetailVO {
    @ApiModelProperty(value = "参数code")
    private String dataCode;

    @ApiModelProperty(value = "参数名称")
    private String name;

    @ApiModelProperty(value = "参数单位")
    private String unit;

    @ApiModelProperty(value = "单步预测值")
    private List<Object[]> singleStepForecastValueList;

    @ApiModelProperty(value = "多步预测值")
    private List<Object[]> MultiStepForecastValueList;

    @ApiModelProperty(value = "时间轴")
    private List<String> timeList;
}