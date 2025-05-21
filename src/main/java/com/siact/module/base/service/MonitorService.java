package com.siact.module.base.service;

import com.siact.module.base.dto.Load;

/**
 * @author Bruce_Hmz
 * @date 2025/4/27
 */
public interface MonitorService {


    /**
     * 查询负载率信息
     * @param dataCode
     * @return
     */
    Load queryLoadRate(String dataCode);
}
