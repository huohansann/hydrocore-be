package com.siact.module.level.service.impl;

import com.siact.module.level.entity.LevelAlgorithmResultEntity;
import com.siact.module.level.repository.LevelAlgorithmResultRepository;
import com.siact.module.level.service.LevelAlgorithmResultService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class LevelAlgorithmResultServiceImpl implements LevelAlgorithmResultService {

    private final LevelAlgorithmResultRepository repository;

    @Override
    public LevelAlgorithmResultEntity getResult(String dataCode) {
        if (dataCode == null) return null;
        return repository.getByDataCode(dataCode);
    }
}
