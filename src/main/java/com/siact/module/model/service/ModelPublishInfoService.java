package com.siact.module.model.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.module.model.entity.ModelPublishInfoEntity;

import java.util.List;

public interface ModelPublishInfoService extends IService<ModelPublishInfoEntity> {
    void removeByDataCode(String dataCode);

    ModelPublishInfoEntity queryLastPublishInfoByDataCode(String dataCode);

    List<ModelPublishInfoEntity> queryLastPublishInfoByDataCodeList(List<String> dataCodeList);

}
