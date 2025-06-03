package com.siact.module.model.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.common.utils.ConvertUtils;
import com.siact.module.base.service.TplService;
import com.siact.module.enmus.ModelStatusEnum;
import com.siact.module.model.dto.ModelConfigParamDetailDTO;
import com.siact.module.model.dto.ModelConfigParamRtnDTO;
import com.siact.module.model.dto.ModelInfoDTO;
import com.siact.module.model.entity.ModelConfigParamEntity;
import com.siact.module.model.mapper.ModelConfigParamMapper;
import com.siact.module.model.service.ModelConfigParamService;
import com.siact.module.model.service.ModelInfoService;
import com.siact.module.model.vo.ModelConfigParamSaveVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ModelConfigParamServiceImpl extends ServiceImpl<ModelConfigParamMapper, ModelConfigParamEntity> implements ModelConfigParamService {

    @Autowired
    private TplService tplService;

    @Autowired
    private ModelInfoService modelInfoService;

    @Override
    public Map<String, String> getParamTemplate() {
        // 公共设置
        List<ModelConfigParamDetailDTO> publicSetting = tplService.getListByCode("modelSettingPublicParam", ModelConfigParamDetailDTO.class);
        // 算法设置
        List<ModelConfigParamDetailDTO> algorithmSetting = tplService.getListByCode("modelSettingAlgorithmParam", ModelConfigParamDetailDTO.class);

        Map<String, String> rtnMap = new HashMap<>();
        rtnMap.put("publicSetting", JSON.toJSONString(publicSetting));
        rtnMap.put("algorithmSetting", JSON.toJSONString(algorithmSetting));
        return rtnMap;
    }

    @Override
    public ModelConfigParamRtnDTO queryParamByDataCodeAndPredictedTypeCode(String dataCode, String predictedTypeCode) {

        LambdaQueryWrapper<ModelConfigParamEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ModelConfigParamEntity::getDataCode, dataCode);
        queryWrapper.eq(ModelConfigParamEntity::getPredictedTypeCode, predictedTypeCode);
        ModelConfigParamEntity configParamEntity = baseMapper.selectOne(queryWrapper);

        ModelConfigParamRtnDTO rtnDTO = ConvertUtils.sourceToTarget(configParamEntity, ModelConfigParamRtnDTO.class);
        // 将数据库当中的json数据转化为对象
        rtnDTO.setPublicSetting(ConvertUtils.sourceToTarget(rtnDTO.getPublicSetting(), ModelConfigParamDetailDTO.class));
        rtnDTO.setAlgorithmSetting(ConvertUtils.sourceToTarget(rtnDTO.getAlgorithmSetting(), ModelConfigParamDetailDTO.class));

        return rtnDTO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateParam(ModelConfigParamSaveVO configParamSaveVo) {
        ModelConfigParamEntity entity = new ModelConfigParamEntity();
        entity.setId(configParamSaveVo.getId());
        entity.setDataCode(configParamSaveVo.getDataCode());
        entity.setPredictedType(configParamSaveVo.getPredictedType());
        entity.setPredictedTypeCode(configParamSaveVo.getPredictedTypeCode());
        entity.setPublicSetting(JSON.toJSONString(configParamSaveVo.getPublicSetting()));
        entity.setAlgorithmSetting(JSON.toJSONString(configParamSaveVo.getAlgorithmSetting()));

        saveOrUpdate(entity);
    }

    @Override
    public void sendParamById(Long id) {
        if (ObjectUtils.isEmpty(id)) {
            log.error("sendParamById. id is null");
            return;
        }

        ModelConfigParamEntity entity = baseMapper.selectById(id);
        if (ObjectUtils.isEmpty(entity)) {
            log.error("sendParamById. entity is null");
            return;
        }

        sendParamForModel(entity);
    }

    /**
     * 根据id下发参数至算法(ps:算法根据参数生成模型数据)
     *
     * @param entity
     */
    private void sendParamForModel(ModelConfigParamEntity entity) {
        HashMap<String, String> sendParamMap = buildSendParamForModel(entity);

        // TODO 调用算法接口 生成模型 (需要把id给算法  后续异步回调更新modelInfo的状态及其他数据信息)

        // 初始化 创建模型任务id
        ModelInfoDTO modelInfoDTO = new ModelInfoDTO();
        modelInfoDTO.setDataCode(entity.getDataCode());
        modelInfoDTO.setPredictedType(entity.getPredictedType());
        modelInfoDTO.setPredictedTypeCode(entity.getPredictedTypeCode());
        modelInfoDTO.setStatus(ModelStatusEnum.RUNNING.getValue());

        modelInfoService.saveModelInfo(modelInfoDTO);
    }

    /**
     * 构建下发生成模型的参数(TODO 对接算法时,可能会调整)
     * 逻辑: 将公共配置  和 算法配置  的paramCode和value做对应
     * 返回结果 k:paramCode v:value
     *
     * @param entity
     * @return
     */
    private HashMap<String, String> buildSendParamForModel(ModelConfigParamEntity entity) {
        if (ObjectUtils.isEmpty(entity)) {
            return null;
        }

        HashMap<String, String> sendParamMap = new HashMap<>();
        // 处理公共配置
        String publicSetting = entity.getPublicSetting();
        List<ModelConfigParamDetailDTO> publicParamDtoList = JSONArray.parseArray(publicSetting, ModelConfigParamDetailDTO.class);

        getParamMapByDto(publicParamDtoList, 0, sendParamMap);


        // 处理算法配置
        String algorithmSetting = entity.getAlgorithmSetting();
        List<ModelConfigParamDetailDTO> algorithmParamDtoList = JSONArray.parseArray(algorithmSetting, ModelConfigParamDetailDTO.class);

        getParamMapByDto(algorithmParamDtoList, 0, sendParamMap);

        return sendParamMap;
    }

    /**
     * 将参数Dto转化为Map结构   TODO 对接算法时,可能会调整
     *
     * @param publicParamDtoList
     * @param index
     * @param sendParamMap
     */
    private static void getParamMapByDto(List<ModelConfigParamDetailDTO> publicParamDtoList, int index, HashMap<String, String> sendParamMap) {
        // curDto为空  跳出递归
        if (index >= publicParamDtoList.size()) {
            return;
        }

        ModelConfigParamDetailDTO curDto = publicParamDtoList.get(index);

        String paramCode = curDto.getParamCode();
        String value = curDto.getValue();
        // 设置值
        sendParamMap.put(paramCode, value);

        List<ModelConfigParamDetailDTO> childrenList = curDto.getChildren();
        if (ObjectUtils.isNotEmpty(childrenList)) {
            // 递归子集
            getParamMapByDto(childrenList, index, sendParamMap);
        }
        // 递归下一级
        getParamMapByDto(publicParamDtoList, index++, sendParamMap);
    }
}
