package com.siact.module.base.convert;

import com.siact.module.base.command.AppConfigCreateCommand;
import com.siact.module.base.entity.AppConfigEntity;
import org.mapstruct.Mapper;

/**
 * @author : kzuo
 * @version : 1.0
 * @date : 2026-02-27 16:13
 * @className : AppConfigConvert
 * @description : 系统配置对象转换器
 */

@Mapper(componentModel = "spring")
public interface AppConfigConvert {
    AppConfigEntity toEntity(AppConfigCreateCommand command);
}
