package com.siact.module.algorithm.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.siact.common.repository.BaseRepositoryImpl;
import com.siact.module.algorithm.mapper.TemperaturePredictMapper;
import com.siact.module.algorithm.entity.TemperaturePredictEntity;
import com.siact.module.algorithm.repository.TemperaturePredictRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class TemperaturePredictRepositoryImpl extends BaseRepositoryImpl<TemperaturePredictMapper, TemperaturePredictEntity> implements TemperaturePredictRepository {
    private final TemperaturePredictMapper mapper;

    @Override
    public List<TemperaturePredictEntity> queryByPropCodesAndTimeRange(List<String> propCodes, String startTime, String endTime) {
        if (CollectionUtils.isEmpty(propCodes)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<TemperaturePredictEntity> wrapper = Wrappers.<TemperaturePredictEntity>lambdaQuery()
                .in(TemperaturePredictEntity::getPropCode, propCodes)
                .ge(TemperaturePredictEntity::getTime, startTime)
                .le(TemperaturePredictEntity::getTime, endTime)
                .orderByAsc(TemperaturePredictEntity::getTime);
        return mapper.selectList(wrapper);
    }
}
