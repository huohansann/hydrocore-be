package com.siact.hydrocore.module.base.dto;

import lombok.Data;

@Data
public class Load {

    /**
     * 重载次数，15min为间隔单位
     */
    private Integer heavyLoadNum;
    /**
     * 轻载次数
     */
    private Integer lightLoadNum;
    /**
     * 过载次数
     */
    private Integer overLoadNum;
    /**
     * 重载时间（小时数）
     */
    private Double heavyLoad;
    /**
     * 轻载时间（小时数）
     */
    private Double lightLoad;
    /**
     * 过载时间（小时数）
     */
    private Double overLoad;
}
