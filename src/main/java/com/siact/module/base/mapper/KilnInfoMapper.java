package com.siact.module.base.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.siact.module.base.entity.KilnInfoEntity;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 炉子基本信息配置 Mapper
 */
@Mapper
public interface KilnInfoMapper extends BaseMapper<KilnInfoEntity> {
    void updateDistributeBatch(List<KilnInfoEntity> list);

    void updateGasFlowBatch(List<KilnInfoEntity> list);

    void updateWindDisBatch(List<KilnInfoEntity> list);

}