package com.siact.module.base.vo;

import io.swagger.annotations.ApiModelProperty;
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
    @ApiModelProperty(value = "炉子编号")
    private String number;
    /** 炉子编码，数字孪生编码 */
    @ApiModelProperty(value = "炉子编码，数字孪生编码")
    private String code;
    /** 状态：是否自动状态（1是 0否） */
    @ApiModelProperty(value = "状态：是否自动状态（1是 0否）")
    private Boolean state;
    /** 天然气计算值 */
    @ApiModelProperty(value = "天然气计算值")
    private BigDecimal gasCalc;
    /** 天然气设定值 */
    @ApiModelProperty(value = "天然气设定值")
    private BigDecimal gasVal;
    /** 助燃风设定值 */
    @ApiModelProperty(value = "助燃风设定值")
    private BigDecimal windVal;
    /** 风气比 */
    @ApiModelProperty(value = "风气比")
    private BigDecimal windGasRate;
    /** 天然气流量设定值上限 */
    @ApiModelProperty(value = "天然气流量设定值上限")
    private BigDecimal gasValUp;
    /** 天然气流量设定值下限 */
    @ApiModelProperty(value = "天然气流量设定值下限")
    private BigDecimal gasValLow;
    /** 气量分布上限 */
    @ApiModelProperty(value = "气量分布上限")
    private BigDecimal windDisUp;
    /** 气量分布下限 */
    @ApiModelProperty(value = "气量分布下限")
    private BigDecimal windDisLow;
    /** 总气量 */
    @ApiModelProperty(value = "总气量")
    private BigDecimal totalWindVal;
} 