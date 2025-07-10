package com.siact.module.control.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("control_rule")
public class ControlRuleEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    // 1:调节步长 2:天然气气量总和 3:各表天然气气量差值 4:换火 5:液位 6:炉压
    private Integer type;

    // 计算公式
    private String formula;
    // 计算公式描述
    private String formulaDesc;
    // 计算公式单位
    private String formulaUnit;

    // 运算符号 > >= < <= = != ±
    private String symbol;

    // 比较值
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private BigDecimal compareValue;
    // 1:普通数值 2:绝对值 3:百分比
    private Integer compareType;
    // 比较值描述
    private String compareDesc;
    // 比较值单位
    private String compareUnit;
    // 比较值公式,ps:这里逻辑处理为比较值*比较值公式
    private String compareFormula;

    // 单位转换系数
    private BigDecimal factor;

    // 状态 1:启用 2:禁用
    private String status;
    // 创建时间
    private Date createTime;
    // 修改时间
    private Date updateTime;
}
