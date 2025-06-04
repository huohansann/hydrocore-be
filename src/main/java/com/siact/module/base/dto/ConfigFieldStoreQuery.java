package com.siact.module.base.dto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 配置字段存储 查询对象
 *
 * @author siact
 */
@Data
@Api("配置字段存储 查询对象")
public class ConfigFieldStoreQuery {
    /** 字段键 */
    @ApiModelProperty(value = "字段键", example = "wind_gas_ratio")
    private String fieldKey;

    @ApiModelProperty(value = "是否模糊查询", example = "true")
    private Boolean isLike;

} 