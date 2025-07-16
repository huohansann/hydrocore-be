package com.siact.module.base.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.siact.common.config.BigDecimalTrimmingConverter;
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
    private String dataCode;
    /** 状态：是否自动状态（1是 0否） */
    @ApiModelProperty(value = "状态：是否自动状态（1是 0否）")
    private Boolean state;
    /** 天然气DCS运行值 */
    @ApiModelProperty(value = "天然气DCS运行值")
    @JsonSerialize(using = BigDecimalTrimmingConverter.class)
    private BigDecimal gasDcs;
    /** 天然气计算值 */
    @ApiModelProperty(value = "天然气计算值")
    @JsonSerialize(using = BigDecimalTrimmingConverter.class)
    private BigDecimal gasCalc;
    /** 天然气设定值 */
    @ApiModelProperty(value = "天然气设定值")
    @JsonSerialize(using = BigDecimalTrimmingConverter.class)
    private BigDecimal gasVal;
//    @ApiModelProperty(value = "天然气变动值")
//    @JsonSerialize(using = BigDecimalTrimmingConverter.class)
//    private BigDecimal gasValueChange;
    /** 助燃风设定值 */
    @ApiModelProperty(value = "助燃风设定值")
    @JsonSerialize(using = BigDecimalTrimmingConverter.class)
    private BigDecimal windVal;
    /** 风气比 */
    @ApiModelProperty(value = "风气比")
    @JsonSerialize(using = BigDecimalTrimmingConverter.class)
    private BigDecimal windGasRate;
    /** 天然气流量设定值上限 */
    @ApiModelProperty(value = "天然气流量设定值上限")
    @JsonSerialize(using = BigDecimalTrimmingConverter.class)
    private BigDecimal gasValUp;
    /** 天然气流量设定值下限 */
    @ApiModelProperty(value = "天然气流量设定值下限")
    @JsonSerialize(using = BigDecimalTrimmingConverter.class)
    private BigDecimal gasValLow;
    /** 气量分布上限 */
    @ApiModelProperty(value = "气量分布上限")
    @JsonSerialize(using = BigDecimalTrimmingConverter.class)
    private BigDecimal windDisUp;
    /** 气量分布下限 */
    @ApiModelProperty(value = "气量分布下限")
    @JsonSerialize(using = BigDecimalTrimmingConverter.class)
    private BigDecimal windDisLow;

} 