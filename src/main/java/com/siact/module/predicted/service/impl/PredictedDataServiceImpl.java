package com.siact.module.predicted.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.module.predicted.dto.PredictedDataDTO;
import com.siact.module.predicted.entity.PredictedDataEntity;
import com.siact.module.predicted.mapper.PredictedDataMapper;
import com.siact.module.predicted.service.PredictedDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PredictedDataServiceImpl extends ServiceImpl<PredictedDataMapper, PredictedDataEntity> implements PredictedDataService {
    @Override
    public Map<Integer, List<PredictedDataDTO>> getPredictedData(List<Integer> predictedTypes, String startTime, String endTime) {
        return new HashMap<>();
    }
}
