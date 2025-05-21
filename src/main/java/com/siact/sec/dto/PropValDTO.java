package com.siact.sec.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.siact.api.common.api.vo.common.DicDataVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class PropValDTO {
    @ApiModelProperty(
            value = "属性取值",
            position = 1
    )
    private String propVal;
    @ApiModelProperty(
            value = "属性值更新时间",
            position = 2
    )
    @JsonFormat(
            locale = "zh",
            timezone = "GMT+8",
            pattern = "yyyy-MM-dd HH:mm:ss"
    )
    private Date updateTime;
    @ApiModelProperty(
            value = "属性取值",
            position = 3
    )
    private String propValLabel;
    @ApiModelProperty(
            value = "二值和菜单 可选值",
            position = 4
    )
    private List<DicDataVo> propValSelect;

    public PropValDTO() {
    }

}
