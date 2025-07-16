package com.siact.module.process.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 工艺日志DTO
 */
@Data
public class ProcessLogQueryDTO {
    private Long id;

    @NotBlank(message = "开始日期不能为空")
    @ApiModelProperty(value = "开始日期")
    private String startTime;

    @NotBlank(message = "结束日期不能为空")
    @ApiModelProperty(value = "结束日期")
    private String endTime;

    @NotBlank(message = "产线数量不能为空")
    @ApiModelProperty(value = "产线数量(Ⅲ\\Ⅳ)")
    private String productLineNum;

    @NotBlank(message = "换火周期不能为空")
    @ApiModelProperty(value = "换火周期(单位min)")
    private String fireCycle;

    @NotBlank(message = "消泡系统不能为空")
    @ApiModelProperty(value = "除泡系统 Y:有 X:无")
    private String defoamSystem;

    @NotNull(message = "换机状态不能为空")
    @ApiModelProperty(value = "更换设备 1:正常 2:换机")
    private Integer replaceMachine;

    @ApiModelProperty(value = "操作人,为空则为当前登录用户名")
    private String operator;

    @ApiModelProperty(value = "操作时间")
    private String operationDate;
} 