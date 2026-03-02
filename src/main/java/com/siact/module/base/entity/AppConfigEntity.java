package com.siact.module.base.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.siact.module.base.enums.AppConfigModuleEnum;
import com.siact.module.base.enums.AppConfigTypeEnum;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Date;

/**
 * @author : kzuo
 * @version : 1.0
 * @date : 2026-02-27 11:50
 * @className : AppConfigEntity
 * @description : 系统程序配置实体
 */
@Data
@EqualsAndHashCode
@TableName("app_config")
public class AppConfigEntity {
    /**
     * 主键 ID
     */
    @ApiModelProperty("主键")
    @NotNull(message = "[主键]不能为空")
    private @TableId(value = "id", type = IdType.ASSIGN_ID) Long id;
    /**
     * 模块
     */
    @ApiModelProperty("模块")
    @NotBlank(message = "[模块]不能为空")
    @Size(max = 255, message = "编码长度不能超过255")
    @Length(max = 255, message = "编码长度不能超过255")
    private AppConfigModuleEnum module;
    /**
     * 配置 key
     */
    @ApiModelProperty("配置 key")
    @NotBlank(message = "[配置 key]不能为空")
    @Size(max = 255, message = "编码长度不能超过255")
    @Length(max = 255, message = "编码长度不能超过255")
    private String acKey;
    /**
     * 配置名称
     */
    @ApiModelProperty("配置名称")
    @NotBlank(message = "[配置名称]不能为空")
    @Size(max = 500, message = "编码长度不能超过500")
    @Length(max = 500, message = "编码长度不能超过500")
    private String acName;
    /**
     * 配置类型: STRING, NUMBER, BOOLEAN, JSON
     */
    @ApiModelProperty("配置类型: STRING, NUMBER, BOOLEAN, JSON")
    @NotBlank(message = "[配置类型: STRING, NUMBER, BOOLEAN, JSON]不能为空")
    @Size(max = 50, message = "编码长度不能超过50")
    @Length(max = 50, message = "编码长度不能超过50")
    private AppConfigTypeEnum acType;
    /**
     * 配置值
     */
    @ApiModelProperty("配置值")
    @NotBlank(message = "[配置值]不能为空")
    private String acValue;
    /**
     * 配置说明
     */
    @ApiModelProperty("配置说明")
    @NotBlank(message = "[配置说明]不能为空")
    @Size(max = 255, message = "编码长度不能超过255")
    @Length(max = 255, message = "编码长度不能超过255")
    private String description;
    /**
     * 顺序
     */
    @ApiModelProperty("顺序")
    private Integer sort;
    /**
     * 创建时间
     */
    @ApiModelProperty("创建时间")
    private Date createTime;
    /**
     * 更新时间
     */
    @ApiModelProperty("更新时间")
    private Date updateTime;
}
