package com.siact.module.system.dto;

import com.siact.module.system.enums.SysConfigTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 系统配置单项传输对象（单个路径的配置）
 *
 * @author siact
 */
@Data
public class SysConfigItemDTO {

    @ApiModelProperty("配置编码")
    private String scCode;

    @ApiModelProperty("配置路径")
    private String scPath;

    @ApiModelProperty("配置名称")
    private String scName;

    @ApiModelProperty("配置类型")
    private SysConfigTypeEnum scType;

    @ApiModelProperty("配置值")
    private String scValue;

    @ApiModelProperty("配置说明")
    private String description;

    @ApiModelProperty("乐观锁版本号")
    private Integer version;
}