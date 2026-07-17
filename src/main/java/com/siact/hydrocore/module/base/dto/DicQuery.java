package com.siact.hydrocore.module.base.dto;

import lombok.Data;

/**
 * 字典表 查询对象
 * 
 * @author siact
 */
@Data
public class DicQuery {
    
    /** 类型 */
    private String type;

    /** 名称 */
    private String name;

    /** 编码 */
    private String code;

    /** 状态 */
    private String status;

    /** 标签 */
    private String tag;
    
    /** 开始时间 */
    private String beginTime;
    
    /** 结束时间 */
    private String endTime;
    
    /** 分页大小 */
    private Integer pageSize;
    
    /** 当前页数 */
    private Integer pageNum;
    
    /** 排序列 */
    private String orderByColumn;
    
    /** 排序的方向 "desc" 或者 "asc" */
    private String isAsc;
} 