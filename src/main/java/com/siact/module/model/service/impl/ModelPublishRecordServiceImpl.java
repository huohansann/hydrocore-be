package com.siact.module.model.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.module.model.entity.ModelPublishRecordEntity;
import com.siact.module.model.mapper.ModelPublishRecordMapper;
import com.siact.module.model.service.ModelPublishRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ModelPublishRecordServiceImpl extends ServiceImpl<ModelPublishRecordMapper, ModelPublishRecordEntity> implements ModelPublishRecordService {
}
