package com.siact.module.base.dto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Api(value = "规则参数")
public class RuleAddDTO implements Serializable {
    @ApiModelProperty(value = "规则编码")
    private String ruleCode;
    @ApiModelProperty(value = "规则名称")
    private String ruleName;
    @ApiModelProperty(value = "规则状态")
    private Integer status;
    @ApiModelProperty(value = "测点条件")
    private List<TempConditionDTO> tempConditions;
    @ApiModelProperty(value = "炉子操作")
    private List<GasOperationDTO> gasOperations;
} 