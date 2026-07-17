package com.siact.hydrocore.module.base.dto;

import lombok.Data;

import java.util.List;

@Data
public class FeeModelDTO {
    private String cityCode;
    private String energyCode;
    private List<FeeModelDataDTO> feeModelData;
}
