package com.siact.module.process.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;
import com.siact.module.process.enums.DefoamSystemEnum;
import com.siact.module.process.enums.ReplaceMachineEnum;

/**
 * 工艺日志实体类
 */
@Data
@TableName("process_log")
public class ProcessLogEntity {
    @TableId
    private Long id;
    // 开始日期
    private String startTime;
    // 结束日期
    private String endTime;
    // 产线数量(Ⅲ\Ⅳ)
    private String productLineNum;
    // 换火周期(单位min)
    private String fireCycle;
    // 除泡系统 Y:有 X:无
    private String defoamSystem;
    // 更换设备 1:正常 2:换机
    private Integer replaceMachine;
    // 工况编码
    private String operatingCode;
    // 二进制编码
    private String binaryCode;
    // 操作人
    private String operator;
    // 操作时间
    private String operationDate;
} 