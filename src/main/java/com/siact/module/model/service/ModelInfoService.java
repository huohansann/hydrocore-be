package com.siact.module.model.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.module.model.dto.ModelAssessChartDTO;
import com.siact.module.model.dto.ModelInfoDTO;
import com.siact.module.model.entity.ModelInfoEntity;
import com.siact.module.model.vo.SendModelVO;

import java.util.List;
import java.util.Map;

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
    Map<String, List<ModelInfoDTO>> queryModelByDataCodeGroupByPredictedTypeCodes(String dataCode, List<String> predictedTypeCodeList);

    /**
     * 下发模型
     * @param sendModelVoList
     */
    void publishModel(List<SendModelVO> sendModelVoList);

    /**
     * 根据模型id 查询指标评价数据
     * @param modelIdList
     * @return
     */
    ModelAssessChartDTO queryModelAssessChart(List<Long> modelIdList);
}
