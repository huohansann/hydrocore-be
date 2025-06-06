package com.siact.module.permission.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 分页请求DTO
 *
 * @author example
 */
@Data
@ApiModel(value = "分页请求参数")
public class PageDTO {
    
    @ApiModelProperty(value = "页码", example = "1")
    private Integer pageNum = 1;
    
    @ApiModelProperty(value = "每页数量", example = "10")
    private Integer pageSize = 10;
    
    @ApiModelProperty(value = "姓名", example = "张三")
    private String username;

    @ApiModelProperty(value = "手机号", example = "15188888888")
    private String mobile;

    @ApiModelProperty(value = "部门ID", example = "1")
    private String orgId;

    @ApiModelProperty(value = "关键字", example = "关键字")
    private String keyword;

    @ApiModelProperty(value = "角色ID", example = "1")
    private String roleId;

    @ApiModelProperty(value = "角色名称（模糊匹配）", example = "管理")
    private String roleName;
} 