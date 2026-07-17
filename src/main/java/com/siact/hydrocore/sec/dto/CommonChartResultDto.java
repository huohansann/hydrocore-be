package com.siact.hydrocore.sec.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
public class CommonChartResultDto {

    @ApiModelProperty(value = "名称")
    private List<CommonChartDataDto> list;

    @ApiModelProperty(value = "名称")
    @JsonProperty("xAxisData")
    private List<String> xAxisData;
}
