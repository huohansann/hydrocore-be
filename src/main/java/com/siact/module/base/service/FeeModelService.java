package com.siact.module.base.service;

import com.siact.module.base.dto.FeeModelDataDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface FeeModelService {

    List<FeeModelDataDTO> getFeeModel(String cityCode, String energyCode, String feeModelStyle);

    /**
     * 根据最小的ts汇总费价模型
     * @param cityCode
     * @param energyCode
     * @param feeModelStyle
     * @return
     */
    Map<String, FeeModelDataDTO> getFeeModelGroupByMinTs(String cityCode, String energyCode, String feeModelStyle);


    /**
     * 根据最小的ts汇总费价模型(仅费用)
     * @param cityCode
     * @param energyCode
     * @param feeModelStyle
     * @return
     */
    Map<String, BigDecimal> getFeeModelFeeGroupByMinTs(String cityCode, String energyCode, String feeModelStyle);

}
