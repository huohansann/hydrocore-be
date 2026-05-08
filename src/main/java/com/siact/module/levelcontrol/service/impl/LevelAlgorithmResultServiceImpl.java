package com.siact.module.levelcontrol.service.impl;

import com.siact.module.levelcontrol.entity.LevelAlgorithmResultEntity;
import com.siact.module.levelcontrol.repository.LevelAlgorithmResultRepository;
import com.siact.module.levelcontrol.service.LevelAlgorithmResultService;
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
