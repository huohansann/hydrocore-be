package com.siact.module.model.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

@Data
@TableName("model_publish_info")
public class ModelPublishInfoEntity {
    private Long id;
    // 模型数据的dataCode
    private String dataCode;
    // 批次下发的模型id,多个用逗号进行分隔
    private String publishModelInfoIds;
    // 多步开始时间
    private String multiStartTime;
    // 多步结束时间
    private String multiEndTime;
    // 创建时间
    private Date createTime;
    // 是否删除 0:未删除 1:已删除
    private Integer deleted;
}
