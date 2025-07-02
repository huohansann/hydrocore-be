package com.siact.module.model.feign;

import com.siact.module.model.dto.AlgorithmGenerateModelParamDTO;
import com.siact.module.model.dto.AlgorithmPublishModelParamDTO;
import org.springframework.cloud.openfeign.FallbackFactory;

import java.util.LinkedHashMap;

public class AlgorithmFeignFallBack  implements FallbackFactory<AlgorithmFeign> {
    @Override
    public AlgorithmFeign create(Throwable cause) {
        return new AlgorithmFeign() {
            @Override
            public LinkedHashMap<String, Object> train(AlgorithmGenerateModelParamDTO paramDTO) {
                return new LinkedHashMap<>();
            }

            @Override
            public LinkedHashMap<String, Object> inference(AlgorithmPublishModelParamDTO paramDTO) {
                return new LinkedHashMap<>();
            }
        };
    }
}
