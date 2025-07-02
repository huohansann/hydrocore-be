package com.siact.module.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class AlgorithmPublishModelParamDTO {
    private String time;

    private List<AlgorithmPublishModelParamDetailDTO> params;
}
