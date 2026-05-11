package com.siact.module.algorithm.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.siact.module.algorithm.enums.IntelliTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("intelligent_data")
public class IntelligentDataEntity implements Serializable {
    /* 主键 */
    private @TableId(value = "id", type = IdType.ASSIGN_ID) Long id;
    /* 点位名称 */
    private String name;
    /* 数字孪生长码 */
    private String dataCode;
    /* 是否为总气量 */
    private boolean isMaster;
    /* 属性类型 */
    private IntelliTypeEnum intelliType;
    /* 算法结果 */
    private BigDecimal val;
    /* 获取数据时间 */
    private String time;
    /* 算法输出 */
    private String data;
    /* 是否通过约束规则校验：true=通过，false=未通过，null=未校验 */
    private Boolean ruleValid;
    /* 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
}
