package com.siact.module.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.module.base.command.AppConfigCreateCommand;
import com.siact.module.base.entity.AppConfigEntity;
import com.siact.module.base.model.AppConfigJsonNode;

/**
 * @author : kzuo
 * @version : 1.0
 * @date : 2026-02-27 12:00
 * @className : AppConfigService
 * @description : 系统配置业务层
 */
public interface AppConfigService extends IService<AppConfigEntity> {

    Boolean create(AppConfigCreateCommand command);

    AppConfigJsonNode queryValueByAcKey(String ackey);
}
