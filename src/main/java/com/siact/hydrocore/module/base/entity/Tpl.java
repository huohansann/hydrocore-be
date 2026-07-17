package com.siact.hydrocore.module.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 模板表 实体
 * 
 * @author siact
 */
@Data
@TableName("tpl")
public class Tpl implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
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