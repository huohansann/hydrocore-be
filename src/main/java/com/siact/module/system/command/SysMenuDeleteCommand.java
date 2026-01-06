package com.siact.module.system.command;

import com.siact.module.system.enums.MenuDeleteType;
import lombok.Data;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-12-23 19:03
 * @className : SysMenuDeleteCommand
 * @description : 系统菜单删除指令对象
 */
@Data
public class SysMenuDeleteCommand {
    private MenuDeleteType type;
    private String value;
}
