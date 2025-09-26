package com.siact.module.snapshot.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 快照查询DTO
 *
 * @author Roo
 * @date 2025-09-22
 */
@Data
public class SnapshotQueryDTO {



    @ApiModelProperty(value = "页码", example = "1")
    private Integer pageNum = 1;

    @ApiModelProperty(value = "每页数量", example = "10")
    private Integer pageSize = 10;
}