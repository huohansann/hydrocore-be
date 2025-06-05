package com.siact.module.base.dto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@Api("测点条件")
public class TempConditionDTO implements Serializable {
    private Long id;

    @ApiModelProperty("规则编码")
    private String ruleCode;

    @ApiModelProperty("测点编码")
    private String mcCode;

    @ApiModelProperty("操作符")
    private String operation;

    @ApiModelProperty("是否超出阈值操作符")
    private String exceedsLimit;

    @ApiModelProperty("阈值")
    private Double threshold;
} 