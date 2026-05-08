package com.siact.module.levelcontrol.service;

import com.siact.module.levelcontrol.dto.LevelControlConfigDTO;
import com.siact.module.levelcontrol.vo.LevelControlConfigVO;

public interface LevelControlConfigService {
    LevelControlConfigVO getConfig();

    void saveConfig(LevelControlConfigDTO dto);

    void switchMode(String mode);
}