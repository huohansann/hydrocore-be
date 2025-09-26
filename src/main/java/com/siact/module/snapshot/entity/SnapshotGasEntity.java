package com.siact.module.snapshot.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 快照实体类
 *
 * @author Roo
 * @date 2025-09-22
 */
@Data
@TableName("snapshot_gas")
public class SnapshotGasEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 炉号
     */
    private String name;

    /**
     * 数字孪生编码
     */
    private String dataCode;


    /**
     * 天然气DCS值
     */
    private BigDecimal gasDcsVal;

    /**
     * 天然气智控(后期算法部门提供接口查询,暂时没有逻辑)
     */
    private BigDecimal gasAlgorithmCalcVal;

    /**
     * 天然气人工值
     */
    private BigDecimal gasManualVal;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private String createTime;
}