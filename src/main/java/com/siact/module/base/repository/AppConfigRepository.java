package com.siact.module.base.repository;

import com.siact.common.repository.BaseRepository;
import com.siact.module.base.entity.AppConfigEntity;

/**
 * @author : kzuo
 * @version : 1.0
 * @date : 2026-02-27 17:25
 * @className : AppConfigRepository
 * @description : 系统配置数据持久层
 */
public interface AppConfigRepository extends BaseRepository<AppConfigEntity> {

    AppConfigEntity queryByAcKey(String ackey);

}
