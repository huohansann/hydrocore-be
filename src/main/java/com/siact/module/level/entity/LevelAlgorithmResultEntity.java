package com.siact.module.level.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@TableName("level_algorithm_result")
public class LevelAlgorithmResultEntity {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private String dataCode;
    private BigDecimal levelTrend;
    private BigDecimal recommendedOpening;
    private String levelStatus;
    private Timestamp createTime;
    private Timestamp updateTime;
}
