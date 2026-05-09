package com.siact.module.level.repository;

import com.siact.common.repository.BaseRepository;
import com.siact.module.level.entity.LevelControlConfigEntity;

public interface LevelControlConfigRepository extends BaseRepository<LevelControlConfigEntity> {
    LevelControlConfigEntity getByDataCode(String dataCode);

    void saveOrUpdate(LevelControlConfigEntity entity);
}
