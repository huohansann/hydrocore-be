package com.siact.module.control.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ControlRuleLocalTplSettingDTO {

    @ApiModelProperty(value = "换火是否启用本地控制")
    private Boolean fireLocalControl;
    @ApiModelProperty(value = "换火本地状态")
    private Boolean fireLocalStatus;
    @ApiModelProperty(value = "液位是否启用本地控制")
    private Boolean liquidLocalControl;
    @ApiModelProperty(value = "液位本地状态")
    private Boolean liquidLocalStatus;
    @ApiModelProperty(value = "炉压是否启用本地控制")
    private Boolean pressureLocalControl;
    @ApiModelProperty(value = "炉压本地状态")
    private Boolean pressureLocalStatus;
}

