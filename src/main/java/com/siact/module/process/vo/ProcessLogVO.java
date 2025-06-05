package com.siact.module.process.vo;

import lombok.Data;
import java.util.Date;
import com.siact.module.process.enums.DefoamSystemEnum;
import com.siact.module.process.enums.ReplaceMachineEnum;

/**
 * 工艺日志VO
 */
@Data
public class ProcessLogVO {
    private Long id;
    private String startTime;
    private String endTime;
    private Integer productLineNum;
    private Integer fireCycle;
    private DefoamSystemEnum defoamSystem;
    private ReplaceMachineEnum replaceMachine;
    private String operator;
    private String operationDate;
} 