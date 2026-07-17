package com.siact.hydrocore.module.system.query;

import com.siact.hydrocore.common.query.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel("菜单查询")
public class SysMenuQuery extends PageQuery {
    @ApiModelProperty(value = "菜单名称")
    private String menuName;

    @ApiModelProperty(value = "父级菜单ID")
    private Long parentId;

    @ApiModelProperty(value = "状态")
    private Integer status;
}
