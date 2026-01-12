package com.siact.module.control.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.common.constant.ConstantSymbol;
import com.siact.common.exception.BizException;
import com.siact.core.event.domain.GenericEvent;
import com.siact.core.event.notify.EventPublisher;
import com.siact.module.algorithm.entity.IntelligentDataEntity;
import com.siact.module.algorithm.enums.IntelliTypeEnum;
import com.siact.module.algorithm.repository.IntelligentDataRepository;
import com.siact.module.control.convert.ControlSettingGasConvert;
import com.siact.module.control.dto.ControlSettingGasDTO;
import com.siact.module.control.entity.ControlGasRecordEntity;
import com.siact.module.control.entity.ControlSettingGasEntity;
import com.siact.module.control.event.GasRecordSaveEventHandler;
import com.siact.module.control.mapper.ControlSettingGasMapper;
import com.siact.module.control.repository.ControlGasRecordRepository;
import com.siact.module.control.repository.ControlSettingGasRepository;
import com.siact.module.control.service.ControlSettingGasService;
import com.siact.module.control.support.ControlSettingSupport;
import com.siact.sec.sevice.DataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class ControlSettingGasServiceImpl extends ServiceImpl<ControlSettingGasMapper, ControlSettingGasEntity> implements ControlSettingGasService {
    private final ControlSettingGasRepository repository;
    private final ControlGasRecordRepository recordRepository;
    private final IntelligentDataRepository intelligentDataRepository;
    private final ControlSettingSupport support;
    private final EventPublisher publisher;
    private final ControlSettingGasConvert convert;
    private final DataService dataService;

    /**
     * 查询天然气设定值
     */
    @Override
    public List<ControlSettingGasDTO> querySetting() {
        // 获取智控值
        Map<String, Map<IntelliTypeEnum, IntelligentDataEntity>> intelliValues = intelligentDataRepository.queryByTypeWithLastTime(IntelliTypeEnum.GAS_RUN_VALUE, IntelliTypeEnum.GAS_CALC_EXPERT2);

        List<ControlSettingGasDTO> result = new ArrayList<>();
        List<ControlSettingGasEntity> settingList = repository.queryValid();
        if (ObjectUtils.isEmpty(settingList)) {
            log.warn("获取天然气控制设定值信息, 配置为空");
            return result;
        }
        settingList.sort(Comparator.comparing(ControlSettingGasEntity::getNumber));

        // 收集 dataCode
        List<String> dataCodeList = settingList.stream().map(ControlSettingGasEntity::getDataCode).distinct().collect(Collectors.toList());

        // 根据 dataCode + 短码查询属性 Code
        String xlsShortCode = "XLS"; // 天然气设定流量短码
        List<String> propShortCode = Collections.singletonList(xlsShortCode);

        Map<String, String> secPropCodeMap = support.querySecPropCode(dataCodeList, propShortCode);
        // 查询DCS实时值, 根据 dataCode 查询
        JSONObject propCodeValObj = dataService.queryRealValue(String.join(",", secPropCodeMap.values()));
        // 封装返回结果
        settingList.forEach(entity -> {
            ControlSettingGasDTO dto = convert.toDTO(entity);
            // 获取 dcs 运行值
            String mapKey = String.join(ConstantSymbol.UNDER_LINE, entity.getDataCode(), xlsShortCode);
            BigDecimal xlsVal = propCodeValObj == null ? null : propCodeValObj.getBigDecimal(secPropCodeMap.get(mapKey));
            Double runningDcsVal = xlsVal == null ? null : xlsVal.doubleValue();
            dto.setRunningDcsVal(runningDcsVal);
            dto.setAlgoDiff(false);

            // 获取智能计算值
            Map<IntelliTypeEnum, IntelligentDataEntity> intelliValueMap = intelliValues.get(entity.getDataCode());
            IntelligentDataEntity intelliRunValue = intelliValueMap.get(IntelliTypeEnum.GAS_RUN_VALUE);
            IntelligentDataEntity intelliModelValue = intelliValueMap.get(IntelliTypeEnum.GAS_CALC_EXPERT2);
            if (!Objects.isNull(intelliRunValue) && !Objects.isNull(intelliModelValue)) {
                dto.setGasAlgorithmCalcVal(intelliRunValue.getVal().add(intelliModelValue.getVal()).doubleValue());
                dto.setAdjustValue(intelliModelValue.getVal());
            }
            result.add(dto);
        });
        this.setStatusAndRecord(result); // 记录状态并触发保存事件
        return result;
    }

    /* 设置信号灯状态和保存 dcs 天然气记录值 */
    private void setStatusAndRecord(List<ControlSettingGasDTO> list) {
        // 获取上一次天然气运行值
        Map<String, ControlGasRecordEntity> recordMap = recordRepository.queryWithLastTime();
        // 获取上一次智控输出
        Map<String, Map<IntelliTypeEnum, List<IntelligentDataEntity>>> intelliValues = intelligentDataRepository.queryByTypeAndLimit("limit 1, 1", IntelliTypeEnum.GAS_RUN_VALUE, IntelliTypeEnum.GAS_CALC_EXPERT2);
        Map<String, List<IntelligentDataEntity>> collect = intelliValues.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                entry -> entry.getValue().values().stream().flatMap(List::stream).collect(Collectors.toList()))
        );
        // 计算上一次智控输出
        Map<String, BigDecimal> lastAlgos = collect.entrySet().stream()
                .filter(entry -> CollectionUtils.isNotEmpty(entry.getValue()) && entry.getValue().size() >= 2 && entry.getValue().stream().allMatch(Objects::nonNull))
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().stream().map(IntelligentDataEntity::getVal).reduce(BigDecimal.ZERO, BigDecimal::add)));


        Timestamp currentTime = new Timestamp(System.currentTimeMillis());

        List<ControlGasRecordEntity> records = list.stream().map(dto -> {
                    BigDecimal currentDcs = NumberUtils.toScaledBigDecimal(dto.getRunningDcsVal(), 2, RoundingMode.HALF_UP);
                    BigDecimal algo = NumberUtils.toScaledBigDecimal(dto.getGasAlgorithmCalcVal(), 2, RoundingMode.HALF_UP);
                    // dcs 运行值是否与智控值不同
                    boolean isDcsDiffAlgo = !currentDcs.equals(algo);
                    // 智控输出是否和上一次不一样
                    BigDecimal lastAlgo = NumberUtils.toScaledBigDecimal(lastAlgos.get(dto.getDataCode()), 2, RoundingMode.HALF_UP);
                    boolean isAlgoDiff = !lastAlgo.equals(algo);
                    // dcs 运行值是否和上一次不一样
                    ControlGasRecordEntity record = recordMap.get(dto.getDataCode());
                    boolean isDcsDiff = false;
                    if (ObjectUtils.isNotEmpty(record) && ObjectUtils.isNotEmpty(record.getDcs())) {
                        String last = record.getDcs().setScale(2, RoundingMode.HALF_UP).toPlainString();
                        String current = currentDcs.setScale(2, RoundingMode.HALF_UP).toPlainString();
                        isDcsDiff = !last.equals(current);
                    }
                    log.info("dcs 与智控值不同: {}, dcs 与上一次不同: {}, 智控值与上一次不同: {}", isDcsDiffAlgo, isAlgoDiff, isDcsDiff);
                    dto.setDcsDiff(isDcsDiffAlgo && isDcsDiff);
                    dto.setAlgoDiff(isDcsDiffAlgo && isAlgoDiff);

                    return ControlGasRecordEntity.builder().code(dto.getDataCode()).dcs(currentDcs).status(dto.getDcsDiff()).time(currentTime).build();
                }
        ).collect(Collectors.toList());

        publisher.publish(GenericEvent.of(GasRecordSaveEventHandler.EVENT_TYPE, records));
    }

    /**
     * 下发天然气设置
     */
    @Override
    public @Transactional(rollbackFor = BizException.class) Boolean publish(List<ControlSettingGasDTO> list) {
        // ps:手动下发, 不校验下发规则
        List<ControlSettingGasDTO> publishList = list.stream().filter(o -> ObjectUtils.isNotEmpty(o.getGasManualVal())).collect(Collectors.toList());

        // 获取历史数据, 对自动模式的数据, 将调整值设置为原本的数据, 对手动模式的调整值保持
        List<ControlSettingGasEntity> settingList = repository.queryValid();
        for (ControlSettingGasDTO dto : publishList) {
            if (dto.getAutoState()) {
                // 自动模式的设定值设置为原数据的设定值
                settingList.stream().filter(gas -> gas.getDataCode().equals(dto.getDataCode())).findFirst().ifPresent(
                        entity -> dto.setGasManualVal(entity.getGasManualVal().doubleValue())
                );
            }
        }
        List<String> publishGasDataCodeList = publishList.stream().map(ControlSettingGasDTO::getDataCode).collect(Collectors.toList());

        // 1保存控制设定值
        // 1.1:先删除旧数据
        repository.deleteByDataCode(publishGasDataCodeList);
        // 1.2:再新增
        repository.save(publishList);

        // 手动下发 TODO 目前暂无下发逻辑对接,暂时返回成功
        return true;
    }
}
