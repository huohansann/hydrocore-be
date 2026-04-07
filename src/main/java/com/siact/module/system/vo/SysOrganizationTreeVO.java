package com.siact.module.system.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("组织树节点")
public class SysOrganizationTreeVO {

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

    @ApiModelProperty(value = "状态")
    private Boolean status;

    @ApiModelProperty(value = "子组织")
    private List<SysOrganizationTreeVO> children;
}
