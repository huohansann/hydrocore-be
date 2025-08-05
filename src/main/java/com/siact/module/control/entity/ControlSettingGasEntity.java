package com.siact.module.control.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("control_setting_gas")
public class ControlSettingGasEntity {
    // 主键id
    private Long id;
    // 炉子编号
    private String number;
    // 炉子对应的数字孪生编码
    private String dataCode;
    // 天然气量下发对应的点位编码
    private String gasPublishCodes;
    // 气量智控算法计算值
    private BigDecimal gasAlgorithmCalcVal;
    // 天然气量人工调整值
    private BigDecimal gasManualVal;
    // 是否自动模式(1:是,0:否)
    private Boolean autoState;
    // 删除编制 0-正常 1-删除
    private Integer deleteFlag;
    // 创建时间
    private Date createTime;
    // 修改时间
    private Date updateTime;
}
