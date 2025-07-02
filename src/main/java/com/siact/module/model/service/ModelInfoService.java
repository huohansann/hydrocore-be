package com.siact.module.model.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.module.model.dto.ModelAssessChartDTO;
import com.siact.module.model.dto.ModelInfoDTO;
import com.siact.module.model.dto.ModelOutputSelectRtnDTO;
import com.siact.module.model.entity.ModelInfoEntity;
import com.siact.module.model.vo.PublishModelVO;

import java.util.List;

public interface ModelInfoService extends IService<ModelInfoEntity> {
    void saveModelInfo(ModelInfoDTO modelInfoDTO);

    /**
     * 根据dataCode和 predictedTypeCodes查询模型
     * 并 根据 predictedTypeCode 进行分组
     *
     * @param dataCode
     * @param predictedTypeCodeList
     * @return
     */
    ModelOutputSelectRtnDTO queryModelByDataCodeGroupByPredictedTypeCodes(String dataCode, List<String> predictedTypeCodeList);

    /**
     * 下发模型 调用设备下发参数
     *
     * @param publishModelVO
     */
    void publishModel(PublishModelVO publishModelVO);

    /**
     * 根据模型id 查询指标评价数据
     *
     * @param modelIdList
     * @return
     */
    ModelAssessChartDTO queryModelAssessChart(List<Long> modelIdList);

    /**
     * 根据dataCodeList和 predictedTypeCodeList查询模型
     * @param dataCodeList
     * @param predictedTypeCodeList
     * @return
     */
    List<ModelInfoEntity> queryModelByDataCodeAndPredictedTypeCodes(List<String> dataCodeList, List<String> predictedTypeCodeList);
}
