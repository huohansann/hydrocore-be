package com.siact.module.permission.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class UserTokenDTO {
    @ApiModelProperty(value = "用户ID")
    private Long id;
    @ApiModelProperty(value = "用户名")
    private String username;
    @ApiModelProperty(value = "账号")
    private String account;
    @ApiModelProperty(value = "状态（1正常 0停用）")
    private Boolean status;
    @ApiModelProperty(value = "角色ID列表")
    private List<Long> roleIds;
    @ApiModelProperty(value = "组织ID列表")
    private List<Long> orgIds;
}
