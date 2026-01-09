package com.siact.module.control.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.siact.common.R;
import com.siact.common.constant.ConstantNum;
import com.siact.common.constant.ConstantSymbol;
import com.siact.module.control.dto.ControlSettingGasDTO;
import com.siact.module.control.dto.ControlSettingWindDTO;
import com.siact.module.control.entity.ControlSettingWindEntity;
import com.siact.module.control.service.ControlSettingWindService;
import com.siact.module.control.service.KilnPublishService;
import com.siact.module.control.support.ControlSettingSupport;
import com.siact.module.control.validator.RuleValidateResult;
import com.siact.module.control.validator.RuleValidator;
import com.siact.sec.sevice.DataService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 下发服务实现类
 *
 * @author wr
 */
@Slf4j
@Service
public class KilnPublishServiceImpl implements KilnPublishService {
    private @Resource ControlSettingWindService controlSettingWindService;
    private @Resource DataService dataService;
    private @Resource ControlSettingSupport support;
    private @Resource List<RuleValidator> validators;

    @Override
    public List<ControlSettingWindDTO> getKilnWindControlSetting() {

        List<ControlSettingWindDTO> result = new ArrayList<>();

        // 助燃风配置
        List<ControlSettingWindEntity> windSettingList = controlSettingWindService.getValidList();
        if (ObjectUtils.isEmpty(windSettingList)) {
            log.error("获取助燃风控制设定值失败, 配置为空");
            return result;
        }
        windSettingList.sort(Comparator.comparing(ControlSettingWindEntity::getNumber));

        // 收集dataCode
        String zrfShortCode = "XLS";// 助燃风流量设定值短码
        List<String> windDataCodeList = windSettingList.stream().map(ControlSettingWindEntity::getWindDataCode).distinct().collect(Collectors.toList());

        String trqShortCode = "XLS";// 天然气流量设定值短码
        List<String> gasDataCodeList = windSettingList.stream().map(ControlSettingWindEntity::getGasDataCode).distinct().collect(Collectors.toList());

        // 合并助燃风和天然气的dataCode
        List<String> allDataCodeList = new ArrayList<>();
        allDataCodeList.addAll(windDataCodeList);
        allDataCodeList.addAll(gasDataCodeList);

        // 合并助燃风设定值和天然气设定值的属性短码 (孪生中两个设定值的短码是一致的,均为XLS)
        List<String> allPropShortCode = Arrays.asList(zrfShortCode, trqShortCode);

        // 天然气和助燃风设定值的属性Code  k:
        Map<String, String> gasAndWindSecPropCodeMap = support.querySecPropCode(allDataCodeList, allPropShortCode);

        // 查询孪生实时值  根据dataCode 查询
        JSONObject propCodeValObj = dataService.queryRealValue(String.join(",", gasAndWindSecPropCodeMap.values()));

        // 封装返回结果
        windSettingList.forEach(entity -> {
            // 解析助燃风设定值
            String windMapKey = String.join(ConstantSymbol.UNDER_LINE, entity.getWindDataCode(), zrfShortCode);
            String windPropCode = gasAndWindSecPropCodeMap.get(windMapKey);// 获取属性Code
            // 助燃风设定值
            BigDecimal windVal = propCodeValObj == null ? null : propCodeValObj.getBigDecimal(windPropCode);

            // 解析天然气设定值
            String gasMapKey = String.join(ConstantSymbol.UNDER_LINE, entity.getGasDataCode(), trqShortCode);
            String trqPropCode = gasAndWindSecPropCodeMap.get(gasMapKey);// 获取属性Code
            // 天然气设定值
            BigDecimal gasVal = propCodeValObj == null ? null : propCodeValObj.getBigDecimal(trqPropCode);

            // 计算风气比
            // 风气比是要计算的  逻辑  风气比 = 助燃风流量设定值 / 天然气流量设定值
            BigDecimal rateVal = (gasVal == null || windVal == null) ? null : windVal.divide(gasVal, ConstantNum.NUMBER_SIX, RoundingMode.HALF_UP);

            log.info("获取助燃风控制设定值, 助燃风流量设定值: {}, 天然气流量设定值: {}, 风气比: {}", windVal, gasVal, rateVal);

            // ps: 风气比调整值 目前在返回时置空(需求)
            ControlSettingWindDTO settingWindDTO = ControlSettingWindDTO.builder()
                    .number(entity.getNumber())
                    .windDataCode(entity.getWindDataCode())
                    .gasDataCode(entity.getGasDataCode())
                    .rateDcsVal(rateVal == null ? null : rateVal.doubleValue())
                    .rateManualVal(null)
                    .settingDcsVal(windVal == null ? null : windVal.doubleValue())// 助燃风设定值 dcs 值
                    .build();
            result.add(settingWindDTO);
        });

        return result;
    }

    @Override
    public Boolean publishWind(List<ControlSettingWindDTO> list) {
        // ps:手动下发,不校验下发规则

        List<ControlSettingWindDTO> publishWindSettingList = list.stream().filter(o -> ObjectUtils.isNotEmpty(o.getRateManualVal())).collect(Collectors.toList());

        List<String> publishWindDataCodeList = publishWindSettingList.stream().map(ControlSettingWindDTO::getWindDataCode).collect(Collectors.toList());

        // 1保存控制设定值
        // 1.1:先删除旧数据
        controlSettingWindService.deleteByDataCode(publishWindDataCodeList);
        // 1.2:再新增
        controlSettingWindService.saveWindSetting(publishWindSettingList);

        // 手动下发 TODO 目前暂无下发逻辑对接,暂时返回成功
        return true;
    }

    /**
     * 自动下发,仅天然气控制设定值(需要校验条规)
     */
    @Override
    public R gasAutoPublish() {

        // 1:获取当前智控计算值 (ps:查询算法)
        List<ControlSettingGasDTO> list = new ArrayList<>();

        // 2:获取当前DCS运行值 (ps:查询点位)

        // 3:本次控制变动值 = 智控计算值 - 当前DCS运行值 的绝对值  (ps:这里的逻辑暂时未完成  TODO)

        // 4. 责任链自动校验参数
        AnnotationAwareOrderComparator.sort(validators);
        for (RuleValidator validator : validators) {
            RuleValidateResult result = validator.validate(list);
            if (!result.isPass()) {
                return R.success(result.getMessage(), result.getErrors());
            }
        }

        // 5. 下发 暂时不开发
        return R.success();
    }
}
