package com.siact.module.control.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("天然气控制设定值")
public class ControlSettingGasDTO {

    @ApiModelProperty("炉号")
    private String number;

    @ApiModelProperty("炉子的孪生dataCode")
    private String dataCode;

    @ApiModelProperty("dcs运行值")
    private Double runningDcsVal;

    @ApiModelProperty("智控算法计算值")
    private Double gasAlgorithmCalcVal;

    @ApiModelProperty("人工调整值")
    private Double gasManualVal;

    @ApiModelProperty("是否自动模式,1:是 0:否")
    private Boolean autoState;

    @ApiModelProperty("是否通过智控校验,1:是 0:否")
    private Boolean ruleValid;

    // 变化值
    private BigDecimal adjustValue;

    // true: dcs 与上一次 dcs 不同, dcs 与算法输出不同
    private Boolean dcsDiff;

    // true: dcs 与算法输出不同, 算法输出与上一次算法输出不同
    private Boolean algoDiff;
}
