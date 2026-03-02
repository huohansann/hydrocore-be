package com.siact.module.base.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.module.base.command.AppConfigCreateCommand;
import com.siact.module.base.convert.AppConfigConvert;
import com.siact.module.base.entity.AppConfigEntity;
import com.siact.module.base.mapper.AppConfigMapper;
import com.siact.module.base.model.AppConfigJsonNode;
import com.siact.module.base.repository.AppConfigRepository;
import com.siact.module.base.service.AppConfigService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

/**
 * @author : kzuo
 * @version : 1.0
 * @date : 2026-02-27 12:00
 * @className : AppConfigServiceImpl
 * @description : 系统配置业务层实现
 */
@Service
@RequiredArgsConstructor
public class AppConfigServiceImpl extends ServiceImpl<AppConfigMapper, AppConfigEntity> implements AppConfigService {
    private final AppConfigConvert convert;
    private final AppConfigRepository repository;

    @Override
    public Boolean create(AppConfigCreateCommand command) {
        AppConfigEntity entity = convert.toEntity(command);
        return this.save(entity);
    }

    @Override
    public AppConfigJsonNode queryValueByAcKey(String ackey) {
        AppConfigEntity entity = repository.queryByAcKey(ackey);
        return ObjectUtils.isNotEmpty(entity) ? AppConfigJsonNode.parse(entity.getAcValue()) : AppConfigJsonNode.of(null);
    }
}
