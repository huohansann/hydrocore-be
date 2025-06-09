package com.siact.module.control.service;

import com.siact.common.R;
import com.siact.module.base.dto.KilnInfoDistributeDTO;

import java.util.List;

public interface KilnPublishService {

    R publish(List<KilnInfoDistributeDTO> list);
}
