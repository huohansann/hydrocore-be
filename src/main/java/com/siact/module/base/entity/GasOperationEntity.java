package com.siact.module.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;

@Data
@TableName("gas_operation")
public class GasOperationEntity implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String ruleCode;
    private String furnaceCode;
    private String operation;
    private Double lowVal;
    private Double upVal;
} 