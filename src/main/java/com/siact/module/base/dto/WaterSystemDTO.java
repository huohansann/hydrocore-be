package com.siact.module.base.dto;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;


/**
 * @desc: 水系统返回值
 * @author: zhangwentao
 * @create: 2025-04-15 11:14
 */
@Accessors(chain = true)
@Data
@ApiModel(description = "水系统返回值")
public class WaterSystemDTO {
    @ApiModelProperty(value = "柱状图数据")
    private ColumnChartDTO columnChartDTOList;

    @ApiModelProperty(value = "实时数据")
    private JSONObject realTimeDataDTO;
}