package com.siact.hydrocore.module.base.dto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**

 *
 * @author siact
 */
@Data
@Api("Config field store query")
public class ConfigFieldStoreQuery {

    @ApiModelProperty(value = "Field key", example = "sample_ratio")
    private String fieldKey;

    @ApiModelProperty(value = "Whether to use fuzzy query", example = "true")
    private Boolean isLike;

} 
