package com.siact.hydrocore.module.base.dto;

import lombok.Data;

/**
 * 模板表 查询对象
 * 
 * @author siact
 */
@Data
public class TplQuery {
    
    /** 模板名称 */
    private String tplName;

    /** 模板编码 */
    private String tplCode;

    /** 模板类型 */
    private String tplType;
    
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