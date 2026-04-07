package com.siact.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

@Data
@TableName("sys_user_new")
public class SysUserEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String account;

    private String username;

    private String password;

    private String email;

    private String phone;

    private String avatar;

    private Long orgId;

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
