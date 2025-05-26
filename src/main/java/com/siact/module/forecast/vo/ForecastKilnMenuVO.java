package com.siact.module.forecast.vo;

import com.siact.module.forecast.dto.PredictionTplDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @desc:
 * @author: zhangwentao
 * @create: 2025-05-26 14:54
 */
@Data
@ApiModel(description = "窑炉预测菜单")
public class ForecastKilnMenuVO {
    @ApiModelProperty(value = "菜单code")
    private String menueCode;

    @ApiModelProperty(value = "菜单名称")
    private String menueName;

    @ApiModelProperty(value = "菜单实例")
    List<PredictionTplDTO> pressurePrediction;
}