package com.siact.module.model.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.module.model.entity.ModelInfoEntity;
import com.siact.module.model.mapper.ModelInfoMapper;
import com.siact.module.model.service.ModelInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ModelInfoServiceImpl extends ServiceImpl<ModelInfoMapper, ModelInfoEntity> implements ModelInfoService {
}
