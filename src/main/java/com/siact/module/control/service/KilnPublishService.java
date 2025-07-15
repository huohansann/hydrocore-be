package com.siact.module.control.service;

import com.siact.common.R;
import com.siact.module.base.dto.KilnInfoDistributeDTO;

import java.util.List;

public interface KilnPublishService {

    /**
     * 手动下发
     * @param list
     * @return
     */
    R publish(List<KilnInfoDistributeDTO> list);

    /**
     * 自动下发
     * @return
     */
    R autoPublish();
}
