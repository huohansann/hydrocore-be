package com.siact.module.base.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class KilnInfoWindDisDTO extends KilnInfoBase {
    /** 气量分布上限 */
    private BigDecimal windDisUp;
    /** 气量分布下限 */
    private BigDecimal windDisLow;
}
