package com.siact.hydrocore.module.base.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * 配置字段存储 数据传输对象
 *
 * @author siact
 */
@Data
public class ConfigFieldStoreDTO implements Serializable {
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
    private String createTime;

    /** 更新时间 */
    private String updateTime;
} 