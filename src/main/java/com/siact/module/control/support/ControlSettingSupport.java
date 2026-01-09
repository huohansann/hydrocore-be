package com.siact.module.control.support;

import com.siact.api.common.api.vo.common.InfoListQueryVo;
import com.siact.common.constant.ConstantSymbol;
import com.siact.sec.dto.EqDypropInsDTO;
import com.siact.sec.sevice.SecInsService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author : kzuo
 * @version 1.0
 * @date : 2026-01-04 14:44
 * @className : ControlSettingSupport
 * @description : 控制设置支撑
 */

@Slf4j
@AllArgsConstructor
@Component
public class ControlSettingSupport {
    private final SecInsService secInsService;

    public Map<String, String> querySecPropCode(List<String> dataCodeList, List<String> propShortCode) {
        InfoListQueryVo infoListQueryVo = new InfoListQueryVo();
        infoListQueryVo.setDataCodes(new HashSet<>(dataCodeList));
        infoListQueryVo.setPropModelCodes(propShortCode);
        Map<String, List<EqDypropInsDTO>> result = secInsService.queryInsDynamicProp(infoListQueryVo);
        log.info("多个实例, 一个属性短码, 获取对应的属性长码对应,dataCodeList:{},propShortCode:{},result:{}", dataCodeList, propShortCode, result);

        HashMap<String, String> dataCodeShortMap = new HashMap<>();
        for (Map.Entry<String, List<EqDypropInsDTO>> entry : result.entrySet()) {
            String dataCode = entry.getKey();

            entry.getValue().forEach(eqDypropInsDTO -> {
                // 属性短码 Code
                String propCode = eqDypropInsDTO.getPropCode();
                String propDataCode = eqDypropInsDTO.getDataCode();
                String mapKey = String.join(ConstantSymbol.UNDER_LINE, dataCode, propCode);
                dataCodeShortMap.put(mapKey, propDataCode);
            });
        }
        return dataCodeShortMap;
    }
}
