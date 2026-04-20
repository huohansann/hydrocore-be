package com.siact.module.device.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.siact.module.device.dto.DeviceMappingQueryDTO;
import com.siact.module.device.entity.DeviceMappingEntity;

import java.util.List;

public interface DeviceMappingRepository {

    Page<DeviceMappingEntity> queryList(DeviceMappingQueryDTO queryDTO, Page<DeviceMappingEntity> page);

    List<DeviceMappingEntity> queryList(DeviceMappingQueryDTO queryDTO);

    boolean existsByPointName(String pointName);

    boolean existsByPointNameExcludeId(String pointName, Long excludeId);

    boolean existsByItemId(String itemId);

    boolean existsByItemIdExcludeId(String itemId, Long excludeId);

    boolean existsByPropCode(String propCode);

    boolean existsByPropCodeExcludeId(String propCode, Long excludeId);

    DeviceMappingEntity findByItemId(String itemId);

    DeviceMappingEntity findByPropCode(String propCode);

    List<String> findPropCodesByConditions(List<String> itemIds, String propName, List<String> deviceCodes);

    List<DeviceMappingEntity> findByPropCodes(List<String> propCodes);

    List<String> findAllItemIds();

    List<String> findAllDeviceCodes();

    List<DeviceMappingEntity> findDistinctDeviceNames();
}
