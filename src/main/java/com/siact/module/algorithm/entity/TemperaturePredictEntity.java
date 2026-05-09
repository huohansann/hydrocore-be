package com.siact.module.algorithm.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("temperature_predict")
public class TemperaturePredictEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String pointName;

    private String propName;

    private String propCode;

    private String time;

    private BigDecimal itemValue;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
}