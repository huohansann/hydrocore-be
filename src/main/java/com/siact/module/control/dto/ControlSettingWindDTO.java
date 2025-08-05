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
@ApiModel("助燃风控制设定值")
public class ControlSettingWindDTO {
    @ApiModelProperty("炉号")
    private String number;

    @ApiModelProperty("炉子的孪生dataCode")
    private String dataCode;

    @ApiModelProperty("风气比dcs值")
    private BigDecimal rateDcsVal;

    @ApiModelProperty("风气比人工调整值")
    private BigDecimal rateManualVal;

    @ApiModelProperty("设定值dcs值")
    private BigDecimal settingDcsVal;
}
