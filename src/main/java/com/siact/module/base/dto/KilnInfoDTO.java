package com.siact.module.base.dto;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 炉子基本信息配置 DTO
 */
@Data
public class KilnInfoDTO implements Serializable {
    /** 主键id */
    private Long id;
    /** 炉子编号 */
    private String number;
    /** 炉子编码，数字孪生编码 */
    private String dataCode;
    /** 状态：是否自动状态（1是 0否） */
    private Boolean state;
    /** 天然气计算值 */
    private BigDecimal gasCalc;
    /** 天然气设定值 */
    private BigDecimal gasVal;
    /** 助燃风调整值 */
    private BigDecimal windVal;
    /** 风气比 */
    private BigDecimal windGasRate;
    /** 天然气流量设定值上限 */
    private BigDecimal gasValUp;
    /** 天然气流量设定值下限 */
    private BigDecimal gasValLow;
    /** 气量分布上限 */
    private BigDecimal windDisUp;
    /** 气量分布下限 */
    private BigDecimal windDisLow;
    /** 天然气流量设定值区间上限 */
    private BigDecimal gasRangeUp;
    /** 天然气流量设定值区间下限 */
    private BigDecimal gasRangeLow;
} 