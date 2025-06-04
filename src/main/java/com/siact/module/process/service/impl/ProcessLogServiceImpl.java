package com.siact.module.process.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.module.process.entity.ProcessLogEntity;
import com.siact.module.process.dto.ProcessLogDTO;
import com.siact.module.process.vo.ProcessLogVO;
import com.siact.module.process.mapper.ProcessLogMapper;
import com.siact.module.process.service.IProcessLogService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 工艺日志Service实现
 */
@Service
public class ProcessLogServiceImpl extends ServiceImpl<ProcessLogMapper, ProcessLogEntity> implements IProcessLogService {
    @Override
    public IPage<ProcessLogVO> pageQuery(int pageNum, int pageSize, ProcessLogDTO queryDTO) {
        Page<ProcessLogEntity> page = new Page<>(pageNum, pageSize);
        QueryWrapper<ProcessLogEntity> wrapper = buildQueryWrapper(queryDTO);
        IPage<ProcessLogEntity> entityPage = this.page(page, wrapper);
        return entityPage.convert(this::toVO);
    }
    @Override
    public List<ProcessLogVO> listAll(ProcessLogDTO queryDTO) {
        QueryWrapper<ProcessLogEntity> wrapper = buildQueryWrapper(queryDTO);
        List<ProcessLogEntity> list = this.list(wrapper);
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }
    @Override
    public ProcessLogVO getById(Long id) {
        ProcessLogEntity entity = baseMapper.selectById(id);
        return entity == null ? null : toVO(entity);
    }
    @Override
    public boolean add(ProcessLogDTO dto) {
        ProcessLogEntity entity = new ProcessLogEntity();
        BeanUtils.copyProperties(dto, entity);
        return this.save(entity);
    }
    @Override
    public boolean update(ProcessLogDTO dto) {
        ProcessLogEntity entity = new ProcessLogEntity();
        BeanUtils.copyProperties(dto, entity);
        return this.updateById(entity);
    }
    @Override
    public boolean delete(Long id) {
        return this.removeById(id);
    }
    private QueryWrapper<ProcessLogEntity> buildQueryWrapper(ProcessLogDTO dto) {
        QueryWrapper<ProcessLogEntity> wrapper = new QueryWrapper<>();
        // 可根据dto字段补充查询条件
        return wrapper;
    }
    private ProcessLogVO toVO(ProcessLogEntity entity) {
        ProcessLogVO vo = new ProcessLogVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
} 