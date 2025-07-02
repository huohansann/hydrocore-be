package com.siact.module.model.dto;

import lombok.Data;

@Data
public class AlgorithmPublishModelParamDTO {
    private String time;

    private AlgorithmPublishModelParamDetailDTO params;
}
