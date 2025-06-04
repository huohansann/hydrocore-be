package com.siact.module.process.vo;

import lombok.Data;
import java.util.Date;
import com.siact.module.process.enums.DefoamingSystemEnum;
import com.siact.module.process.enums.ReplaceMachineEnum;

/**
 * 工艺日志VO
 */
@Data
public class ProcessLogVO {
    private Long id;
    private Date startTime;
    private Date endTime;
    private Integer productLineNum;
    private Integer fireCycle;
    private DefoamingSystemEnum defoamingSystem;
    private ReplaceMachineEnum replaceMachine;
    private String operator;
    private Date operationDate;
} 