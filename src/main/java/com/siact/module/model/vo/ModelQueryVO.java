package com.siact.module.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
@ApiModel("查询模型")
public class ModelQueryVO {
    @ApiModelProperty("模型dataCode")
    @NotBlank(message = "dataCode不能为空")
    private String dataCode;
    @ApiModelProperty("预测类型列表,T20/T40....")
    private List<String> predictedTypeCodeList;
}
