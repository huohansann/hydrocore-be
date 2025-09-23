package com.siact.module.snapshot.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.module.snapshot.entity.SnapshotGasEntity;

import java.util.List;

/**
 * 快照服务接口
 *
 * @author Roo
 * @date 2025-09-22
 */
public interface SnapshotGasService extends IService<SnapshotGasEntity> {

    List<SnapshotGasEntity> queryByDataCodeInRange(List<String> gasDataCodeList, String startTime, String endTime);
}