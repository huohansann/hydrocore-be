package com.siact.module.model.utils;

import com.siact.module.base.service.TplService;
import com.siact.module.model.dto.AlgorithmDataCodeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class AlgorithmDataCodeUtil {

    @Autowired
    private TplService tplService;

    // k:算法code v:孪生code
    Map<String, AlgorithmDataCodeDTO> algorithmDataCodeMap = new HashMap<>();
    // k:孪生code v:算法code
    Map<String, AlgorithmDataCodeDTO> dataCodeAlgorithmMap = new HashMap<>();

    @PostConstruct
    public void init() {
        List<AlgorithmDataCodeDTO> algorithmDataCode = tplService.getListByCode("algorithmDataCode", AlgorithmDataCodeDTO.class);

        algorithmDataCodeMap = algorithmDataCode.stream().collect(Collectors.toMap(AlgorithmDataCodeDTO::getAlgorithmCode, o -> o, (o1, o2) -> o1));
        dataCodeAlgorithmMap = algorithmDataCode.stream().collect(Collectors.toMap(AlgorithmDataCodeDTO::getDataCode, o -> o, (o1, o2) -> o1));
    }

    public AlgorithmDataCodeDTO getByAlgorithmCode(String algorithmCode) {
        return algorithmDataCodeMap.get(algorithmCode);
    }

    public AlgorithmDataCodeDTO getByDataCode(String dataCode) {
        return dataCodeAlgorithmMap.get(dataCode);
    }
}
