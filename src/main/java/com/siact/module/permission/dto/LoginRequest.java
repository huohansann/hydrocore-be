package com.siact.module.permission.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @desc:
 * @author: wr
 * @create: 2025-05-10 16:46
 */
@ApiModel(description = "登录请求参数")
@Data
public class LoginRequest {
    @ApiModelProperty(value = "密码", required = true)
    private String password;

    @ApiModelProperty(value = "用户名", required = true)
    private String username;
}