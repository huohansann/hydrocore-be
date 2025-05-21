package com.siact.module.permission.entity;

import cn.afterturn.easypoi.excel.annotation.Excel;
import lombok.Data;

/**
 * 用户实体类
 *
 * @author example
 */
@Data
public class UserExcelEntity {
    
    /**
     * 用户名
     */
    @Excel(name = "用户名")
    private String username;

    /**
     * 手机号码
     */
    @Excel(name = "手机号码")
    private String mobile;
    
    /**
     * 密码
     */
    @Excel(name = "密码")
    private String password;
    
    /**
     * 性别（0未知 1男 2女）
     */
    @Excel(name = "性别")
    private String gender;

    /**
     * 账号
     */
    @Excel(name = "账号")
    private String account;
    
    /**
     * 邮箱
     */
    @Excel(name = "邮箱")
    private String email;

    /**
     * 状态（1正常 0停用）
     */
    @Excel(name = "状态")
    private String status;
} 