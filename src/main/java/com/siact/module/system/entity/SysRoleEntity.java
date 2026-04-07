package com.siact.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sys_role_new")
public class SysRoleEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String roleName;

    private String roleCode;

    private String description;

    private Integer sort;

    private Boolean status;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;

    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    @TableLogic
    private Boolean deleted;
}
