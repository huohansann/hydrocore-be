package com.siact.module.pressure.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 窑压控制参数配置
 */
@Data
@TableName("pressure_control_config")
public class PressureControlConfigEntity {

    /**
     * 主键
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 窑号
     */
    @TableField("kiln_number")
    private String kilnNumber;

    /**
     * 控制模式(AI/PID/MANUAL)
     */
    @TableField("control_mode")
    private String controlMode;

    /**
     * 炉压设定值(Pa)
     */
    @TableField("pressure_sp")
    private String pressureSp;

    /**
     * 闸板开度(%)
     */
    @TableField("damper_opening")
    private String damperOpening;

    /**
     * 开度上限(%)
     */
    @TableField("opening_max")
    private String openingMax;

    /**
     * 开度下限(%)
     */
    @TableField("opening_min")
    private String openingMin;

    /**
     * 报警上限(Pa)
     */
    @TableField("alarm_high")
    private String alarmHigh;

    /**
     * 报警下限(Pa)
     */
    @TableField("alarm_low")
    private String alarmLow;

    /**
     * 状态：1=启用，0=停用
     */
    @TableField("status")
    private Integer status;

    /**
     * 创建者
     */
    @TableField("create_by")
    private String createBy;

    /**
     * 创建时间
     */
    @TableField("create_time")
    private Date createTime;

    /**
     * 更新者
     */
    @TableField("update_by")
    private String updateBy;

    /**
     * 更新时间
     */
    @TableField("update_time")
    private Date updateTime;
}
