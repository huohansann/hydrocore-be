package com.siact.module.permission.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "修改密码")
public class UserModifyPwdDTO {
    @ApiModelProperty("账号")
    private String account;

    @ApiModelProperty("原密码")
    private String oldPwd;

    @ApiModelProperty("新密码")
    private String newPwd;
}
