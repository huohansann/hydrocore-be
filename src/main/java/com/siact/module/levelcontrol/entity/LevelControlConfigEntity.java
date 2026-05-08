package com.siact.module.levelcontrol.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@TableName("level_control_config")
public class LevelControlConfigEntity {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private String dataCode;
    private String mode;
    private BigDecimal aiPredictWindow;
    private BigDecimal aiPredictDuration;
    private BigDecimal pidPb;
    private BigDecimal pidTi;
    private BigDecimal pidTd;
    private BigDecimal manualControlValue;
    private BigDecimal safeLimit;
    private BigDecimal openingUpperLimit;
    private Timestamp createTime;
    private Timestamp updateTime;
    @TableLogic
    private Boolean deleted;
}
