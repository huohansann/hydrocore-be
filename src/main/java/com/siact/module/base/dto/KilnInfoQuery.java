package com.siact.module.base.dto;

import lombok.Data;

/**
 * 炉子基本信息配置 查询对象
 */
@Data
public class KilnInfoQuery {
    private String number; // 炉子编号
    private String code;   // 炉子编码
    private Boolean state; // 状态
    private Integer pageSize;    // 分页大小
    private Integer pageNum;     // 当前页数
} 