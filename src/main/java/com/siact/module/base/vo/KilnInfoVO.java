package com.siact.module.base.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 炉子基本信息配置 VO
 */
@Data
public class KilnInfoVO implements Serializable {
    /** 主键id */
    private Long id;
    /** 炉子编号 */
    private String number;
    /** 炉子编码，数字孪生编码 */
    private String code;
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
    /** 天然气流量设定值上限 */
    private BigDecimal gasValUp;
    /** 天然气流量设定值下限 */
    private BigDecimal gasValLow;
    /** 气量分布上限 */
    private BigDecimal windDisUp;
    /** 气量分布下限 */
    private BigDecimal windDisLow;
    /** 总气量 */
    private BigDecimal totalWindVal;
} 