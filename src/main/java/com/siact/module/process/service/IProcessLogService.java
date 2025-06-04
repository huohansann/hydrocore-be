package com.siact.module.process.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.module.process.entity.ProcessLogEntity;
import com.siact.module.process.dto.ProcessLogDTO;
import com.siact.module.process.vo.ProcessLogVO;
import java.util.List;

/**
 * 工艺日志Service接口
 */
public interface IProcessLogService extends IService<ProcessLogEntity> {
    IPage<ProcessLogVO> pageQuery(int pageNum, int pageSize, ProcessLogDTO queryDTO);
    List<ProcessLogVO> listAll(ProcessLogDTO queryDTO);
    ProcessLogVO getById(Long id);
    boolean add(ProcessLogDTO dto);
    boolean update(ProcessLogDTO dto);
    boolean delete(Long id);
} 