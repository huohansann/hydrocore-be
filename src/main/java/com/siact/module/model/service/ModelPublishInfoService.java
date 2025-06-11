package com.siact.module.model.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.module.model.entity.ModelPublishInfoEntity;

public interface ModelPublishInfoService extends IService<ModelPublishInfoEntity> {
    ModelPublishInfoEntity queryLastPublishInfoByDataCode(String dataCode);
}
