package com.siact.module.control.enums;

import lombok.Getter;

/**
 * 目前的逻辑：1、2、3、8、9属于气量约束，4、5、6、7属于禁止调节，目前只用到了7、8、9
 */
@Getter
public enum ControlRuleTypeEnum {
    STEP(1, "调节步长"),
    TOTAL_GAS(2, "天然气气量总和"),
    DIFF_GAS(3, "天然气气量差值"),
    FIRE(4, "换火"),
    LIQUID(5, "液位"),
    PRESSURE(6, "炉压"),
    DISABLE(7, "禁止调节"),
    LIMIT(8, "调节限制"),
    GAS_DISTRIBUTION(9, "气量分布");
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
