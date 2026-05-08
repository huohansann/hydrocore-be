package com.siact.module.levelcontrol.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
@TableName("level_predicted_data")
public class LevelPredictedDataEntity {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private String dataCode;
    private String predictedTime;
    private BigDecimal predictedValue;
    private Integer predictedType;
    private String unit;
    private Timestamp createTime;
}
