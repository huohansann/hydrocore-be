package com.siact.module.model.vo;

import com.siact.module.model.dto.ModelConfigParamDTO;
import com.siact.module.model.dto.ModelConfigParamDetailDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel(value = "模型参数保存")
public class ModelConfigParamSaveVO {
    @ApiModelProperty(value = "id(修改时携带)")
    private Long id;
    // 预测设备的孪生code
    @ApiModelProperty(value = "预测设备的孪生code")
    private String dataCode;
    // 预测类型 1:单步预测 2:多步预测
    @ApiModelProperty(value = "预测类型 1:单步预测 2:多步预测")
    private Integer predictedType;
    // 预测类型Code,单步如:T20,T40,多步:MULTI
    @ApiModelProperty(value = "预测类型Code,单步如:T20,T40,多步:MULTI")
    private String predictedTypeCode;
    // 公共参数配置(页面配置json)
    @ApiModelProperty(value = "公共参数配置(页面配置json)")
    private ModelConfigParamDTO publicSetting;
    // 算法参数配置(页面配置json)
    @ApiModelProperty(value = "算法参数配置(页面配置json)")
    private List<ModelConfigParamDetailDTO> algorithmSetting;
    @ApiModelProperty(value = "自定义模型名称")
    private String customModelName;
}
