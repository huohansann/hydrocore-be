package com.siact.module.device.repository.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.siact.module.device.dto.DeviceMappingQueryDTO;
import com.siact.module.device.entity.DeviceMappingEntity;
import com.siact.module.device.mapper.DeviceMappingMapper;
import com.siact.module.device.repository.DeviceMappingRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Repository
public class DeviceMappingRepositoryImpl implements DeviceMappingRepository {
    private final DeviceMappingMapper mapper;

    @Override
    public Page<DeviceMappingEntity> queryList(DeviceMappingQueryDTO queryDTO, Page<DeviceMappingEntity> page) {
        return mapper.selectPage(page, buildQueryWrapper(queryDTO));
    }

    @Override
    public List<DeviceMappingEntity> queryList(DeviceMappingQueryDTO queryDTO) {
        return mapper.selectList(buildQueryWrapper(queryDTO));
    }

    @Override
    public boolean existsByPointName(String pointName) {
        return mapper.selectCount(Wrappers.<DeviceMappingEntity>lambdaQuery()
                .eq(DeviceMappingEntity::getPointName, pointName)) > 0;
    }

    @Override
    public boolean existsByPointNameExcludeId(String pointName, Long excludeId) {
        return mapper.selectCount(Wrappers.<DeviceMappingEntity>lambdaQuery()
                .eq(DeviceMappingEntity::getPointName, pointName)
                .ne(DeviceMappingEntity::getId, excludeId)) > 0;
    }

    @Override
    public boolean existsByItemId(String itemId) {
        return mapper.selectCount(Wrappers.<DeviceMappingEntity>lambdaQuery()
                .eq(DeviceMappingEntity::getItemId, itemId)) > 0;
    }

    @Override
    public boolean existsByItemIdExcludeId(String itemId, Long excludeId) {
        return mapper.selectCount(Wrappers.<DeviceMappingEntity>lambdaQuery()
                .eq(DeviceMappingEntity::getItemId, itemId)
                .ne(DeviceMappingEntity::getId, excludeId)) > 0;
    }

    @Override
    public boolean existsByPropCode(String propCode) {
        return mapper.selectCount(Wrappers.<DeviceMappingEntity>lambdaQuery()
                .eq(DeviceMappingEntity::getPropCode, propCode)) > 0;
    }

    @Override
    public boolean existsByPropCodeExcludeId(String propCode, Long excludeId) {
        return mapper.selectCount(Wrappers.<DeviceMappingEntity>lambdaQuery()
                .eq(DeviceMappingEntity::getPropCode, propCode)
                .ne(DeviceMappingEntity::getId, excludeId)) > 0;
    }

    @Override
    public DeviceMappingEntity findByItemId(String itemId) {
        return mapper.selectOne(Wrappers.<DeviceMappingEntity>lambdaQuery()
                .eq(DeviceMappingEntity::getItemId, itemId));
    }

    @Override
    public DeviceMappingEntity findByPropCode(String propCode) {
        return mapper.selectOne(Wrappers.<DeviceMappingEntity>lambdaQuery()
                .eq(DeviceMappingEntity::getPropCode, propCode));
    }

    @Override
    public DeviceMappingEntity findByPropName(String propName) {
        return mapper.selectOne(Wrappers.<DeviceMappingEntity>lambdaQuery()
                .eq(DeviceMappingEntity::getPropName, propName));
    }

    private LambdaQueryWrapper<DeviceMappingEntity> buildQueryWrapper(DeviceMappingQueryDTO queryDTO) {
        return Wrappers.<DeviceMappingEntity>lambdaQuery()
                .like(StringUtils.isNotBlank(queryDTO.getPointName()), DeviceMappingEntity::getPointName, queryDTO.getPointName())
                .eq(StringUtils.isNotBlank(queryDTO.getItemId()), DeviceMappingEntity::getItemId, queryDTO.getItemId())
                .eq(StringUtils.isNotBlank(queryDTO.getPropCode()), DeviceMappingEntity::getPropCode, queryDTO.getPropCode())
                .like(StringUtils.isNotBlank(queryDTO.getPropName()), DeviceMappingEntity::getPropName, queryDTO.getPropName())
                .eq(StringUtils.isNotBlank(queryDTO.getDeviceCode()), DeviceMappingEntity::getDeviceCode, queryDTO.getDeviceCode())
                .like(StringUtils.isNotBlank(queryDTO.getDeviceName()), DeviceMappingEntity::getDeviceName, queryDTO.getDeviceName())
                .orderByDesc(DeviceMappingEntity::getUpdateTime);
    }

    @Override
    public List<String> findPropCodesByConditions(List<String> itemIds, String propName, List<String> deviceCodes) {
        boolean hasItemId = itemIds != null && !itemIds.isEmpty();
        boolean hasPropName = StringUtils.isNotBlank(propName);
        boolean hasDeviceCode = deviceCodes != null && !deviceCodes.isEmpty();

        // 无筛选条件时返回所有 propCode
        if (!hasItemId && !hasPropName && !hasDeviceCode) {
            return mapper.selectList(Wrappers.<DeviceMappingEntity>lambdaQuery()
                    .select(DeviceMappingEntity::getPropCode)
                    .isNotNull(DeviceMappingEntity::getPropCode))
                    .stream()
                    .map(DeviceMappingEntity::getPropCode)
                    .filter(StringUtils::isNotBlank)
                    .distinct()
                    .collect(Collectors.toList());
        }

        LambdaQueryWrapper<DeviceMappingEntity> wrapper = Wrappers.<DeviceMappingEntity>lambdaQuery();
        wrapper.and(w -> {
            if (hasItemId) {
                w.or().in(DeviceMappingEntity::getItemId, itemIds);
            }
            if (hasPropName) {
                w.or().like(DeviceMappingEntity::getPropName, propName);
            }
            if (hasDeviceCode) {
                w.or().in(DeviceMappingEntity::getDeviceCode, deviceCodes);
            }
        });
        wrapper.select(DeviceMappingEntity::getPropCode);

        return mapper.selectList(wrapper).stream()
                .map(DeviceMappingEntity::getPropCode)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<DeviceMappingEntity> findByPropCodes(List<String> propCodes) {
        if (propCodes == null || propCodes.isEmpty()) {
            return Collections.emptyList();
        }
        return mapper.selectList(Wrappers.<DeviceMappingEntity>lambdaQuery()
                .in(DeviceMappingEntity::getPropCode, propCodes));
    }

    @Override
    public List<String> findAllItemIds() {
        return mapper.selectList(Wrappers.<DeviceMappingEntity>lambdaQuery()
                .select(DeviceMappingEntity::getItemId)
                .isNotNull(DeviceMappingEntity::getItemId)
                .orderByAsc(DeviceMappingEntity::getItemId))
                .stream()
                .map(DeviceMappingEntity::getItemId)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<String> findAllDeviceCodes() {
        return mapper.selectList(Wrappers.<DeviceMappingEntity>lambdaQuery()
                .select(DeviceMappingEntity::getDeviceCode)
                .isNotNull(DeviceMappingEntity::getDeviceCode)
                .orderByAsc(DeviceMappingEntity::getDeviceCode))
                .stream()
                .map(DeviceMappingEntity::getDeviceCode)
                .distinct()
                .collect(Collectors.toList());
    }

    @Override
    public List<DeviceMappingEntity> findDistinctDeviceNames() {
        return mapper.selectList(Wrappers.<DeviceMappingEntity>lambdaQuery()
                .select(DeviceMappingEntity::getDeviceName, DeviceMappingEntity::getDeviceCode)
                .isNotNull(DeviceMappingEntity::getDeviceName)
                .groupBy(DeviceMappingEntity::getDeviceName, DeviceMappingEntity::getDeviceCode)
                .orderByAsc(DeviceMappingEntity::getDeviceName));
    }
}
