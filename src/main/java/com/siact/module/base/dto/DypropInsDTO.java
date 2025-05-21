package com.siact.module.base.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @desc:
 * @author: zhangwentao
 * @create: 2025-04-15 14:21
 */
@Data
@ApiModel(description = "动态属性实例DTO")
public class DypropInsDTO {

    @ApiModelProperty(value = "实例DataCode", position = 1)
    private String insDataCode;

    @ApiModelProperty(value = "属性数字化编码", position = 2)
    private String dataCode;

    @ApiModelProperty(value = "属性编码", position = 3)
    private String propCode;

    @ApiModelProperty(value = "属性模型Code（属性模型短码）", position = 4)
    private String propModelCode;

    @ApiModelProperty(value = "属性名称", position = 5)
    private String propName;
}