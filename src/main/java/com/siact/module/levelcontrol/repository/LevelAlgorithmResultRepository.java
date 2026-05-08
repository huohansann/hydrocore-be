package com.siact.module.levelcontrol.repository;

import com.siact.common.repository.BaseRepository;
import com.siact.module.levelcontrol.entity.LevelAlgorithmResultEntity;

public interface LevelAlgorithmResultRepository extends BaseRepository<LevelAlgorithmResultEntity> {
    LevelAlgorithmResultEntity getByDataCode(String dataCode);

    void saveOrUpdate(LevelAlgorithmResultEntity entity);
}
