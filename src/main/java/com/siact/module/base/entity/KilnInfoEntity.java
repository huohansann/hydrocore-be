package com.siact.module.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 炉子基本信息配置
 */
@Data
@TableName("kiln_info")
public class KilnInfoEntity {
    /**
     * 主键id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 炉子编号
     */
    @TableField("number")
    private String number;

    /**
     * 炉子编码，数字孪生编码
     */
    @TableField("code")
    private String code;

    /**
     * 状态： 是否自动状态（1是 0否）
     */
    @TableField("state")
    private Boolean state;

    /**
     * 天然气计算值
     */
    @TableField("gas_calc")
    private BigDecimal gasCalc;

    /**
     * 天然气设定值
     */
    @TableField("gas_val")
    private BigDecimal gasVal;

    /**
     * 助燃风设定值
     */
    @TableField("wind_val")
    private BigDecimal windVal;

    /**
     * 风气比
     */
    @TableField("wind_gas_rate")
    private BigDecimal windGasRate;

    /**
     * 天然气流量设定值上限
     */
    @TableField("gas_val_up")
    private BigDecimal gasValUp;

    /**
     * 天然气流量设定值下限
     */
    @TableField("gas_val_low")
    private BigDecimal gasValLow;

    /**
     * 气量分布上限
     */
    @TableField("wind_dis_up")
    private BigDecimal windDisUp;

    /**
     * 气量分布下限
     */
    @TableField("wind_dis_low")
    private BigDecimal windDisLow;

}
