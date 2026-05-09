package com.siact.module.level.repository;

import com.siact.common.repository.BaseRepository;
import com.siact.module.level.entity.LevelAlgorithmResultEntity;

public interface LevelAlgorithmResultRepository extends BaseRepository<LevelAlgorithmResultEntity> {
    LevelAlgorithmResultEntity getByDataCode(String dataCode);

    void saveOrUpdate(LevelAlgorithmResultEntity entity);
}
