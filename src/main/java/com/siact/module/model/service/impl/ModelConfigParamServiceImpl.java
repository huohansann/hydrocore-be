package com.siact.module.model.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.module.model.entity.ModelConfigParamEntity;
import com.siact.module.model.mapper.ModelConfigParamMapper;
import com.siact.module.model.service.ModelConfigParamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ModelConfigParamServiceImpl extends ServiceImpl<ModelConfigParamMapper, ModelConfigParamEntity> implements ModelConfigParamService {
}
