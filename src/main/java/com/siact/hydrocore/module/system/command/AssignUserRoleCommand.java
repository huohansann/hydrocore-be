package com.siact.hydrocore.module.system.command;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("分配用户角色")
public class AssignUserRoleCommand {

    @ApiModelProperty(value = "角色ID列表")
    private List<Long> roleIds;
}
