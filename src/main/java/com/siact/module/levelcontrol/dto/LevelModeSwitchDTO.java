package com.siact.module.levelcontrol.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LevelModeSwitchDTO {

    @NotBlank(message = "控制模式不能为空")
    @ApiModelProperty(value = "控制模式：ai/pid/manual", required = true)
    private String mode;
}
