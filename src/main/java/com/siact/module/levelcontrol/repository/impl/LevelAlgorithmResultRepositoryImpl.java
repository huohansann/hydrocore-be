package com.siact.module.levelcontrol.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.siact.common.repository.BaseRepositoryImpl;
import com.siact.module.levelcontrol.entity.LevelAlgorithmResultEntity;
import com.siact.module.levelcontrol.mapper.LevelAlgorithmResultMapper;
import com.siact.module.levelcontrol.repository.LevelAlgorithmResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class LevelAlgorithmResultRepositoryImpl
        extends BaseRepositoryImpl<LevelAlgorithmResultMapper, LevelAlgorithmResultEntity>
        implements LevelAlgorithmResultRepository {

    private final LevelAlgorithmResultMapper mapper;

    @Override
    public LevelAlgorithmResultEntity getByDataCode(String dataCode) {
        return mapper.selectOne(
                Wrappers.<LevelAlgorithmResultEntity>lambdaQuery()
                        .eq(LevelAlgorithmResultEntity::getDataCode, dataCode));
    }

    @Override
    public void saveOrUpdate(LevelAlgorithmResultEntity entity) {
        LevelAlgorithmResultEntity existing = getByDataCode(entity.getDataCode());
        if (existing != null) {
            entity.setId(existing.getId());
            mapper.updateById(entity);
        } else {
            mapper.insert(entity);
        }
    }
}
