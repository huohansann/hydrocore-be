package com.siact.module.model.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.module.model.entity.ModelConfigParamEntity;
import com.siact.module.model.vo.ModelConfigParamSaveVO;

public interface ModelConfigParamService extends IService<ModelConfigParamEntity> {
    void saveParam(ModelConfigParamSaveVO configParamSaveVo);
}
