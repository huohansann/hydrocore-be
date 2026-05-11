package com.siact.module.algorithm.repository;

import com.siact.common.repository.BaseRepository;
import com.siact.module.algorithm.entity.TemperaturePredictEntity;

import java.util.List;

public interface TemperaturePredictRepository extends BaseRepository<TemperaturePredictEntity> {

    List<TemperaturePredictEntity> queryByPropCodesAndTimeRange(List<String> propCodes, String startTime, String endTime);
}
