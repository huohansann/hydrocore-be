package com.siact.module.control.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.siact.common.repository.BaseRepositoryImpl;
import com.siact.module.control.entity.ControlGasRecordEntity;
import com.siact.module.control.mapper.ControlGasRecordMapper;
import com.siact.module.control.repository.ControlGasRecordRepository;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-05 11:08
 * @className : ControlGasRecordRepositoryImpl
 * @description : 天然气 dcs 运行值记录数据持久层实现
 */
@AllArgsConstructor
@Repository
public class ControlGasRecordRepositoryImpl extends BaseRepositoryImpl<ControlGasRecordMapper, ControlGasRecordEntity> implements ControlGasRecordRepository {
    private final ControlGasRecordMapper mapper;

    @Override
    public Map<String, ControlGasRecordEntity> queryWithLastTime() {
        LambdaQueryWrapper<ControlGasRecordEntity> wrapper = Wrappers.<ControlGasRecordEntity>lambdaQuery().select(ControlGasRecordEntity::getTime).orderByDesc(ControlGasRecordEntity::getTime).last("limit 1");
        ControlGasRecordEntity entity = mapper.selectOne(wrapper);
        if (ObjectUtils.isEmpty(entity) || ObjectUtils.isEmpty(entity.getTime())) return Collections.emptyMap();
        Date time = entity.getTime();

        List<ControlGasRecordEntity> entities = mapper.selectList(Wrappers.<ControlGasRecordEntity>lambdaQuery().eq(ControlGasRecordEntity::getTime, time));
        return entities.stream().collect(Collectors.toMap(ControlGasRecordEntity::getCode, l -> l, (v1, v2) -> v1));
    }
}
