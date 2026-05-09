package com.siact.module.level.service.impl;

import com.siact.module.level.convert.LevelControlConfigConvert;
import com.siact.module.level.dto.LevelControlConfigDTO;
import com.siact.module.level.entity.LevelControlConfigEntity;
import com.siact.module.level.enums.LevelControlModeEnum;
import com.siact.module.level.repository.LevelControlConfigRepository;
import com.siact.module.level.service.LevelControlConfigService;
import com.siact.module.level.vo.LevelControlConfigVO;
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
    private final LevelControlConfigConvert convert;

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
        return convert.toVO(entity);
    }

    @Override
    public void saveConfig(LevelControlConfigDTO dto) {
        LevelControlModeEnum.fromCode(dto.getMode());
        String dataCode = getLevelCode();

        LevelControlConfigEntity entity = convert.toEntity(dto);
        entity.setDataCode(dataCode);
        repository.saveOrUpdate(entity);
    }

    @Override
    public void switchMode(String mode) {
        LevelControlModeEnum.fromCode(mode);
        String dataCode = getLevelCode();

        LevelControlConfigEntity existing = repository.getByDataCode(dataCode);
        LevelControlConfigEntity entity = existing != null
                ? convert.copy(existing)
                : new LevelControlConfigEntity();
        entity.setDataCode(dataCode);
        entity.setMode(mode);
        repository.saveOrUpdate(entity);
    }
}
