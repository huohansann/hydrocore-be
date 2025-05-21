package com.siact.sec.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class DypropInsDTO extends PropValDTO {
    @ApiModelProperty(
            value = "模型DataCode",
            position = 2
    )
    private String modelDataCode;
    @ApiModelProperty(
            value = "实例DataCode",
            position = 3
    )
    private String insDataCode;
    @ApiModelProperty(
            value = "ID",
            position = 1
    )
    private Long id;
    @ApiModelProperty(
            value = "模型id",
            position = 2
    )
    private Long modelId;
    @ApiModelProperty(
            value = "实例id",
            position = 3
    )
    private Long insId;
    @ApiModelProperty(
            value = "属性数字化编码",
            position = 4
    )
    private String dataCode;
    @ApiModelProperty(
            value = "属性编码",
            position = 5
    )
    private @NotNull(
            message = "662"
    ) @Pattern(
            regexp = "^[0-9a-zA-Z_\\-]{3,10}$",
            message = "662"
    ) String propCode;
    @ApiModelProperty(
            value = "序号",
            position = 6
    )
    private Integer serial;
    @ApiModelProperty(
            value = "属性名称",
            position = 7
    )
    private @NotNull(
            message = "663"
    ) @Size(
            min = 1,
            max = 30,
            message = "663"
    ) String propName;
    @ApiModelProperty(
            value = "数据类型",
            position = 8
    )
    private String dataType;
    @ApiModelProperty(
            value = "二值分类",
            position = 9
    )
    private String tfGroup;
    @ApiModelProperty(
            value = "二值类型",
            position = 10
    )
    private String tfType;
    @ApiModelProperty(
            value = "菜单分类",
            position = 11
    )
    private String menuGroup;
    @ApiModelProperty(
            value = "菜单类型",
            position = 12
    )
    private String menuType;
    @ApiModelProperty(
            value = "单选标记（1单选 0多选）",
            position = 13
    )
    private Integer singleFlag;
    @ApiModelProperty(
            value = "数值类型",
            position = 14
    )
    private String numType;
    @ApiModelProperty(
            value = "其它类型",
            position = 15
    )
    private String otherType;
    @ApiModelProperty(
            value = "数据精度",
            position = 16
    )
    private @Min(
            value = 0L,
            message = "664"
    ) Integer p;
    @ApiModelProperty(
            value = "属性类型",
            position = 17
    )
    private String propType;
    @ApiModelProperty(
            value = "转换比率",
            position = 18
    )
    private Float ratio;
    @ApiModelProperty(
            value = "字符长度",
            position = 19
    )
    private @Min(
            value = 0L,
            message = "665"
    ) Integer maxLen;
    @ApiModelProperty(
            value = "结构化数据定义",
            position = 20
    )
    private String structDef;
    @ApiModelProperty(
            value = "公式",
            position = 21
    )
    private String formula;
    @ApiModelProperty(
            value = "单位",
            position = 22
    )
    private String unit;
    @ApiModelProperty(
            value = "约束类型",
            position = 23
    )
    private String consType;
    @ApiModelProperty(
            value = "最大值",
            position = 24
    )
    private Float maxVal;
    @ApiModelProperty(
            value = "最小值",
            position = 25
    )
    private Float minVal;
    @ApiModelProperty(
            value = "取值列表",
            position = 26
    )
    private String valArr;
    @ApiModelProperty(
            value = "必选/可选",
            position = 27
    )
    private String mo;
    @ApiModelProperty(
            value = "读/写/读写",
            position = 28
    )
    private String rw;
    @ApiModelProperty(
            value = "英文全称",
            position = 29
    )
    private @Pattern(
            regexp = "^$|^.{1,50}",
            message = "667"
    ) String allName;
    @ApiModelProperty(
            value = "中文描述",
            position = 30
    )
    private @Pattern(
            regexp = "^$|^.{1,50}",
            message = "666"
    ) String zhDesc;
    @JsonIgnore
    @ApiModelProperty(
            value = "属性类型, JC:监测,KZ:控制,NX:能效,NH:能耗,GZ:故障（告警、运维、检修）,MT:市场属性,C:碳计量属性,CG:可调属性",
            position = 2
    )
    private List<String> propTypes;
    @JsonIgnore
    @ApiModelProperty("实例id")
    private List<Long> insIds;
    @JsonIgnore
    @ApiModelProperty("属性数字化编码")
    private List<String> dataCodes;
    @ApiModelProperty("数字化编码前缀")
    @JsonIgnore
    private String dataCodePreffix;
}
