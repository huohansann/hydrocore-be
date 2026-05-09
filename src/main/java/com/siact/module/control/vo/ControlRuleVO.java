package com.siact.module.control.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel(value = "约束规则VO")
public class ControlRuleVO {

    private Long id;

    @ApiModelProperty(value = "类型 1:调节步长 2:天然气气量总和 3:各表天然气气量差值 4:换火 5:液位 6:炉压")
    private Integer type;

    @ApiModelProperty(value = "计算公式")
    private String formula;

    @ApiModelProperty(value = "计算公式描述")
    private String formulaDesc;

    @ApiModelProperty(value = "计算公式单位")
    private String formulaUnit;

    @ApiModelProperty(value = "运算符号")
    private String symbol;

    @ApiModelProperty(value = "比较值")
    private BigDecimal compareValue;

    @ApiModelProperty(value = "比较类型 1:普通数值 2:绝对值 3:百分比")
    private Integer compareType;

    @ApiModelProperty(value = "比较值描述")
    private String compareDesc;

    @ApiModelProperty(value = "比较值单位")
    private String compareUnit;

    @ApiModelProperty(value = "比较值公式")
    private String compareFormula;

    @ApiModelProperty(value = "调节周期(分钟)")
    private Integer adjustCycle;

    @ApiModelProperty(value = "单位转换系数")
    private BigDecimal factor;

    @ApiModelProperty(value = "状态 1:启用 2:禁用")
    private String status;

    @ApiModelProperty(value = "是否合法 1:合法 2:不合法")
    private Boolean legal;

    @ApiModelProperty(value = "创建时间")
    private String createTime;

    @ApiModelProperty(value = "修改时间")
    private String updateTime;
}
