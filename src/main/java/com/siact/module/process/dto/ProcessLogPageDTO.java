package com.siact.module.process.dto;

import lombok.Data;

import java.util.Date;

/**
 * 工艺日志DTO
 */
@Data
public class ProcessLogPageDTO extends ProcessLogDTO {
    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页条数
     */
    private Integer pageSize;
} 