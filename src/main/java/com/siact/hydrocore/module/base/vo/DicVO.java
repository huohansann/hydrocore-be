package com.siact.hydrocore.module.base.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 字典表 视图对象
 * 
 * @author siact
 */
@Data
public class DicVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
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
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    
    /** 子字典列表 */
    private List<DicVO> children;
} 