package com.siact.sec.controller;

import com.siact.api.common.api.vo.common.InfoListQueryVo;
import com.siact.sec.dto.EqDypropInsDTO;
import com.siact.sec.dto.EqStpropInsDTO;
import com.siact.sec.dto.InsTreeDTO;
import com.siact.sec.dto.TMInsSimpleDTO;
import com.siact.sec.sevice.SecInsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@Api(tags="数字孪生查询实例")
@RequestMapping("/api/ins")
public class SecInsController {
    @Autowired
    private SecInsService secInsService;

    /**
     * 查询实例下使用指定设备模型长码的设备
     *
     * dataCode 数字孪生编码codes
     * modelDataCode 设备模板长码code
     * @return {List<TMInsSimpleDTO>}
     */
    @ApiOperation("查询实例下使用指定设备模型长码的设备")
    @GetMapping("/queryInsListByModeCode")
    public List<TMInsSimpleDTO> queryInsListByModeCode(String dataCode, String modelDataCode) {
        if (StringUtils.isEmpty(dataCode) || StringUtils.isEmpty(modelDataCode)) {
            throw new RuntimeException("查询实例下使用指定设备模型长码的设备参数校验不通过");
        }

        return secInsService.queryInsListByModeCode(dataCode, modelDataCode);
    }

    /**
     * 查询实例下所有设备
     * @param dataCode
     * @return
     */
    @ApiOperation("查询实例下所有设备")
    @GetMapping("/queryAllDevUnderIns")
    public List<InsTreeDTO> queryAllDevUnderIns(String dataCode) {
        if (StringUtils.isEmpty(dataCode)) {
            throw new RuntimeException("查询实例下使用指定设备模型长码的设备参数校验不通过");
        }

        return secInsService.queryAllDevUnderIns(dataCode);
    }

    /**
     * 查询实例树
     * @param dataCode 数字化编码,不传则查询项目实例。
     * @param hasEq 是否需要查询设备,默认false。
     * @param hasSub 是否需要查询孙子节点,默认false。
     * @return
     */
    @ApiOperation("查询实例树")
    @GetMapping("/queryTree")
    public List<InsTreeDTO> queryTree(String dataCode, boolean hasEq, boolean hasSub) {

        return secInsService.queryTree(dataCode, hasEq, hasSub);
    }

    /**
     * 批量查询实例对应属性短码的动态属性信息(可通过实例长码+属性短码获取属性长码)
     *
     * dataCodes 数字孪生编码codes
     * propTypes 属性类型, JC:监测,KZ:控制,NX:能效,NH:能耗,GZ:故障（告警、运维、检修）,MT:市场属性,C:碳计量属性,CG:可调属性
     * propModelCodes 属性模型Code（属性模型短码）
     * @return {List<EqVO>}
     */
    @ApiOperation("批量查询实例对应属性短码的动态属性信息(可通过实例长码+属性短码获取属性长码)")
    @PostMapping("/queryInsDynamicProp")
    public Map<String, List<EqDypropInsDTO>> queryInsDynamicProp(@RequestBody @Validated InfoListQueryVo vo) {
        Set<String> dataCodes = vo.getDataCodes();
        if (CollectionUtils.isEmpty(dataCodes)) {
            throw new RuntimeException("批量查询实例对应属性短码的动态属性信息(可通过实例长码+属性短码获取属性长码)参数校验不通过");
        }

        return secInsService.queryInsDynamicProp(vo);
    }

    /**
     * 批量查询实例对应属性短码的静态属性信息(可通过实例长码+属性短码获取属性长码)
     *
     * dataCodes 数字孪生编码codes
     * propTypes 属性类型, JC:监测,KZ:控制,NX:能效,NH:能耗,GZ:故障（告警、运维、检修）,MT:市场属性,C:碳计量属性,CG:可调属性
     * propModelCodes 属性模型Code（属性模型短码）
     * @return {List<EqVO>}
     */
    @ApiOperation("批量查询实例对应属性短码的静态属性信息(可通过实例长码+属性短码获取属性长码)")
    @PostMapping("/queryInsStaticProp")
    public Map<String, List<EqStpropInsDTO>> queryInsStaticProp(@RequestBody @Validated InfoListQueryVo vo) {
        Set<String> dataCodes = vo.getDataCodes();
        if (CollectionUtils.isEmpty(dataCodes)) {
            throw new RuntimeException("批量查询实例对应属性短码的静态属性信息(可通过实例长码+属性短码获取属性长码)参数校验不通过");
        }

        return secInsService.queryInsStaticProp(vo);
    }
}
