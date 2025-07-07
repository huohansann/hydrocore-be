package com.siact.module.control.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("约束规则查询")
public class ControlRuleQuery {

    @ApiModelProperty("1:调节步长 2:天然气气量总和 3:各表天然气气量差值 4:换火 5:液位 6:炉压")
    private List<Integer> types;
}
