package com.siact.module.process.enums;

import com.siact.module.process.dto.DefoamSystemDTO;
import com.siact.module.process.dto.FireCycleDTO;
import com.siact.module.process.dto.ProcessOneHotEncoderDTO;
import com.siact.module.process.dto.ProductLineDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "process")
public class ProcessConfig {

    @ApiModelProperty(value = "消泡系统")
    private List<DefoamSystemDTO> defoamSystem;

    @ApiModelProperty(value = "换火周期")
    private List<FireCycleDTO> fireCycle;

    @ApiModelProperty(value = "生产线")
    private List<ProductLineDTO> productLine;

    @ApiModelProperty(value = "工况 one-hot 编码")
    private List<ProcessOneHotEncoderDTO> processOneHotEncoder;


    // 合并所有配置
    public Map<String, Object> getAllProcessConfig() {
        return new HashMap<String, Object>() {{
            put("defoamSystem", defoamSystem);
            put("fireCycle", fireCycle);
            put("productLine", productLine);
            put("processOneHotEncoder", processOneHotEncoder);
        }};
    }

    // 消泡系统 根据 code 查询
    public DefoamSystemDTO getDefoamSystemByCode(String code) {
        for (DefoamSystemDTO e : defoamSystem) {
            if (e.getCode().equals(code)) return e;
        }
        return null;
    }

    // 换火周期 根据 code 查询
    public FireCycleDTO getByFireCycleCode(String code) {
        for (FireCycleDTO e : fireCycle) {
            if (e.getCode().equals(code)) return e;
        }
        return null;
    }


    // 生产线 根据 code 查询
    public ProductLineDTO getByProductLineCode(String code) {
        for (ProductLineDTO e : productLine) {
            if (e.getCode().equals(code)) return e;
        }
        return null;
    }

    // 工况 one-hot 编码
    /**
     * 将类型字符串转换为 one-hot 编码数组
     *
     * @param type 类型名称（如 "Type1"）
     * @return 对应的 one-hot 编码数组
     * @throws IllegalArgumentException 如果类型不存在
     */
    public ProcessOneHotEncoderDTO getProcessOneHotEncoderByType(String type) {
        for (ProcessOneHotEncoderDTO encoder : processOneHotEncoder) {
            if (encoder.getType().equals(type)) {
                return encoder;
            }
        }

        return null;
    }


    /**
     * 将类型字符串转换为 one-hot 编码数组
     *
     * @param type 类型名称（如 "Type1"）
     * @return 对应的 one-hot 编码数组
     * @throws IllegalArgumentException 如果类型不存在
     */
    public int[] getProcessOneHotByType(String type) {
        for (ProcessOneHotEncoderDTO encoder : processOneHotEncoder) {
            if (encoder.getType().equals(type)) {
                return encoder.getOneHotArr();
            }
        }

        return null;
    }

    /**
     * 将类型字符串转换为 operatingCode 编码
     *
     * @param type 类型名称
     * @return 对应的 operatingCode 编码
     * @throws IllegalArgumentException 如果类型不存在
     */
    public Integer getProcessAlgorithmCodeByType(String type) {
        for (ProcessOneHotEncoderDTO encoder : processOneHotEncoder) {
            if (encoder.getType().equals(type)) {
                return encoder.getAlgorithmProcessCode();
            }
        }

        return null;
    }

}