package com.siact.module.model.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("下发模型参数")
public class PublishModelVO {
    @ApiModelProperty(value = "左侧模型的dataCode,如MC1的dataCode")
    private String dataCode;

    @ApiModelProperty(value = "勾选的模型ID列表")
    private List<Long> modelIdList;

    @ApiModelProperty(value = "多步预测开始时间")
    private String multiStartTime;

    @ApiModelProperty(value = "多步预测结束时间")
    private String multiEndTime;

}
