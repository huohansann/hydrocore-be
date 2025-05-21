package com.siact.module.permission.vo;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @desc:
 * @author: wr
 * @create: 2025-05-15 10:46
 */
@Data
@ApiModel(description = "职位信息")
public class PositionVO {
    @ApiModelProperty(value = "职位id")
    private Long id;

    @ApiModelProperty(value = "职位名称")
    private String name;
}