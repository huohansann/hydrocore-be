package com.siact.module.system.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-12-19 9:53
 * @className : SysMenuQueryDTO
 * @description : 系统菜单传输对象(数据库查询)
 */
@Getter
@Setter
public class SysMenuQueryDTO {
    private Long parentId;
    private String code;
    private String label;
}
