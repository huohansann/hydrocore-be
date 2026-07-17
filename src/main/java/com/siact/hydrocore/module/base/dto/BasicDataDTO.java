package com.siact.hydrocore.module.base.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @desc:
 * @author: zhangwentao
 * @create: 2025-04-17 08:54
 */
@Data
@ApiModel(description = "基础数据")
public class BasicDataDTO {

    @ApiModelProperty(value = "实例code",example = "PGY02003_SSXT1002_ST00000000_U00000000_BJSXT1SBBJ1042_MP0000000")
    private String insDataCode;

    @ApiModelProperty(value = "属性code",example = "PGY02003_SSXT1002_ST00000000_U00000000_BJSXT1SBBJ1042_MPLJS2001")
    private String dataCode;

    @ApiModelProperty(value = "属性名称")
    private String name;

    @ApiModelProperty(value = "单位")
    private String unit;

    @ApiModelProperty(value = "数据")
    private List<Object[]> data;
}