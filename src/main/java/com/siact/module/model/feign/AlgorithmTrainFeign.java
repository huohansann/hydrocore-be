package com.siact.module.model.feign;

import com.siact.module.model.dto.AlgorithmGenerateModelParamDTO;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.LinkedHashMap;

@RefreshScope
@FeignClient(url = "${algorithm.trainUrl}", name = "algorithm-train-server", fallbackFactory = AlgorithmTrainFeignFallBack.class)
public interface AlgorithmTrainFeign {

    @PostMapping("/train")
    LinkedHashMap<String, Object> train(@RequestBody AlgorithmGenerateModelParamDTO paramDTO);
}
