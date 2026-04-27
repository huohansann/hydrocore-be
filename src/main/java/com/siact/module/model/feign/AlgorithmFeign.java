package com.siact.module.model.feign;

import com.siact.module.model.dto.AlgorithmPublishModelParamDTO;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.LinkedHashMap;

@RefreshScope
@FeignClient(url = "${algorithm.baseUrl}", name = "order-server", fallbackFactory = AlgorithmFeignFallBack.class)
public interface AlgorithmFeign {

    /**
     * 获取预测数据
     *
     * @param paramDTO
     * @return
     */
    @PostMapping("/inference")
    LinkedHashMap<String, Object> inference(@RequestBody AlgorithmPublishModelParamDTO paramDTO);
}
