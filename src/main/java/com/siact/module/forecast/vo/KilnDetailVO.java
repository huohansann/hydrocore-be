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
@ApiModel(description = "窑炉数据查询参数:真实数据，预测数据")
@Data
public class KilnDetailVO extends KilnForecastDetailVO {

    @ApiModelProperty(value = "实际值")
    private List<Object[]> actualValueList;

}