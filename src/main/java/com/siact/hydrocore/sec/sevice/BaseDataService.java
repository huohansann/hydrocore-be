package com.siact.hydrocore.sec.sevice;


import com.alibaba.fastjson.JSONObject;
import com.siact.hydrocore.module.base.dto.ColumnChartDTO;
import com.siact.hydrocore.sec.dto.AttributeBetweenValVO;
import com.siact.hydrocore.sec.dto.AttributeIntervalValParamsDto;
import com.siact.hydrocore.sec.dto.CumulativeDataDTO;
import com.siact.hydrocore.sec.dto.IntervalDataDto;
import com.siact.hydrocore.sec.dto.IntervalValParamsDto;
import com.siact.hydrocore.sec.vo.CloumChartParmsVO;
import com.siact.hydrocore.sec.vo.CumulativeDataVO;

import java.util.List;

/**
 * @desc:
 * @author: zhangwentao
 * @create: 2025-04-15 16:46
 */

public interface BaseDataService {


    /**
     * @desc: 获取柱状图数据：包含折线图数据名称，code，单位
     * @author: zhangwentao
     * @create: 2025-04-15 16:46
     */
    ColumnChartDTO getColumnChartInfo(CloumChartParmsVO projectPropVO);


    /**
     * @desc: 获取实时数据
     * @author: zhangwentao
     * @create: 2025-04-15 16:46
     */
    JSONObject queryRealTimeInfo(List<String> dataCode);

    /**
     * @desc: 获取历史数据:等间隔的数据
     * @author: zhangwentao
     * @create: 2025-04-15 16:46
     */
    List<IntervalDataDto> queryAttributeIntervalVal(AttributeIntervalValParamsDto dto);

    /**
     * 获取历史数据:时间段内的数据
     *
     * @param vo
     * @return
     */
    JSONObject queryBetweenVal(AttributeBetweenValVO vo);

    /**
     * 系统累计数据(同步-环比
     *
     * @param vo
     * @return
     */
    List<CumulativeDataDTO> queryCumulativeData(CumulativeDataVO vo);

    /**
     * 解析属性参数
     *
     * @param vo
     * @return
     */
    IntervalValParamsDto parseAttributeParams(IntervalValParamsDto vo);
}