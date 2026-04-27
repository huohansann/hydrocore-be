package com.siact.module.model.feign;

import com.siact.module.model.dto.AlgorithmGenerateModelParamDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;

@Slf4j
@Component
public class AlgorithmTrainFeignFallBack implements FallbackFactory<AlgorithmTrainFeign> {
    @Override
    public AlgorithmTrainFeign create(Throwable cause) {
        return new AlgorithmTrainFeign() {
            @Override
            public LinkedHashMap<String, Object> train(AlgorithmGenerateModelParamDTO paramDTO) {
                log.error("Feign调用/train降级, 原因: {}", cause.getMessage(), cause);
                LinkedHashMap<String, Object> result = new LinkedHashMap<>();
                result.put("code", "500");
                result.put("message", "Feign降级: " + cause.getMessage());
                return result;
            }
        };
    }
}
