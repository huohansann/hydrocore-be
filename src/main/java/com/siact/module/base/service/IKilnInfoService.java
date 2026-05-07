package com.siact.module.base.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.module.base.dto.KilnInfoDTO;
import com.siact.module.base.dto.KilnInfoDistributeDTO;
import com.siact.module.base.dto.KilnInfoGasFlowDTO;
import com.siact.module.base.dto.KilnInfoQuery;
import com.siact.module.base.dto.KilnInfoWindDisDTO;
import com.siact.module.base.entity.KilnInfoEntity;
import com.siact.module.base.vo.KilnInfoVO;

import java.util.List;

/**
 * 炉子基本信息配置 服务接口
 */
public interface IKilnInfoService extends IService<KilnInfoEntity> {
    KilnInfoVO selectKilnInfoById(Long id);
    List<KilnInfoVO> selectKilnInfoList(KilnInfoQuery query);
    int insertKilnInfo(KilnInfoDTO dto);
    int updateKilnInfo(KilnInfoDTO dto);
    int deleteKilnInfoByIds(Long[] ids);

    int logicalDeleteKilnInfoByIds(Long[] ids);

    int saveKilnInfoBatch(List<KilnInfoDTO> list);

    int updateDistribute(List<KilnInfoDistributeDTO> list);

    int updateGasFlow(List<KilnInfoGasFlowDTO> list);

    int updateWindDis(List<KilnInfoWindDisDTO> list);

}