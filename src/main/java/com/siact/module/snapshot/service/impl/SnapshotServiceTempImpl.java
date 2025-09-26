package com.siact.module.snapshot.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.module.snapshot.entity.SnapshotTempEntity;
import com.siact.module.snapshot.mapper.SnapshotTempMapper;
import com.siact.module.snapshot.service.SnapshotTempService;
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
public class SnapshotServiceTempImpl extends ServiceImpl<SnapshotTempMapper, SnapshotTempEntity> implements SnapshotTempService {

    @Override
    public List<SnapshotTempEntity> queryByDataCodeInRange(List<String> dataCodeList, String startTime, String endTime) {
        if (ObjectUtils.isEmpty(dataCodeList)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<SnapshotTempEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(SnapshotTempEntity::getDataCode, dataCodeList);
        queryWrapper.ge(ObjectUtils.isNotEmpty(startTime), SnapshotTempEntity::getCreateTime, startTime);
        queryWrapper.le(ObjectUtils.isNotEmpty(endTime), SnapshotTempEntity::getCreateTime, endTime);

        return baseMapper.selectList(queryWrapper);
    }
}