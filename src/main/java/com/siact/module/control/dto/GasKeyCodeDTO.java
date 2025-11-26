package com.siact.module.control.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2025-11-26 13:34
 * @className : GasKeyCodeDTO
 * @description : 天然气配置编码参数实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel("天然气编码配置参数")
public class GasKeyCodeDTO {
    private @ApiModelProperty("设备编号") String key;
    private @ApiModelProperty("设备编码") String code;
}
