package com.siact.module.control.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-11-27 15:50
 * @className : GasValueEntity
 * @description : 天然气运行值实体类
 */
@TableName("gas_value")
@Data
@Builder
public class GasValueEntity {
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    private String time;
    private String dataKey;
    private String dataCode;
    private BigDecimal gasValue;
}
