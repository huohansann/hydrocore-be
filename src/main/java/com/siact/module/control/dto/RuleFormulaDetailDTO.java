package com.siact.module.control.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("条规则公式详情")
public class RuleFormulaDetailDTO {
    @ApiModelProperty("规则ID")
    private Long ruleId;
    @ApiModelProperty("规则编码")
    private String ruleCode;
    @ApiModelProperty("温度约束jep公式")
    private String tempConditionFormula;
    @ApiModelProperty("天然气调控约束jep公式")
    private String gasOperationFormula;
}
