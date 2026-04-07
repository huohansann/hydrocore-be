package com.siact.module.system.command;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@ApiModel("编辑组织")
public class SysOrganizationUpdateCommand {

    @NotNull(message = "组织ID不能为空")
    @ApiModelProperty(value = "组织ID")
    private Long id;

    @ApiModelProperty(value = "父组织ID")
    private Long parentId;

    @ApiModelProperty(value = "组织名称")
    private String orgName;

    @ApiModelProperty(value = "组织编码")
    private String orgCode;

    @ApiModelProperty(value = "排序序号")
    private Integer sort;

    @ApiModelProperty(value = "状态：true=启用，false=停用")
    private Boolean status;
}
