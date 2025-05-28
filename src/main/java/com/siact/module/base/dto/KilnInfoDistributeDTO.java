package com.siact.module.base.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class KilnInfoDistributeDTO extends KilnInfoBase {
    /** 状态：是否自动状态（1是 0否） */
    private Boolean state;
    /** 天然气计算值 */
    private BigDecimal gasCalc;
    /** 天然气设定值 */
    private BigDecimal gasVal;
    /** 风气比设定值 */
    private BigDecimal windCalc;
    /** 风气比调整值 */
    private BigDecimal windVal;
}
