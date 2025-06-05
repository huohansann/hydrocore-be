package com.siact.module.base.dto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
@Api("炉气量操作")
public class GasOperationDTO implements Serializable {
    private Long id;

    @ApiModelProperty("规则编号")
    private String ruleCode;

    @ApiModelProperty("炉号")
    private String furnaceCode;

    @ApiModelProperty("操作")
    private String operation;

    @ApiModelProperty("操作下限值")
    private Double lowVal;

    @ApiModelProperty("操作上限值")
    private Double upVal;
} 