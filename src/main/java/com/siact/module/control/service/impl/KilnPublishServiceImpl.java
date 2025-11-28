package com.siact.module.control.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.siact.api.common.api.vo.common.InfoListQueryVo;
import com.siact.common.R;
import com.siact.common.constant.ConstantNum;
import com.siact.common.constant.ConstantSymbol;
import com.siact.module.control.dto.ControlSettingGasDTO;
import com.siact.module.control.dto.ControlSettingWindDTO;
import com.siact.module.control.entity.ControlSettingGasEntity;
import com.siact.module.control.entity.ControlSettingWindEntity;
import com.siact.module.control.entity.GasValueEntity;
import com.siact.module.control.entity.IntelligentComputingEntity;
import com.siact.module.control.mapper.GasValueMapper;
import com.siact.module.control.mapper.IntelligentComputingMapper;
import com.siact.module.control.service.ControlSettingGasService;
import com.siact.module.control.service.ControlSettingWindService;
import com.siact.module.control.service.KilnPublishService;
import com.siact.module.control.validator.RuleValidateResult;
import com.siact.module.control.validator.RuleValidator;
import com.siact.sec.dto.EqDypropInsDTO;
import com.siact.sec.sevice.DataService;
import com.siact.sec.sevice.SecInsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
@Service
@Slf4j
public class KilnPublishServiceImpl implements KilnPublishService {
    @Autowired
    private ControlSettingGasService controlSettingGasService;

    @Autowired
    private ControlSettingWindService controlSettingWindService;

    @Autowired
    private DataService dataService;

    @Autowired
    private SecInsService secInsService;

    @Autowired
    private List<RuleValidator> validators;

    private @Resource IntelligentComputingMapper intelligentComputingMapper;

    private @Resource GasValueMapper gasValueMapper;

    @Override
    public List<ControlSettingGasDTO> getKilnGasControlSetting() {
        // 查询最后一条智能计算值
        LambdaQueryWrapper<IntelligentComputingEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(IntelligentComputingEntity::getResultTime);
        wrapper.last("limit 1");
        IntelligentComputingEntity intelligentComputingEntity = intelligentComputingMapper.selectOne(wrapper);
        // 获取结果
        JSONObject intelliComputingObj = JSON.parseObject(intelligentComputingEntity.getData());
        // 获取调用时刻的 dcs 天然气值
        LambdaQueryWrapper<GasValueEntity> gasValueWrapper = new LambdaQueryWrapper<>();
        gasValueWrapper.orderByDesc(GasValueEntity::getTime);
        gasValueWrapper.eq(GasValueEntity::getTime, intelligentComputingEntity.getResultTime());
        List<GasValueEntity> gasValues = gasValueMapper.selectList(gasValueWrapper);
        Map<String, GasValueEntity> gasValueMap = gasValues.stream().collect(Collectors.toMap(GasValueEntity::getDataCode, o -> o, (v1, v2) -> v1));

        List<ControlSettingGasDTO> result = new ArrayList<>();
        List<ControlSettingGasEntity> settingList = controlSettingGasService.getValidList();
        if (ObjectUtils.isEmpty(settingList)) {
            log.error("获取天然气控制设定值失败, 配置为空");
            return result;
        }
        settingList.sort(Comparator.comparing(ControlSettingGasEntity::getNumber));

        // 收集dataCode
        List<String> dataCodeList = settingList.stream().map(ControlSettingGasEntity::getDataCode).distinct().collect(Collectors.toList());

        // 根据dataCode+短码查询属性Code
        String xlsShortCode = "XLS";// 天然气设定流量短码
        List<String> propShortCode = Collections.singletonList(xlsShortCode);

        Map<String, String> secPropCodeMap = querySecPropCode(dataCodeList, propShortCode);
        // 查询DCS实时值  根据dataCode 查询
        JSONObject propCodeValObj = dataService.queryRealValue(String.join(",", secPropCodeMap.values()));
        // 封装返回结果
        settingList.forEach(entity -> {
            String mapKey = String.join(ConstantSymbol.UNDER_LINE, entity.getDataCode(), xlsShortCode);
            String propCode = secPropCodeMap.get(mapKey);// 获取属性Code
            BigDecimal xlsVal = propCodeValObj == null ? null : propCodeValObj.getBigDecimal(propCode);
            Double runningDcsVal = xlsVal == null ? null : xlsVal.doubleValue();
            // 获取智能计算值
            GasValueEntity gasValueEntity = gasValueMap.get(propCode);
            Double gasAlgorithmCalcVal = Objects.isNull(entity.getGasAlgorithmCalcVal()) ? null : entity.getGasAlgorithmCalcVal().doubleValue();
            if (!Objects.isNull(gasValueEntity)) {
                BigDecimal deltaC = intelliComputingObj.getJSONObject(gasValueEntity.getDataKey()).getJSONObject("method2").getBigDecimal("delta_C");
                gasAlgorithmCalcVal = gasValueEntity.getGasValue().add(deltaC).doubleValue();
            }
            // Double gasAlgorithmCalcVal = Objects.isNull(runningDcsVal) ? entity.getGasAlgorithmCalcVal().doubleValue() : deltaC.add(BigDecimal.valueOf(runningDcsVal)).doubleValue();
            // Double gasAlgorithmCalcVal = entity.getGasAlgorithmCalcVal() == null ? null : entity.getGasAlgorithmCalcVal().doubleValue();
            Double gasManualVal = entity.getGasManualVal() == null ? null : entity.getGasManualVal().doubleValue();

            if (entity.getAutoState()) {
                // 如果开启了自动下发,则将人工调整值设置为null
                gasManualVal = entity.getGasAlgorithmCalcVal() == null ? null : entity.getGasAlgorithmCalcVal().doubleValue();
            }

            result.add(new ControlSettingGasDTO(entity.getNumber(), entity.getDataCode(), runningDcsVal, gasAlgorithmCalcVal, gasManualVal, entity.getAutoState()));
        });

        return result;
    }


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
        Map<String, String> gasAndWindSecPropCodeMap = querySecPropCode(allDataCodeList, allPropShortCode);

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
                    .settingDcsVal(windVal == null ? null : windVal.doubleValue())// 助燃风设定值dcs值
                    .build();
            result.add(settingWindDTO);
        });

        return result;
    }

    @Override
    public Boolean publishGas(List<ControlSettingGasDTO> list) {
        // ps:手动下发,不校验下发规则

        List<ControlSettingGasDTO> publishGasSettingList = list.stream().filter(o -> ObjectUtils.isNotEmpty(o.getGasManualVal())).collect(Collectors.toList());

        for (ControlSettingGasDTO settingGasDTO : publishGasSettingList) {
            if (settingGasDTO.getAutoState()) {
                // 如果开启了自动下发,则将人工调整值置为null
                settingGasDTO.setGasManualVal(null);
            }
        }


        List<String> publishGasDataCodeList = publishGasSettingList.stream().map(ControlSettingGasDTO::getDataCode).collect(Collectors.toList());

        // 1保存控制设定值
        // 1.1:先删除旧数据
        controlSettingGasService.deleteByDataCode(publishGasDataCodeList);
        // 1.2:再新增
        controlSettingGasService.saveGasSetting(publishGasSettingList);

        // 手动下发 TODO 目前暂无下发逻辑对接,暂时返回成功
        return true;
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

    private Map<String, String> querySecPropCode(List<String> dataCodeList, List<String> propShortCode) {
        InfoListQueryVo infoListQueryVo = new InfoListQueryVo();
        infoListQueryVo.setDataCodes(new HashSet<>(dataCodeList));
        infoListQueryVo.setPropModelCodes(propShortCode);
        Map<String, List<EqDypropInsDTO>> result = secInsService.queryInsDynamicProp(infoListQueryVo);
        log.info("多个实例，一个属性短码，获取对应的属性长码对应,dataCodeList:{},propShortCode:{},result:{}", dataCodeList, propShortCode, result);

        HashMap<String, String> dataCodeShortMap = new HashMap<>();
        for (Map.Entry<String, List<EqDypropInsDTO>> entry : result.entrySet()) {
            String dataCode = entry.getKey();

            entry.getValue().forEach(eqDypropInsDTO -> {
                // 属性短码Code
                String propCode = eqDypropInsDTO.getPropCode();
                String propDataCode = eqDypropInsDTO.getDataCode();

                String mapKey = String.join(ConstantSymbol.UNDER_LINE, dataCode, propCode);
                dataCodeShortMap.put(mapKey, propDataCode);
            });
        }

        return dataCodeShortMap;
    }

    /**
     * 自动下发,仅天然气控制设定值(需要校验条规)
     *
     * @return
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
