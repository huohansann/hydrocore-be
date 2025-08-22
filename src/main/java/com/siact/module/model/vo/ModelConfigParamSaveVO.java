package com.siact.module.model.vo;

import com.siact.module.model.dto.ModelConfigParamDTO;
import com.siact.module.model.dto.ModelConfigParamDetailDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Date;
import java.util.List;

@Data
@ApiModel(value = "模型参数保存")
public class ModelConfigParamSaveVO {
    @ApiModelProperty(value = "id(修改时携带)")
    private Long id;

    @NotBlank(message = "预测设备的孪生code不能为空")
    @ApiModelProperty(value = "预测设备的孪生code")
    private String dataCode;

    @ApiModelProperty(value = "预测类型 1:单步预测 2:多步预测")
    private Integer predictedType;

    @ApiModelProperty(value = "预测类型Code,单步如:T20,T40,多步:MULTI")
    @NotBlank(message = "预测类型Code不能为空")
    private String predictedTypeCode;

    @ApiModelProperty(value = "公共参数配置(页面配置json)")
    @NotNull(message = "公共参数配置不能为空")
    private ModelConfigParamDTO publicSetting;

    @ApiModelProperty(value = "算法参数配置(页面配置json)")
    @NotNull(message = "公共参数配置不能为空")
    private List<ModelConfigParamDetailDTO> algorithmSetting;

    @ApiModelProperty(value = "自定义模型名称")
    @NotBlank(message = "自定义模型名称不能为空")
    @Length(max = 20, message = "自定义模型名称长度不能超过20个字")
    @Pattern(regexp = "^[\\u4E00-\\u9FA5a-zA-Z0-9_]+$", message = "自定义模型名称只能包含中文、字母、数字、下划线")
    private String customModelName;

    @ApiModelProperty(value = "算法生成模型训练集开始时间")
    @NotNull(message = "算法生成模型训练集开始时间不能为空")
    private Date trainDataStartTime;

    @ApiModelProperty(value = "算法生成模型训练集结束时间")
    @NotNull(message = "算法生成模型训练集结束时间不能为空")
    private Date trainDataEndTime;
}
