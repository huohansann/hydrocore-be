package com.siact.hydrocore.common.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PageDataDto<T> {

    @ApiModelProperty(value = "总页数")
    private Long total;

    @ApiModelProperty(value = "要展示的List数据")
    private List<T> data;

}
