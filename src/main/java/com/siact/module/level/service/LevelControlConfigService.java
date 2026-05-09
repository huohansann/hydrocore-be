package com.siact.module.level.service;

import com.siact.module.level.dto.LevelControlConfigDTO;
import com.siact.module.level.vo.LevelControlConfigVO;

public interface LevelControlConfigService {
    LevelControlConfigVO getConfig();

    void saveConfig(LevelControlConfigDTO dto);

    void switchMode(String mode);
}