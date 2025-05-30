package com.siact.module.model.service.impl;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.module.model.entity.ModelConfigParamEntity;
import com.siact.module.model.mapper.ModelConfigParamMapper;
import com.siact.module.model.service.ModelConfigParamService;
import com.siact.module.model.vo.ModelConfigParamSaveVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ModelConfigParamServiceImpl extends ServiceImpl<ModelConfigParamMapper, ModelConfigParamEntity> implements ModelConfigParamService {
    @Override
    public void saveParam(ModelConfigParamSaveVO configParamSaveVo) {
        ModelConfigParamEntity entity = new ModelConfigParamEntity();
        entity.setDataCode(configParamSaveVo.getDataCode());
        entity.setPredictedType(configParamSaveVo.getPredictedType());
        entity.setPredictedTypeCode(configParamSaveVo.getPredictedTypeCode());
        entity.setPublicSetting(JSON.toJSONString(configParamSaveVo.getPublicSetting()));
        entity.setAlgorithmSetting(JSON.toJSONString(configParamSaveVo.getAlgorithmSetting()));

        save(entity);
    }
}
