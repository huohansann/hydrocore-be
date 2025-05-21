package com.siact.sec.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class DevModelTypeDTO {
    public DevModelTypeDTO(){}

    public DevModelTypeDTO(String modelName, String modelDataCode, List<TMInsSimpleDTO> devInsDTOS){
        this.modelName = modelName;
        this.modelDataCode = modelDataCode;
        this.devInsDTOS = devInsDTOS;
    }

    @ApiModelProperty(value = "设备模型名")
    private String modelName;

    @ApiModelProperty(value = "设备模型长码")
    private String modelDataCode;

    @ApiModelProperty(value = "设备实例")
    private List<TMInsSimpleDTO> devInsDTOS;
}
