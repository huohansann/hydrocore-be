package com.siact.module.base.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.siact.common.repository.BaseRepositoryImpl;
import com.siact.module.base.entity.AppConfigEntity;
import com.siact.module.base.mapper.AppConfigMapper;
import com.siact.module.base.repository.AppConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * @author : kzuo
 * @version : 1.0
 * @date : 2026-02-27 17:28
 * @className : AppConfigRepositoryImpl
 * @description : 系统配置数据持久层实现
 */
@RequiredArgsConstructor
@Repository
public class AppConfigRepositoryImpl extends BaseRepositoryImpl<AppConfigMapper, AppConfigEntity> implements AppConfigRepository {
    private final AppConfigMapper mapper;

    @Override
    public AppConfigEntity queryByAcKey(String ackey) {
        LambdaQueryWrapper<AppConfigEntity> wrapper = Wrappers.<AppConfigEntity>lambdaQuery().eq(AppConfigEntity::getAcKey, ackey);
        return mapper.selectOne(wrapper);
    }
}
