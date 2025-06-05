package com.siact.module.process.dto;

import cn.hutool.core.date.DateTime;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.Date;
import com.siact.module.process.enums.DefoamSystemEnum;
import com.siact.module.process.enums.ReplaceMachineEnum;

/**
 * 工艺日志DTO
 */
@Data
public class ProcessLogDTO {
    private Long id;

    @ApiModelProperty(value = "开始日期")
    private String startTime;

    @ApiModelProperty(value = "结束日期")
    private String endTime;

    @ApiModelProperty(value = "产线数量(Ⅲ\\Ⅳ)")
    private String productLineNum;

    @ApiModelProperty(value = "换火周期(单位min)")
    private String fireCycle;

    @ApiModelProperty(value = "除泡系统 Y:有 X:无")
    private String defoamSystem;

    @ApiModelProperty(value = "更换设备 1:正常 2:换机")
    private Integer replaceMachine;

    @ApiModelProperty(value = "操作人")
    private String operator;

    @ApiModelProperty(value = "操作时间")
    private String operationDate;
} 