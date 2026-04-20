package com.siact.module.device.service;

import com.siact.common.vo.PageVO;
import com.siact.module.device.query.DeviceRealtimeQuery;
import com.siact.module.device.vo.DeviceRealtimeVO;
import com.siact.module.device.vo.SelectOptionVO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface DeviceRealtimeService {

    List<SelectOptionVO> listItemIds();

    List<SelectOptionVO> listDeviceNames();

    PageVO<DeviceRealtimeVO> query(DeviceRealtimeQuery query, int page, int pageSize);

    void export(DeviceRealtimeQuery query, String format, HttpServletResponse response);
}
