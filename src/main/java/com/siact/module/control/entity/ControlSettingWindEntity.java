package com.siact.module.control.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("control_setting_wind")
public class ControlSettingWindEntity {
    // 主键id
    private Long id;
    // 炉子编号
    private String number;
    // 助燃风炉子对应的数字孪生insCode
    private String windDataCode;
    // 天然气炉子对应的数字孪生insCode
    private String gasDataCode;
    // 炉子下发风气比对应的点位编码
    private String ratePublishCodes;
    // 风气比人工调整值
    private BigDecimal rateManualVal;
    // 删除标志 0-正常 1-删除
    private Integer deleteFlag;
    // 创建时间
    private Date createTime;
    // 修改时间
    private Date updateTime;
}
