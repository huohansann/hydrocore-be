package com.siact.module.process.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.util.Date;
import com.siact.module.process.enums.DefoamingSystemEnum;
import com.siact.module.process.enums.ReplaceMachineEnum;

/**
 * 工艺日志实体类
 */
@Data
@TableName("process_log")
public class ProcessLogEntity {
    @TableId
    private Long id;
    private Date startTime;
    private Date endTime;
    private Integer productLineNum;
    private Integer fireCycle;
    private Integer defoamingSystem;
    private Integer replaceMachine;
    private String operator;
    private Date operationDate;
} 