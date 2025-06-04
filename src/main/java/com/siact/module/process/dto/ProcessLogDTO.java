package com.siact.module.process.dto;

import lombok.Data;
import java.util.Date;
import com.siact.module.process.enums.DefoamingSystemEnum;
import com.siact.module.process.enums.ReplaceMachineEnum;

/**
 * 工艺日志DTO
 */
@Data
public class ProcessLogDTO {
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