// DISABLED: missing internal API dependencies
// package com.siact.hydrocore.sec.sevice;
//
// import com.alibaba.fastjson.JSONObject;
// import com.siact.api.common.api.vo.prop.NodePropFutureValQueryVo;
// import com.siact.api.common.api.vo.prop.PropRtValVo;
// import com.siact.hydrocore.sec.dto.*;
// import com.siact.hydrocore.sec.vo.CommonChartParamsVo;
// import com.siact.hydrocore.sec.vo.CumulativeDataVO;
// import com.siact.hydrocore.sec.vo.ExportCommonChartParamsVO;
//
// import javax.servlet.http.HttpServletResponse;
// import java.util.List;
// import java.util.Map;
// import java.util.Set;
//
// public interface DataService {
//
//     /**
//      * 查询柱状图、折线图等图表数据(量)
//      *
//      * @param vo
//      * @return
//      */
//     CommonChartResultDto queryCommonChartData(CommonChartParamsVo vo);
//
//     /**
//      * 查询某个时间段的量 --> AVG:均值;MAX:最大值;MIN:最小值;LAST:最新值;FIRST:最早值;SUM:累加;INC:增量;COUNT:数量
//      *
//      * @param dataCodes 数字孪生编码codes
//      * @param startTime 开始时间 yyyy-MM-dd hh:mm:ss
//      * @param endTime   结束时间 yyyy-MM-dd hh:mm:ss
//      * @param calcType  计算类型 AVG:均值;MAX:最大值;MIN:最小值;LAST:最新值;FIRST:最早值;SUM:累加;INC:增量;COUNT:数量
//      * @return {dataCode:value}
//      */
//     JSONObject queryBetweenVal(String dataCodes, String startTime, String endTime, String calcType);
//
//     /**
//      * 查询某个时间段的量 --> AVG:均值;MAX:最大值;MIN:最小值;LAST:最新值;FIRST:最早值;SUM:累加;INC:增量;COUNT:数量
//      * AVG:均值;MAX:最大值;MIN:最小值;LAST:最新值;FIRST:最早值;SUM:累加;INC:增量;COUNT:数量
//      *
//      * @param parms
//      * @return
//      */
//     JSONObject queryBetweenVal(IntervalValParamsDto parms);
//
//
//     /**
//      * 查询等时间间隔的量 --> AVG:均值;MAX:最大值;MIN:最小值;LAST:最新值;FIRST:最早值;SUM:累加;INC:增量;COUNT:数量
//      *
//      * @param dto
//      * @return
//      */
//     List<IntervalDataDto> queryIntervalVal(IntervalValParamsDto dto);
//
//
//     /**
//      * 查询实时值（最后一包数据）
//      *
//      * @param dataCodes
//      * @return
//      */
//     JSONObject queryRealValue(String dataCodes);
//
//
//     /**
//      * 查询节点下属性某个时间段的量 --> AVG:均值;MAX:最大值;MIN:最小值;LAST:最新值;FIRST:最早值;SUM:累加;INC:增量;COUNT:数量
//      *
//      * @param dataCode
//      * @param propModelCodes
//      * @param startTime
//      * @param endTime
//      * @param calcType
//      * @return
//      */
//     JSONObject queryNoteBetweenVal(String dataCode, String propModelCodes, String startTime, String endTime, String calcType);
//
//     /**
//      * 查询节点下属性等时间间隔的量
//      *
//      * @param dto
//      * @return
//      */
//     List<IntervalDataDto> queryNoteIntervalVal(IntervalNoteValParamsDto dto);
//
//     /**
//      * 导出(量)查询柱状图、折线图等图表数据
//      *
//      * @param response
//      * @param vo
//      */
//     void exportIntervalInstantData(HttpServletResponse response, ExportCommonChartParamsVO vo);
//
//
//     /**
//      * 查询累计值
//      *
//      * @param vo
//      * @return
//      */
//     List<CumulativeDataDTO> queryCumulativeData(CumulativeDataVO vo);
//
//     /**
//      * 根据实例编码和属性短码查询属性编码
//      *
//      * @param dataCodes
//      * @param shortCodes
//      * @return
//      */
//     List<PropInsDTO> queryPropCodeByInsCodeAndShortCode(String dataCodes, String shortCodes);
//
//     /**
//      * Compatibility path for the external data API. HydroCore baseline does not expose prediction as a default business capability.
//      *
//      * @param params
//      * @return
//      */
//     List<IntervalDataDto> queryForecastIntervalVal(NodePropFutureValQueryVo params);
// }
//