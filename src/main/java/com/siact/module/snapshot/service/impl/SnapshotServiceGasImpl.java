package com.siact.module.snapshot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.module.snapshot.entity.SnapshotGasEntity;
import com.siact.module.snapshot.mapper.SnapshotGasMapper;
import com.siact.module.snapshot.service.SnapshotGasService;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 快照服务实现类
 *
 * @author Roo
 * @date 2025-09-22
 */
@Service
public class SnapshotServiceGasImpl extends ServiceImpl<SnapshotGasMapper, SnapshotGasEntity> implements SnapshotGasService {

    @Override
    public List<SnapshotGasEntity> queryByDataCodeInRange(List<String> gasDataCodeList, String startTime, String endTime) {

        if (ObjectUtils.isEmpty(gasDataCodeList)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<SnapshotGasEntity> queryWrapper = new LambdaQueryWrapper<>();

        queryWrapper.in(SnapshotGasEntity::getDataCode, gasDataCodeList);
        queryWrapper.ge(ObjectUtils.isNotEmpty(startTime), SnapshotGasEntity::getCreateTime, startTime);
        queryWrapper.le(ObjectUtils.isNotEmpty(endTime), SnapshotGasEntity::getCreateTime, endTime);
        return baseMapper.selectList(queryWrapper);
    }
}