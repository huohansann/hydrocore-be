package com.siact.hydrocore.module.system.dto;

import com.siact.hydrocore.module.system.enums.SysConfigModuleEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 系统配置传输对象（组装后的完整配置）
 *
 * @author siact
 */
@Data
public class SysConfigDTO {

    @ApiModelProperty("配置编码")
    private String scCode;

    @ApiModelProperty("模块")
    private SysConfigModuleEnum module;

    @ApiModelProperty("配置名称")
    private String scName;

    @ApiModelProperty("配置说明")
    private String description;

    @ApiModelProperty("乐观锁版本号")
    private Integer version;

    @ApiModelProperty("配置数据（Map 或 List）")
    private Object data;
}