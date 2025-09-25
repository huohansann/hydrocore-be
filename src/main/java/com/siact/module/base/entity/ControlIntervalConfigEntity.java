package com.siact.module.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 控制区间实体
 *
 * @author wr
 */
@Data
@TableName("control_interval_config")
public class ControlIntervalConfigEntity {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 测点，温度中有MC1-10
     */
    @TableField("measure_point")
    private String measurePoint;

    /**
     * 孪生dataCode
     */
    @TableField("data_code")
    private String dataCode;

    /**
     * 测点类型，暂时预留一下
     */
    @TableField("point_type")
    private String pointType;

    /**
     * 上控制值
     */
    @TableField("up_control")
    private String upControl;

    /**
     * 下控制值
     */
    @TableField("low_control")
    private String lowControl;

    /**
     * 上告警值
     */
    @TableField("up_alarm")
    private String upAlarm;

    /**
     * 下告警值
     */
    @TableField("low_alarm")
    private String lowAlarm;

    @TableField("temperature_set")
    private String temperatureSet;

    /**
     * 生效开始日期
     */
    @TableField("start_time")
    private String startTime;

    /**
     * 生效结束时间
     */
    @TableField("end_time")
    private String endTime;

    /**
     * 删除状态，0-正常，1-删除
     */
    @TableField("delete_flag")
    private Boolean deleteFlag;
}
