// DISABLED: missing internal API dependencies
// package com.siact.hydrocore.sec.controller;
//
//
// import com.siact.api.common.api.vo.eq.EqListQueryVo;
// import com.siact.hydrocore.common.api.ApiResponse;
// import com.siact.hydrocore.sec.dto.EqDypropInsDTO;
// import com.siact.hydrocore.sec.dto.EqStpropInsDTO;
// import com.siact.hydrocore.sec.sevice.DevService;
// import io.swagger.annotations.Api;
// import io.swagger.annotations.ApiOperation;
// import lombok.extern.slf4j.Slf4j;
// import org.apache.commons.collections4.CollectionUtils;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.validation.annotation.Validated;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RestController;
//
// import java.util.List;
// import java.util.Map;
//
//
// @Slf4j
// @RestController
// @Api(tags="数字孪生查询设备")
// @RequestMapping("/api/dev")
// public class DevController {
//
//
//
//     @Autowired
//     private DevService devService;
//
//
//     /**
//      * 批量查询设备对应属性短码的动态属性信息(可通过实例长码+属性短码获取属性长码)
//      *
//      * dataCodes 数字孪生编码codes
//      * propTypes 属性类型, JC:监测,KZ:控制,NX:能效,NH:能耗,GZ:故障（告警、运维、检修）,MT:市场属性,C:碳计量属性,CG:可调属性
//      * propModelCodes 属性模型Code（属性模型短码）
//      * @return {List<EqVO>}
//      */
//     @ApiOperation("批量查询设备对应属性短码的动态属性信息(可通过实例长码+属性短码获取属性长码)")
//     @PostMapping("/queryDeviceDynamicProp")
//     public ApiResponse<Map<String, List<EqDypropInsDTO>>> queryDeviceDynamicProp(@RequestBody @Validated EqListQueryVo vo) {
//         List<String> dataCodes = vo.getDataCodes();
//         if (CollectionUtils.isEmpty(dataCodes)) {
//             throw new RuntimeException("查询某个时间段的量参数校验不通过");
//         }
//
//         return ApiResponse.success(devService.queryDeviceDynamicProp(vo));
//     }
//
//     /**
//      * 批量查询设备对应属性短码的静态属性信息(可通过实例长码+属性短码获取属性长码)
//      *
//      * dataCodes 数字孪生编码codes
//      * propTypes 属性类型, JC:监测,KZ:控制,NX:能效,NH:能耗,GZ:故障（告警、运维、检修）,MT:市场属性,C:碳计量属性,CG:可调属性
//      * propModelCodes 属性模型Code（属性模型短码）
//      * @return {List<EqVO>}
//      */
//     @ApiOperation("批量查询设备对应属性短码的静态属性信息(可通过实例长码+属性短码获取属性长码)")
//     @PostMapping("/queryDeviceStaticProp")
//     public ApiResponse<Map<String, List<EqStpropInsDTO>>> queryDeviceStaticProp(@RequestBody @Validated EqListQueryVo vo) {
//         List<String> dataCodes = vo.getDataCodes();
//         if (CollectionUtils.isEmpty(dataCodes)) {
//             throw new RuntimeException("查询某个时间段的量参数校验不通过");
//         }
//
//         return ApiResponse.success(devService.queryDeviceStaticProp(vo));
//     }
// }
//