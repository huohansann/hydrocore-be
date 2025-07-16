package com.siact.module.process.dto;

import lombok.Data;

/**
 * 工艺日志DTO
 */
@Data
public class ProcessLogPageDTO extends ProcessLogQueryDTO {
    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页条数
     */
    private Integer pageSize;
} 