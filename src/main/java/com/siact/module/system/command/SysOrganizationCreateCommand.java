package com.siact.module.system.command;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel("新增组织")
public class SysOrganizationCreateCommand {

    @ApiModelProperty(value = "父组织ID，顶级为0")
    private Long parentId = 0L;

    @NotBlank(message = "组织名称不能为空")
    @ApiModelProperty(value = "组织名称")
    private String orgName;

    @NotBlank(message = "组织编码不能为空")
    @ApiModelProperty(value = "组织编码")
    private String orgCode;

    @ApiModelProperty(value = "排序序号")
    private Integer sort = 0;
}
