package com.siact.module.control.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("助燃风控制设定值")
public class ControlSettingWindDTO {
    @ApiModelProperty("炉号")
    private String number;

    @ApiModelProperty("助燃风炉子对应的数字孪生insCode")
    private String windDataCode;

    @ApiModelProperty("天然气炉子对应的数字孪生insCode")
    private String gasDataCode;

    @ApiModelProperty("风气比dcs值")
    private Double rateDcsVal;

    @ApiModelProperty("风气比人工调整值")
    private Double rateManualVal;

    @ApiModelProperty("设定值dcs值")
    private Double settingDcsVal;
}
