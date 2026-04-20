package com.siact.module.device.query;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "设备实时数据查询条件")
public class DeviceRealtimeQuery {

    @ApiModelProperty("点位ID列表")
    private List<String> itemIds;

    @ApiModelProperty("属性名称(模糊搜索)")
    private String propName;

    @ApiModelProperty("设备编码列表")
    private List<String> deviceCodes;

    @ApiModelProperty(value = "查询开始时间", example = "2025-01-01 00:00:00")
    private String startTime;

    @ApiModelProperty(value = "查询结束时间", example = "2025-01-01 23:59:59")
    private String endTime;

}
