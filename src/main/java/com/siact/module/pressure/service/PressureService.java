package com.siact.module.pressure.service;

import com.alibaba.fastjson.JSONObject;
import com.siact.module.pressure.dto.PressureDto;
import com.siact.module.pressure.dto.PressureQuery;
import com.siact.module.pressure.entity.PressureControlConfigEntity;
import com.siact.module.pressure.vo.PressureHistoryVO;

import java.util.List;

/**
 * @Author: HouBo
 * @Date: 2026/5/8 9:34
 * @Description: 窑压控制服务接口
 */
public interface PressureService {

    /**
     * @Author: HouBo
     * @Date: 2026/5/8 9:42
     * @Description: 根据dataCode查询数据
     */
    JSONObject getModelData(PressureDto pressureDto);

    /**
     * @Author: HouBo
     * @Date: 2026/5/8 11:50
     * @Description: 查询窑压控制参数
     */
    List<PressureControlConfigEntity> selectAll();

    /**
     * @Author: HouBo
     * @Date: 2026/5/8 11:50
     * @Description: 修改窑压控制参数
     */
    int updateAll(List<PressureControlConfigEntity> list);

    /**
     * @Author: HouBo
     * @Date: 2026/5/8 15:40
     * @Description: 查询窑压历史数据
     */
    PressureHistoryVO queryHistory(PressureQuery query);
}
