package com.siact.sec.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * @desc:
 * @author: zhangwentao
 * @create: 2025-04-24 11:04
 */
@Data
@ApiModel
public class RealTimeDTO {
    @NotEmpty(message = "数据编码不能为空")
    @ApiModelProperty(value = "数据编码")
    List<String> dataCode;
}