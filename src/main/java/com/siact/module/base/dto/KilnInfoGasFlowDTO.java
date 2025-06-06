package com.siact.module.base.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 炉子天然气流量信息
 */
@Data
public class KilnInfoGasFlowDTO extends KilnInfoBase {
    /** 天然气流量设定值上限 */
    private BigDecimal gasValUp;
    /** 天然气流量设定值下限 */
    private BigDecimal gasValLow;
}
