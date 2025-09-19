package com.siact.module.control.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.siact.api.common.api.vo.common.InfoListQueryVo;
import com.siact.common.R;
import com.siact.common.constant.ConstantSymbol;
import com.siact.module.base.dto.KilnInfoDistributeDTO;
import com.siact.module.base.service.IKilnInfoService;
import com.siact.module.control.dto.ControlSettingGasDTO;
import com.siact.module.control.dto.ControlSettingWindDTO;
import com.siact.module.control.entity.ControlSettingGasEntity;
import com.siact.module.control.entity.ControlSettingWindEntity;
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

import java.math.BigDecimal;
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
    private IKilnInfoService kilnInfoService;

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

    @Override
    public List<ControlSettingGasDTO> getKilnGasControlSetting() {
        List<ControlSettingGasDTO> result = new ArrayList<>();
        List<ControlSettingGasEntity> settingList = controlSettingGasService.getValidList();

        // 收集dataCode
        List<String> dataCodeList = settingList.stream().map(ControlSettingGasEntity::getDataCode).distinct().collect(Collectors.toList());

        // 根据dataCode+短码查询属性Code
        String xlsShortCode = "XLS";// 天然气设定流量短码
        List<String> propShortCode = Collections.singletonList(xlsShortCode);

        Map<String, String> secPropCodeMap = querySecPropCode(dataCodeList, propShortCode);
        // 查询DCS实时值  根据dataCode 查询
        JSONObject propCodeValObj = dataService.queryRealValue(String.join(",", secPropCodeMap.values()));

        // 封装返回结果
        if (ObjectUtils.isNotEmpty(settingList)) {
            settingList.forEach(entity -> {

                String mapKey = String.join(ConstantSymbol.UNDER_LINE, entity.getDataCode(), xlsShortCode);
                String propCode = secPropCodeMap.get(mapKey);// 获取属性Code
                BigDecimal xlsVal = propCodeValObj == null ? null : propCodeValObj.getBigDecimal(propCode);
                Double runningDcsVal = xlsVal == null ? null : xlsVal.doubleValue();
                Double gasAlgorithmCalcVal = entity.getGasAlgorithmCalcVal() == null ? null : entity.getGasAlgorithmCalcVal().doubleValue();
                Double gasManualVal = entity.getGasManualVal() == null ? null : entity.getGasManualVal().doubleValue();

                Double changeValue =
                        (gasAlgorithmCalcVal == null
                            || runningDcsVal == null
                            || gasAlgorithmCalcVal - runningDcsVal == 0
                        ) ? null : runningDcsVal - gasManualVal;
                result.add(new ControlSettingGasDTO(entity.getNumber(), entity.getDataCode(), runningDcsVal, gasAlgorithmCalcVal, changeValue,gasManualVal, entity.getAutoState()));
            });
        }

        return result;
    }


    @Override
    public List<ControlSettingWindDTO> getKilnWindControlSetting() {

        List<ControlSettingWindDTO> result = new ArrayList<>();

        List<ControlSettingWindEntity> settingList = controlSettingWindService.getValidList();

        // 收集dataCode
        List<String> dataCodeList = settingList.stream().map(ControlSettingWindEntity::getDataCode).distinct().collect(Collectors.toList());

        // 根据dataCode+短码查询属性Code
        String rateShortCode = "XLS";// 风气比短码  TODO 风气比短码暂时没有,先拿设定值的
        String xlsShortCode = "XLS";// 风气比设定值短码
        List<String> propShortCode = Arrays.asList(rateShortCode, xlsShortCode);

        Map<String, String> secPropCodeMap = querySecPropCode(dataCodeList, propShortCode);
        // 查询DCS实时值  根据dataCode 查询
        JSONObject propCodeValObj = dataService.queryRealValue(String.join(",", secPropCodeMap.values()));

        // 封装返回结果
        if (ObjectUtils.isNotEmpty(settingList)) {
            settingList.forEach(entity -> {
                // 解析风气比
                String rateMapKey = String.join(ConstantSymbol.UNDER_LINE, entity.getDataCode(), rateShortCode);
                String ratePropCode = secPropCodeMap.get(rateMapKey);// 获取属性Code
                BigDecimal rateVal = propCodeValObj == null ? null : propCodeValObj.getBigDecimal(ratePropCode);// 风气比值
                // 解析风气比设定值
                String xlsMapKey = String.join(ConstantSymbol.UNDER_LINE, entity.getDataCode(), xlsShortCode);
                String xlsPropCode = secPropCodeMap.get(xlsMapKey);// 获取属性Code
                BigDecimal xlsVal = propCodeValObj == null ? null : propCodeValObj.getBigDecimal(xlsPropCode);// 风气比设定值

                // ps: 风气比调整值 目前在返回时置空(需求)
                result.add(new ControlSettingWindDTO(entity.getNumber(), entity.getDataCode(),
                        rateVal == null ? null : rateVal.doubleValue(),
                        null,
                        xlsVal == null ? null : xlsVal.doubleValue()));
            });
        }

        return result;
    }

    @Override
    public Boolean publishGas(List<ControlSettingGasDTO> list) {
        // ps:手动下发,不校验下发规则

        List<ControlSettingGasDTO> publishGasSettingList = list.stream().filter(o -> ObjectUtils.isNotEmpty(o.getGasManualVal())).collect(Collectors.toList());

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

        List<String> publishWindDataCodeList = publishWindSettingList.stream().map(ControlSettingWindDTO::getDataCode).collect(Collectors.toList());

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
