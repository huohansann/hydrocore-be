package com.siact.module.model.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.module.model.dto.ModelConfigParamRtnDTO;
import com.siact.module.model.entity.ModelConfigParamEntity;
import com.siact.module.model.vo.ModelConfigParamSaveVO;

import java.util.Map;

public interface ModelConfigParamService extends IService<ModelConfigParamEntity> {

    /**
     * 获取组装tpl中配置的模板
     * @return
     */
    Map<String, String> getParamTemplate();

    /**
     * 根据dataCode和预测类型code查询参数
     * @param dataCode
     * @param predictedTypeCode
     * @return
     */
    ModelConfigParamRtnDTO queryParamByDataCodeAndPredictedTypeCode(String dataCode, String predictedTypeCode);


    /**
     * 保存或更新参数配置
     * @param configParamSaveVo
     */
    void saveOrUpdateParam(ModelConfigParamSaveVO configParamSaveVo);

    /**
     * 根据id下发参数至算法(ps:算法根据参数生成模型数据)
     * @param id
     */
    void sendParamById(Long id);
}
