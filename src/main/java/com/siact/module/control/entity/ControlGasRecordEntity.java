package com.siact.module.control.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-05 10:42
 * @className : ControlGasRecordEntity
 * @description : 天然气运行值记录实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@TableName("control_gas_record")
public class ControlGasRecordEntity {
    private @TableId(type = IdType.ASSIGN_ID) Long id;
    private String code;
    private BigDecimal dcs;
    private Boolean status;
    private Timestamp time;
}
