package com.siact.module.level.service;

import com.siact.module.level.entity.LevelAlgorithmResultEntity;

public interface LevelAlgorithmResultService {
    LevelAlgorithmResultEntity getResult(String dataCode);
}