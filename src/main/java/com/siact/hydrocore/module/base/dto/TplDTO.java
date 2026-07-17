package com.siact.hydrocore.module.base.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 模板表 数据传输对象
 * 
 * @author siact
 */
@Data
public class TplDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 模板名称 */
    private String tplName;

    /** 模板编码 */
    private String tplCode;

    /** 模板内容 */
    private String tplContent;

    /** 模板类型 */
    private String tplType;

    /** 模板描述 */
    private String tplDescribe;

    /** 创建时间 */
    private String createTime;
} 