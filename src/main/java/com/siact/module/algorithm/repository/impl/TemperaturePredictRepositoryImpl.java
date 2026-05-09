package com.siact.module.algorithm.repository.impl;

import com.siact.common.repository.BaseRepositoryImpl;
import com.siact.module.algorithm.mapper.TemperaturePredictMapper;
import com.siact.module.algorithm.entity.TemperaturePredictEntity;
import com.siact.module.algorithm.repository.TemperaturePredictRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class TemperaturePredictRepositoryImpl extends BaseRepositoryImpl<TemperaturePredictMapper, TemperaturePredictEntity> implements TemperaturePredictRepository {
    private final TemperaturePredictMapper mapper;
}