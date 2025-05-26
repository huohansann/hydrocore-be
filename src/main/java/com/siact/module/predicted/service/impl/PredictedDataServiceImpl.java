package com.siact.module.predicted.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.module.predicted.entity.PredictedDataEntity;
import com.siact.module.predicted.mapper.PredictedDataMapper;
import com.siact.module.predicted.service.PredictedDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PredictedDataServiceImpl extends ServiceImpl<PredictedDataMapper, PredictedDataEntity> implements PredictedDataService {
}
