package com.siact.hydrocore.module.base.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;

/**
 * 配置字段存储 视图对象
 *
 * @author siact
 */
@Data
public class ConfigFieldStoreVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 字段键 */
    private String fieldKey;

    /** 字段值 */
    private String fieldValue;

    /** 字段名称 */
    private String fieldName;

    /** 备注 */
    private String remark;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
} 