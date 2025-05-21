package com.siact.module.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 字典表 实体
 * 
 * @author siact
 */
@Data
@TableName("dic")
public class Dic implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 类型 */
    private String type;

    /** 名称 */
    private String name;

    /** 编码 */
    private String code;

    /** 值 */
    private String value;

    /** 单位 */
    private String unit;

    /** 标签 */
    private String tag;

    /** 父ID */
    private Long parentId;

    /** 精度 */
    private Integer accuracy;

    /** 排序 */
    private Integer sort;

    /** 状态 */
    private String status;

    /** 描述信息 */
    private String msg;

    /** 计算公式 */
    private String formula;

    /** 创建时间 */
    private String createTime;
} 