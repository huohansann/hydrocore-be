package com.siact.hydrocore.module.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.Version;
import com.siact.hydrocore.module.system.enums.SysConfigModuleEnum;
import com.siact.hydrocore.module.system.enums.SysConfigTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.sql.Timestamp;

/**
 * 系统选项配置实体
 *
 * @author siact
 */
@Data
@TableName("sys_config")
public class SysConfigEntity {

    @ApiModelProperty("主键")
    @NotNull(message = "[主键]不能为空")
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    @ApiModelProperty("模块")
    @NotBlank(message = "[模块]不能为空")
    @Size(max = 255, message = "模块长度不能超过255")
    private SysConfigModuleEnum module;

    @ApiModelProperty("配置编码")
    @NotBlank(message = "[配置编码]不能为空")
    @Size(max = 255, message = "配置编码长度不能超过255")
    private String scCode;

    @ApiModelProperty("配置路径")
    @NotBlank(message = "[配置路径]不能为空")
    @Size(max = 255, message = "配置路径长度不能超过255")
    private String scPath;

    @ApiModelProperty("配置名称")
    @NotBlank(message = "[配置名称]不能为空")
    @Size(max = 500, message = "配置名称长度不能超过500")
    private String scName;

    @ApiModelProperty("配置类型")
    @NotBlank(message = "[配置类型]不能为空")
    private SysConfigTypeEnum scType;

    @ApiModelProperty("配置值")
    @NotBlank(message = "[配置值]不能为空")
    private String scValue;

    @ApiModelProperty("配置说明")
    @NotBlank(message = "[配置说明]不能为空")
    @Size(max = 255, message = "配置说明长度不能超过255")
    private String description;

    @ApiModelProperty("乐观锁版本号")
    @Version
    private Integer version;

    @ApiModelProperty("创建时间")
    private Timestamp createTime;

    @ApiModelProperty("更新时间")
    private Timestamp updateTime;
}