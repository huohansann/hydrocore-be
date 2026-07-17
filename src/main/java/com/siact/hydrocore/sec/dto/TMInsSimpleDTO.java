package com.siact.hydrocore.sec.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel("设备实例DTO")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TMInsSimpleDTO {
    @ApiModelProperty(value = "id")
    private Long insId;
    @ApiModelProperty(value = "名称")
    private String insName;
    @ApiModelProperty(value = "实例数字化编码")
    private String dataCode;
    @ApiModelProperty(value = "节点类型", position = 3)
    private String nodeType;
}
