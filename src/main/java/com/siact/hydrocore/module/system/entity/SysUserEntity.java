package com.siact.hydrocore.module.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.sql.Timestamp;

@Data
@TableName("sys_user")
public class SysUserEntity {

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private String account;

    private String username;

    private String password;

    private String email;

    @TableField("mobile")
    private String phone;

    private String avatar;

    private Long orgId;

    private Boolean status;

    @TableField(value = "create_by", fill = FieldFill.INSERT)
    private String createBy;

    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Timestamp createTime;

    @TableField(value = "update_by", fill = FieldFill.INSERT_UPDATE)
    private String updateBy;

    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private Timestamp updateTime;

    @TableLogic
    private Boolean deleted;
}
