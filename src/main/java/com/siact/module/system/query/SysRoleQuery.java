package com.siact.module.system.query;

import com.siact.common.query.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel("角色查询")
public class SysRoleQuery extends PageQuery {
    @ApiModelProperty(value = "角色名称")
    private String roleName;
    @ApiModelProperty(value = "状态")
    private Integer status;
}
