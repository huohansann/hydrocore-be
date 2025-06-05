package com.siact.module.control.service;

import com.siact.module.base.dto.KilnInfoDistributeDTO;

import java.util.List;

public interface KilnPublishService {

    void publish(List<KilnInfoDistributeDTO> list);
}
