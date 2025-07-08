package com.siact.module.base.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 炉子基本信息配置 查询对象
 */
@Data
public class KilnInfoQuery {
    @ApiModelProperty(value = "炉子编号")
    private String number; // 炉子编号
    @ApiModelProperty(value = "炉子数字孪生编码")
    private String dataCode;   // 炉子编码
    @ApiModelProperty(value = "状态")
    private Boolean state; // 状态
//    private Integer pageSize;    // 分页大小
//    private Integer pageNum;     // 当前页数
} 