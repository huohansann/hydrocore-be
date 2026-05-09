package com.siact.module.level.repository.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.siact.common.repository.BaseRepositoryImpl;
import com.siact.module.level.entity.LevelControlConfigEntity;
import com.siact.module.level.mapper.LevelControlConfigMapper;
import com.siact.module.level.repository.LevelControlConfigRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class LevelControlConfigRepositoryImpl
        extends BaseRepositoryImpl<LevelControlConfigMapper, LevelControlConfigEntity>
        implements LevelControlConfigRepository {

    private final LevelControlConfigMapper mapper;

    @Override
    public LevelControlConfigEntity getByDataCode(String dataCode) {
        return mapper.selectOne(
                Wrappers.<LevelControlConfigEntity>lambdaQuery()
                        .eq(LevelControlConfigEntity::getDataCode, dataCode));
    }

    @Override
    public void saveOrUpdate(LevelControlConfigEntity entity) {
        LevelControlConfigEntity existing = getByDataCode(entity.getDataCode());
        if (existing != null) {
            mapper.deleteById(existing.getId());
        }
        entity.setId(null);
        entity.setDeleted(false);
        mapper.insert(entity);
    }
}
