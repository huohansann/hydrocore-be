package com.siact.hydrocore.module.base.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @desc: 水系统入参
 * @author: zhangwentao
 * @create: 2025-04-15 11:05
 */
@ApiModel(description = "水系统入参")
@Data
public class WaterSystemVO {
    @ApiModelProperty(value = "柱状图code", required = true)
    private String columnChartCode;

    @ApiModelProperty(value = "实时数据code", required = true)
    private String realTimeCode;

    @ApiModelProperty(value = "数据类型", required = true)
    private String dataType;

    @ApiModelProperty("开始时间")
    private String startTime;

    @ApiModelProperty("结束时间")
    private String endTime;

    @ApiModelProperty("步长")
    private Integer ts = 1;

    @ApiModelProperty("时间单位")
    private String tsUnit;

    @ApiModelProperty("计算类型")
    private String calcType;

    @ApiModelProperty("返回时间格式，如MM-dd")
    private String formatVal;
}