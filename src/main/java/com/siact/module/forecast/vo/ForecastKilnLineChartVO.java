package com.siact.module.forecast.vo;

import com.siact.module.base.dto.BasicDataDTO;
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
public class ForecastKilnLineChartVO {
    @ApiModelProperty(value = "数据")
    private List<ForecastKilnDetailVO> data;

    @ApiModelProperty(value = "x轴")
    private List<String> xAxis;
}