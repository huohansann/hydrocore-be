package com.siact.module.predicted.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.module.predicted.dto.PredictedDataDTO;
import com.siact.module.predicted.entity.PredictedDataEntity;

import java.util.List;
import java.util.Map;

public interface PredictedDataService extends IService<PredictedDataEntity> {

    Map<Integer, List<PredictedDataDTO>> getPredictedData(List<Integer> predictedTypes,String startTime,String endTime);

}
