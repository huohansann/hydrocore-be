package com.siact.module.control.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
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

    private BigDecimal adjustValue;
}
