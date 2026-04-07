package com.siact.module.system.query;

import com.siact.common.query.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel("用户查询")
public class SysUserQuery extends PageQuery {

    @ApiModelProperty(value = "账号")
    private String account;

    @ApiModelProperty(value = "用户名")
    private String username;

    @ApiModelProperty(value = "所属组织ID")
    private Long orgId;

    @ApiModelProperty(value = "状态")
    private Integer status;
}
