package com.siact.module.base.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class KilnInfoDistributeDTO extends KilnInfoBase {
    /** 状态：是否自动状态（1是 0否） */
    @ApiModelProperty("状态：是否自动状态（1是 0否）")
    private Boolean state;
    /** 天然气计算值 */
    @ApiModelProperty("天然气计算值")
    private BigDecimal gasCalc;
    /** 天然气设定值 */
    @ApiModelProperty("天然气设定值")
    private BigDecimal gasVal;
//    /** 助燃风计算值 */
//    @ApiModelProperty("助燃风计算值")
//    private BigDecimal windCalc;
    /** 助燃风设定值 */
    @ApiModelProperty("助燃风设定值")
    private BigDecimal windVal;
    /** 风气比 */
    @ApiModelProperty("风气比")
    private BigDecimal windGasRate;
}
