package com.siact.hydrocore.module.device.service;

import com.siact.hydrocore.common.vo.PageVO;
import com.siact.hydrocore.module.device.query.DeviceRealtimeQuery;
import com.siact.hydrocore.module.device.vo.DeviceRealtimeVO;
import com.siact.hydrocore.module.device.vo.SelectOptionVO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface DeviceRealtimeService {

    List<SelectOptionVO> listItemIds();

    List<SelectOptionVO> listDeviceNames();

    PageVO<DeviceRealtimeVO> query(DeviceRealtimeQuery query, int page, int pageSize);

    void export(DeviceRealtimeQuery query, String format, HttpServletResponse response);
}
