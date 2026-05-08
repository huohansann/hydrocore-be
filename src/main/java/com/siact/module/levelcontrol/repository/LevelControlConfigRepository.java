package com.siact.module.levelcontrol.repository;

import com.siact.common.repository.BaseRepository;
import com.siact.module.levelcontrol.entity.LevelControlConfigEntity;

public interface LevelControlConfigRepository extends BaseRepository<LevelControlConfigEntity> {
    LevelControlConfigEntity getByDataCode(String dataCode);

    void saveOrUpdate(LevelControlConfigEntity entity);
}
