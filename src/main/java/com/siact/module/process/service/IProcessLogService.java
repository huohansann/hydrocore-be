package com.siact.module.process.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.module.permission.vo.PageVO;
import com.siact.module.process.dto.ProcessLogDTO;
import com.siact.module.process.dto.ProcessLogPageDTO;
import com.siact.module.process.dto.ProcessLogQueryDTO;
import com.siact.module.process.entity.ProcessLogEntity;
import com.siact.module.process.vo.ProcessLogVO;

import java.util.List;
import java.util.Map;

/**
 * 工艺日志Service接口
 */
public interface IProcessLogService extends IService<ProcessLogEntity> {
    /**
     * 分页查询
     * @param queryDTO
     * @return
     */
    PageVO<ProcessLogEntity> pageQuery(ProcessLogPageDTO queryDTO);

    /**
     * 条件查询
     * @param queryDTO
     * @return
     */
    List<ProcessLogVO> listAll(ProcessLogQueryDTO queryDTO);
    /**
     * 根据id查询
     * @param id
     * @return
     */
    ProcessLogVO getById(Long id);

    /**
     * 新增
     * @param dto
     * @return
     */
    void add(ProcessLogDTO dto);

    /**
     * 修改
     * @param dto
     * @return
     */
    boolean update(ProcessLogDTO dto);

    /**
     * 删除
     * @param id
     * @return
     */
    boolean delete(Long id);

    /**
     * 根据日期查询
     * @param queryDate
     * @return
     */
    ProcessLogVO queryByDate(String queryDate);

    /**
     * 批量删除
     * @param idList
     * @return
     */
    Boolean deleteBatch(List<Long> idList);

    /**
     * 查询时间段内的数据
     * @param startTime
     * @param endTime
     * @return
     */
    Map<String,List<ProcessLogVO>> queryByDateRange(String startTime, String endTime);

    /**
     * 查询时间段内的数据
     * @param startTime
     * @param endTime
     * @return
     */
    List<ProcessLogEntity> getByTimeRange(String startTime, String endTime);

    /**
     * 查询时间段内的数据,是否换机
     * @param startTime
     * @param endTime
     * @return
     */
    List<ProcessLogEntity> getByTimeRange(String startTime, String endTime, int replaceMachine);
}