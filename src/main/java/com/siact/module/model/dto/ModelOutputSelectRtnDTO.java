package com.siact.module.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("模型输出选择DTO")
public class ModelOutputSelectRtnDTO {

    @ApiModelProperty(value = "模型预测选择信息")
    Map<String, List<ModelInfoDTO>> selectedModelInfo;

    @ApiModelProperty(value = "多步开始时间")
    private String multiStartTime;

    @ApiModelProperty(value = "多步结束时间")
    private String multiEndTime;
}
