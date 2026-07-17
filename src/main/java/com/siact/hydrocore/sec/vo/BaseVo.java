package com.siact.hydrocore.sec.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Data
public class BaseVo extends FiveBaseVo {
    public BaseVo() {super();}

    @NotBlank(message = "格式化格式不能为空")
    @ApiModelProperty(value = "格式化格式，如MM-dd")
    private String formatVal;

    @ApiModelProperty(value = "名称集合")
    private List<String> names;

    @ApiModelProperty(value = "单位集合")
    private List<String> units;

    @ApiModelProperty(value = "是否展示表格集合")
    private List<Boolean> showTables;
}
