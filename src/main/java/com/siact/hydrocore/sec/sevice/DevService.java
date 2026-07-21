// DISABLED: missing internal API dependencies
// package com.siact.hydrocore.sec.sevice;
//
//
// import com.siact.api.common.api.vo.eq.EqListQueryVo;
// import com.siact.hydrocore.sec.dto.EqDypropInsDTO;
// import com.siact.hydrocore.sec.dto.EqStpropInsDTO;
//
// import java.util.List;
// import java.util.Map;
//
// public interface DevService {
//
//
//
//     /**
//      * 批量查询设备对应属性短码的动态属性信息
//      *
//      * dataCodes 数字孪生编码codes
//      * propGroups 属性分组（基础：base，静态：static，动态：dynamic,端点信息：rtnode）
//      * propTypes 属性类型, JC:监测,KZ:控制,NX:能效,NH:能耗,GZ:故障（告警、运维、检修）,MT:市场属性,C:碳计量属性,CG:可调属性
//      * propModelCodes 属性模型Code（属性模型短码）
//      * @return {Map<String, List<EqDypropInsVO>>}
//      */
//     Map<String, List<EqDypropInsDTO>> queryDeviceDynamicProp(EqListQueryVo vo);
//
//     /**
//      * 批量查询设备对应属性短码的静态属性信息
//      *
//      * dataCodes 数字孪生编码codes
//      * propGroups 属性分组（基础：base，静态：static，动态：dynamic,端点信息：rtnode）
//      * propTypes 属性类型, JC:监测,KZ:控制,NX:能效,NH:能耗,GZ:故障（告警、运维、检修）,MT:市场属性,C:碳计量属性,CG:可调属性
//      * propModelCodes 属性模型Code（属性模型短码）
//      * @return {Map<String, List<EqDypropInsVO>>}
//      */
//     Map<String, List<EqStpropInsDTO>> queryDeviceStaticProp(EqListQueryVo vo);
//
//
// }
//