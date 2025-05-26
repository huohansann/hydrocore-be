package com.siact.module.predicted.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.module.predicted.entity.PredictedRecordEntity;
import com.siact.module.predicted.mapper.PredictedRecordMapper;
import com.siact.module.predicted.service.PredictedRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class PredictedRecordServiceImpl extends ServiceImpl<PredictedRecordMapper, PredictedRecordEntity> implements PredictedRecordService {
}
