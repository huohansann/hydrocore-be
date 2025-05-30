package com.siact.module.model.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.module.model.entity.ModelPublishInfoEntity;
import com.siact.module.model.mapper.ModelPublishInfoMapper;
import com.siact.module.model.service.ModelPublishInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ModelPublishInfoServiceImpl extends ServiceImpl<ModelPublishInfoMapper, ModelPublishInfoEntity> implements ModelPublishInfoService {
}
