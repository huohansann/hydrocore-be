package com.siact.module.base.command;

import com.siact.module.base.enums.AppConfigModuleEnum;
import com.siact.module.base.enums.AppConfigTypeEnum;
import lombok.Data;

/**
 * @author : kzuo
 * @version : 1.0
 * @date : 2026-02-27 16:08
 * @className : AppConfigCreateCommand
 * @description : 系统配置创建指令对象
 */
@Data
public class AppConfigCreateCommand {
    private AppConfigModuleEnum module;
    private String acKey;
    private String acName;
    private AppConfigTypeEnum acType;
    private String acValue;
    private String description;
    private Integer sort;
}
