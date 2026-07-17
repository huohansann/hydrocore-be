package com.siact.hydrocore.module.device.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.siact.hydrocore.common.vo.PageVO;
import com.siact.hydrocore.module.device.command.DeviceMappingCommand;
import com.siact.hydrocore.module.device.entity.DeviceMappingEntity;
import com.siact.hydrocore.module.device.query.DeviceMappingQuery;
import com.siact.hydrocore.module.device.vo.DeviceMappingVO;
import com.siact.hydrocore.module.device.vo.DeviceImportResult;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface DeviceMappingService extends IService<DeviceMappingEntity> {

    PageVO<DeviceMappingVO> list(DeviceMappingQuery query);

    DeviceMappingVO getById(Long id);

    Boolean add(DeviceMappingCommand command);

    Boolean update(DeviceMappingCommand command);

    Boolean delete(Long id);

    Boolean deleteBatch(List<Long> ids);

    Boolean clear();

    DeviceImportResult importData(MultipartFile file);

    void exportData(DeviceMappingQuery query, String format, HttpServletResponse response);

    void downloadImportTemplate(HttpServletResponse response);
}
