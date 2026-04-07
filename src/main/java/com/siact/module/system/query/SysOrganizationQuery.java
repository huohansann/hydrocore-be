package com.siact.module.system.query;

import com.siact.common.query.PageQuery;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ApiModel("组织查询")
public class SysOrganizationQuery extends PageQuery {

    @ApiModelProperty(value = "组织名称")
    private String orgName;

    @ApiModelProperty(value = "状态")
    private Integer status;
}
