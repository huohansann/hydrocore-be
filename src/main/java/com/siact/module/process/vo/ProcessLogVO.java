package com.siact.module.process.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 工艺日志VO
 */
@Data
public class ProcessLogVO {
    private Long id;

    @ApiModelProperty("当前工艺开始日期")
    private String startTime;

    @ApiModelProperty("当前工艺结束日期")
    private String endTime;

    @ApiModelProperty("产线数量(Ⅲ/Ⅳ)")
    private Integer productLineNum;

    @ApiModelProperty("换火周期(单位min)")
    private String fireCycle;

    @ApiModelProperty("除泡系统 Y:有 X:无")
    private String defoamSystem;

    @ApiModelProperty("更换设备 1:正常 2:换机")
    private Integer replaceMachine;

    @ApiModelProperty("工况编码")
    private String operatingCode;

    @ApiModelProperty("工况二进制编码")
    private String binaryCode;

    @ApiModelProperty("操作员")
    private String operator;

    @ApiModelProperty("操作时间")
    private String operationDate;
} 