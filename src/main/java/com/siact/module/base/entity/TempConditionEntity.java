package com.siact.module.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;

@Data
@TableName("temp_condition")
public class TempConditionEntity implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String ruleCode;
    private String mcCode;
    private String operation;
    @TableField(value = "exceeds_limit")
    private String exceedsLimit;
    private Double threshold;
} 