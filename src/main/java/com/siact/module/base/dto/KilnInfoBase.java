package com.siact.module.base.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class KilnInfoBase {
    /** 主键id */
    @ApiModelProperty("主键id")
    private Long id;
    /** 炉子编号 */
    @ApiModelProperty("炉子编号")
    private String number;
    /** 炉子编码，数字孪生编码 */
    @ApiModelProperty("炉子编码，数字孪生编码")
    private String code;
}
