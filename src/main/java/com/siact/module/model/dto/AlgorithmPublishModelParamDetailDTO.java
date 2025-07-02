package com.siact.module.model.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class AlgorithmPublishModelParamDetailDTO {

    private String model_id;
    private String model_name;
    private String method;
    private Integer work_code_num;
    private Integer work_code;
    private Map<String, String> data;
    private Integer step;
}
