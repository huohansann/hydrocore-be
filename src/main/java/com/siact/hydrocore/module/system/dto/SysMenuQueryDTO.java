package com.siact.hydrocore.module.system.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SysMenuQueryDTO {
    private String menuName;
    private Long parentId;
    private Integer status;
}
