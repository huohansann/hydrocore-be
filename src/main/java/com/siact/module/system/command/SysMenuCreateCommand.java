package com.siact.module.system.command;

import lombok.Data;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-12-23 14:08
 * @className : SysMenuCreateCommand
 * @description : 系统菜单创建指令对象
 */
@Data
public class SysMenuCreateCommand {
    private Long parentId;
    private String code;
    private String name;
    private Integer sort;
    private Boolean show;
    private Boolean disabled;
    private String target;
}
