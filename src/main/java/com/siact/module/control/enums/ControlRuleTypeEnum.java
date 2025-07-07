package com.siact.module.control.enums;

import lombok.Getter;

@Getter
public enum ControlRuleTypeEnum {
    STEP(1, "调节步长"),
    TOTAL_GAS(2, "天然气气量总和"),
    DIFF_GAS(3, "天然气气量差值"),
    FIRE(4, "换火"),
    LIQUID(5, "液位"),
    PRESSURE(6, "炉压");
    private Integer code;
    private String desc;

    ControlRuleTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
