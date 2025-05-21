package com.siact.sec.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Data
@ApiModel(description = "累计数据请求参数")
public class CumulativeDataVO {

    @NotEmpty
    @ApiModelProperty(value = "编码codes")
    private List<String> dataCodes;

    @ApiModelProperty(value = "开始时间")
    private String startTime;

    @ApiModelProperty(value = "结束时间")
    private String endTime;

    @ApiModelProperty(value = "是否同比")
    private boolean yoy;

    @ApiModelProperty(value = "是否环比")
    private boolean qoq;

    @ApiModelProperty(value = "时间类型")
    private String timeType;
}
