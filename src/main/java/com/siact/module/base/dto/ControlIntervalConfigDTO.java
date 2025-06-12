package com.siact.module.base.dto;

import lombok.Data;

@Data
public class ControlIntervalConfigDTO {
    /**
     * 主键
     */
    private Long id;

    /**
     * 测点，温度中有MC1-10
     */
    private String measurePoint;

    /**
     * 孪生dataCode
     */
    private String dataCode;

    /**
     * 测点code，暂时预留一下
     */
    private String pointType;

    /**
     * 上控制值
     */
    private String upControl;

    /**
     * 下控制值
     */
    private String lowControl;

    /**
     * 上告警值
     */
    private String upAlarm;

    /**
     * 下告警值
     */
    private String lowAlarm;

    /**
     * 温度设定值
     */
    private String temperatureSet;

    private String time;
}
