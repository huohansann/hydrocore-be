package com.siact.module.levelcontrol.service.impl;

import com.siact.module.levelcontrol.dto.LevelControlConfigDTO;
import com.siact.module.levelcontrol.entity.LevelControlConfigEntity;
import com.siact.module.levelcontrol.enums.LevelControlModeEnum;
import com.siact.module.levelcontrol.repository.LevelControlConfigRepository;
import com.siact.module.levelcontrol.service.LevelControlConfigService;
import com.siact.module.levelcontrol.vo.LevelControlConfigVO;
import com.siact.module.system.constants.SysConfigCodeConstants;
import com.siact.module.system.dto.SysConfigDTO;
import com.siact.module.system.service.SysConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class LevelControlConfigServiceImpl implements LevelControlConfigService {

    private final LevelControlConfigRepository repository;
    private final SysConfigService sysConfigService;

    @SuppressWarnings("unchecked")
    private String getLevelCode() {
        SysConfigDTO config = sysConfigService.getByCode(SysConfigCodeConstants.LEVEL_CONTROL_DATACODES);
        if (config != null && config.getData() instanceof Map) {
            Map<String, Object> dataMap = (Map<String, Object>) config.getData();
            return String.valueOf(dataMap.get("level"));
        }
        return null;
    }

    @Override
    public LevelControlConfigVO getConfig() {
        String dataCode = getLevelCode();
        if (dataCode == null) {
            return null;
        }
        LevelControlConfigEntity entity = repository.getByDataCode(dataCode);
        if (entity == null) {
            return null;
        }
        return toVO(entity);
    }

    @Override
    public void saveConfig(LevelControlConfigDTO dto) {
        LevelControlModeEnum.fromCode(dto.getMode());
        String dataCode = getLevelCode();

        LevelControlConfigEntity entity = new LevelControlConfigEntity();
        entity.setDataCode(dataCode);
        entity.setMode(dto.getMode());
        entity.setAiPredictWindow(dto.getAiPredictWindow());
        entity.setAiPredictDuration(dto.getAiPredictDuration());
        entity.setPidPb(dto.getPidPb());
        entity.setPidTi(dto.getPidTi());
        entity.setPidTd(dto.getPidTd());
        entity.setManualControlValue(dto.getManualControlValue());
        entity.setSafeLimit(dto.getSafeLimit());
        entity.setOpeningUpperLimit(dto.getOpeningUpperLimit());
        repository.saveOrUpdate(entity);
    }

    @Override
    public void switchMode(String mode) {
        LevelControlModeEnum.fromCode(mode);
        String dataCode = getLevelCode();

        LevelControlConfigEntity entity = new LevelControlConfigEntity();
        entity.setDataCode(dataCode);
        entity.setMode(mode);
        repository.saveOrUpdate(entity);
    }

    private LevelControlConfigVO toVO(LevelControlConfigEntity entity) {
        LevelControlConfigVO vo = new LevelControlConfigVO();
        vo.setDataCode(entity.getDataCode());
        vo.setMode(entity.getMode());
        vo.setAiPredictWindow(entity.getAiPredictWindow());
        vo.setAiPredictDuration(entity.getAiPredictDuration());
        vo.setPidPb(entity.getPidPb());
        vo.setPidTi(entity.getPidTi());
        vo.setPidTd(entity.getPidTd());
        vo.setManualControlValue(entity.getManualControlValue());
        vo.setSafeLimit(entity.getSafeLimit());
        vo.setOpeningUpperLimit(entity.getOpeningUpperLimit());
        return vo;
    }
}
