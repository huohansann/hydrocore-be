package com.siact.module.system.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MenuTypeEnum {

    DIRECTORY(1, "目录"),
    MENU(2, "菜单");

    private final int code;
    private final String description;
}
