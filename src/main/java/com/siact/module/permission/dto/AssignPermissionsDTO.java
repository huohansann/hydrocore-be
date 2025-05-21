package com.siact.module.permission.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * @desc:
 * @author: wr
 * @create: 2025-05-13 16:30
 */
@Data
@ApiModel(description = "分配权限参数")
public class AssignPermissionsDTO {
    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "角色id")
    private List<Long> roleIds;

    @ApiModelProperty(value = "组织id")
    private List<Long> orgIds;
}