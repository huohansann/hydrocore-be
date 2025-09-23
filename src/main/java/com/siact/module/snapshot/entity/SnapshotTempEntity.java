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
@TableName("snapshot_temperature")
public class SnapshotTempEntity {

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
     * 实际运行值
     */
    private BigDecimal actualVal;

    /**
     * 单点预测值(T20)
     */
    private BigDecimal singlePredictedT20Val;

    /**
     * 单点预测值(T40)
     */
    private BigDecimal singlePredictedT40Val;

    /**
     * 单点预测值(T60)
     */
    private BigDecimal singlePredictedT60Val;

    /**
     * 单点预测值(T80)
     */
    private BigDecimal singlePredictedT80Val;

    /**
     * 单点预测值(T27)
     */
    private BigDecimal singlePredictedT27Val;

    /**
     * 单点预测值(T54)
     */
    private BigDecimal singlePredictedT54Val;

    /**
     * 多步预测值(T20)
     */
    private BigDecimal multiPredictedVal;

    /**
     * 温度设定值(取控制设置-控制区间设置-温度设定值)
     */
    private String tempSetVal;

    /**
     * 预测最大值(后期算法部门提供接口查询,暂时没有逻辑)
     */
    private BigDecimal predictedMaxVal;

    /**
     * 预测最小值(后期算法部门提供接口查询,暂时没有逻辑)
     */
    private BigDecimal predictedMinVal;

    /**
     * 创建时间
     */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private String createTime;
}