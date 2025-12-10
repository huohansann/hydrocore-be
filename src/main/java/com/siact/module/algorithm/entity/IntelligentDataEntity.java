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
    /* 属性类型 */
    private IntelliTypeEnum intelliType;
    /* 算法结果 */
    private BigDecimal val;
    /* 获取数据时间 */
    private String time;
    /* 算法输出 */
    private String data;
    /* 创建时间 */
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private Date createTime;
}
