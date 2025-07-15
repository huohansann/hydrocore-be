package com.siact.module.base.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 炉子下发数据
 */
@Data
public class KilnInfoPublishDTO extends KilnInfoBase {
    /** 状态：是否自动状态（1是 0否） */
    @ApiModelProperty("状态：是否自动状态（1是 0否）")
    private Boolean state;
    /** 天然气计算值 */
    @ApiModelProperty("天然气Dcs值")
    private BigDecimal gasDcsVal;
    /** 天然气设定值 */
    @ApiModelProperty("天然气下发值")
    private BigDecimal gasSendVal;
    /** 助燃风设定值 */
    @ApiModelProperty("助燃风设定值")
    private BigDecimal windVal;
    /** 风气比 */
    @ApiModelProperty("风气比")
    private BigDecimal windGasRate;
}
