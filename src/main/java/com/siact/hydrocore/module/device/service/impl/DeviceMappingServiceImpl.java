package com.siact.hydrocore.module.device.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.siact.hydrocore.common.exception.BizException;
import com.siact.hydrocore.common.vo.PageVO;
import com.siact.hydrocore.module.device.command.DeviceMappingCommand;
import com.siact.hydrocore.module.device.convert.DeviceMappingConvert;
import com.siact.hydrocore.module.device.dto.DeviceMappingQueryDTO;
import com.siact.hydrocore.module.device.entity.DeviceMappingEntity;
import com.siact.hydrocore.module.device.mapper.DeviceMappingMapper;
import com.siact.hydrocore.module.device.query.DeviceMappingQuery;
import com.siact.hydrocore.module.device.repository.DeviceMappingRepository;
import com.siact.hydrocore.module.device.service.DeviceMappingService;
import com.siact.hydrocore.module.device.vo.DeviceImportResult;
import com.siact.hydrocore.module.device.vo.DeviceMappingVO;
import com.siact.hydrocore.common.utils.JacksonUtils;
import com.siact.hydrocore.common.utils.ExcelUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import cn.afterturn.easypoi.excel.entity.params.ExcelExportEntity;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import cn.afterturn.easypoi.excel.ExcelImportUtil;

@RequiredArgsConstructor
@Service
public class DeviceMappingServiceImpl extends ServiceImpl<DeviceMappingMapper, DeviceMappingEntity> implements DeviceMappingService {
    private final DeviceMappingConvert convert;
    private final DeviceMappingRepository repository;

    @Override
    public PageVO<DeviceMappingVO> list(DeviceMappingQuery query) {
        DeviceMappingQueryDTO queryDTO = convert.toQueryDTO(query);
        Page<DeviceMappingEntity> page = repository.queryList(queryDTO, Page.of(query.getPage(), query.getPageSize()));
        List<DeviceMappingVO> voList = convert.toVOList(page.getRecords());
        return PageVO.<DeviceMappingVO>builder()
                .current(page.getCurrent())
                .size(page.getSize())
                .total(page.getTotal())
                .pages(page.getPages())
                .records(voList)
                .build();
    }

    @Override
    public DeviceMappingVO getById(Long id) {
        DeviceMappingEntity entity = super.getById(id);
        return convert.toVO(entity);
    }

    @Override
    public Boolean add(DeviceMappingCommand command) {
        checkUniqueForAdd(command);
        DeviceMappingEntity entity = convert.toEntity(command);
        return this.save(entity);
    }

    @Override
    public Boolean update(DeviceMappingCommand command) {
        checkUniqueForUpdate(command);
        DeviceMappingEntity entity = convert.toUpdateEntity(command);
        return this.updateById(entity);
    }

    @Override
    public Boolean delete(Long id) {
        return this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteBatch(List<Long> ids) {
        return this.removeByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean clear() {
        return this.remove(Wrappers.<DeviceMappingEntity>lambdaQuery());
    }

    private void checkUniqueForAdd(DeviceMappingCommand command) {
        if (repository.existsByPointName(command.getPointName())) {
            throw new BizException("现场点位名称已存在: " + command.getPointName());
        }
        if (repository.existsByItemId(command.getItemId())) {
            throw new BizException("点位ID已存在: " + command.getItemId());
        }
        if (repository.existsByPropCode(command.getPropCode())) {
            throw new BizException("属性编码已存在: " + command.getPropCode());
        }
    }

    private void checkUniqueForUpdate(DeviceMappingCommand command) {
        if (repository.existsByPointNameExcludeId(command.getPointName(), command.getId())) {
            throw new BizException("现场点位名称已存在: " + command.getPointName());
        }
        if (repository.existsByItemIdExcludeId(command.getItemId(), command.getId())) {
            throw new BizException("点位ID已存在: " + command.getItemId());
        }
        if (repository.existsByPropCodeExcludeId(command.getPropCode(), command.getId())) {
            throw new BizException("属性编码已存在: " + command.getPropCode());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DeviceImportResult importData(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new BizException("文件名不能为空");
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();

        List<DeviceMappingCommand> commands;
        if (".xlsx".equals(extension) || ".xls".equals(extension)) {
            commands = parseExcel(file);
        } else if (".csv".equals(extension)) {
            commands = parseCsv(file);
        } else if (".json".equals(extension)) {
            commands = parseJson(file);
        } else {
            throw new BizException("不支持的文件格式: " + extension + "，仅支持 xlsx/xls/csv/json");
        }

        DeviceImportResult result = new DeviceImportResult();
        for (int i = 0; i < commands.size(); i++) {
            DeviceMappingCommand command = commands.get(i);
            int rowNum = i + 2;
            try {
                validateCommand(command);
                DeviceMappingEntity existing = repository.findByItemId(command.getItemId());
                if (existing != null) {
                    command.setId(existing.getId());
                    DeviceMappingEntity entity = convert.toUpdateEntity(command);
                    this.updateById(entity);
                    result.setUpdateCount(result.getUpdateCount() + 1);
                } else {
                    DeviceMappingEntity entity = convert.toEntity(command);
                    this.save(entity);
                    result.setSuccessCount(result.getSuccessCount() + 1);
                }
            } catch (Exception e) {
                DeviceImportResult.ImportError error = new DeviceImportResult.ImportError();
                error.setRow(rowNum);
                error.setPointName(command.getPointName());
                error.setItemId(command.getItemId());
                error.setReason(e.getMessage());
                result.getErrors().add(error);
                result.setFailCount(result.getFailCount() + 1);
            }
        }
        return result;
    }

    private void validateCommand(DeviceMappingCommand command) {
        if (command.getItemId() == null || command.getItemId().trim().isEmpty()) {
            throw new BizException("点位ID不能为空");
        }
        if (command.getPointName() == null || command.getPointName().trim().isEmpty()) {
            throw new BizException("现场点位名称不能为空");
        }
        if (command.getPropCode() == null || command.getPropCode().trim().isEmpty()) {
            throw new BizException("属性编码不能为空");
        }
        if (command.getPropName() == null || command.getPropName().trim().isEmpty()) {
            throw new BizException("属性名称不能为空");
        }
        if (command.getDeviceCode() == null || command.getDeviceCode().trim().isEmpty()) {
            throw new BizException("设备编码不能为空");
        }
        if (command.getDeviceName() == null || command.getDeviceName().trim().isEmpty()) {
            throw new BizException("设备名称不能为空");
        }
    }

    @SuppressWarnings("unchecked")
    private List<DeviceMappingCommand> parseExcel(MultipartFile file) {
        try {
            ImportParams params = new ImportParams();
            params.setTitleRows(0);
            params.setHeadRows(1);
            params.setStartSheetIndex(0);
            params.setNeedSave(false);
            List<?> rows = ExcelImportUtil.importExcel(file.getInputStream(), Map.class, params);
            List<DeviceMappingCommand> result = new ArrayList<>();
            for (Object row : rows) {
                Map<String, Object> map = (Map<String, Object>) row;
                DeviceMappingCommand command = new DeviceMappingCommand();
                command.setPointName(getStringValue(map, "现场点位名称"));
                command.setItemId(getStringValue(map, "点位ID"));
                command.setPropCode(getStringValue(map, "属性编码"));
                command.setPropName(getStringValue(map, "属性名称"));
                command.setDeviceCode(getStringValue(map, "设备编码"));
                command.setDeviceName(getStringValue(map, "设备名称"));
                command.setRemark(getStringValue(map, "备注"));
                result.add(command);
            }
            return result;
        } catch (Exception e) {
            throw new BizException("Excel文件解析失败: " + e.getMessage());
        }
    }

    private List<DeviceMappingCommand> parseCsv(MultipartFile file) {
        try {
            List<DeviceMappingCommand> result = new ArrayList<>();
            BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
            String[] headers = reader.readLine().split(",");
            int pointNameIdx = findIndex(headers, "现场点位名称");
            int itemIdIdx = findIndex(headers, "点位ID");
            int propCodeIdx = findIndex(headers, "属性编码");
            int propNameIdx = findIndex(headers, "属性名称");
            int deviceCodeIdx = findIndex(headers, "设备编码");
            int deviceNameIdx = findIndex(headers, "设备名称");
            int remarkIdx = findIndex(headers, "备注");

            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(",", -1);
                DeviceMappingCommand command = new DeviceMappingCommand();
                command.setPointName(getIndexValue(values, pointNameIdx));
                command.setItemId(getIndexValue(values, itemIdIdx));
                command.setPropCode(getIndexValue(values, propCodeIdx));
                command.setPropName(getIndexValue(values, propNameIdx));
                command.setDeviceCode(getIndexValue(values, deviceCodeIdx));
                command.setDeviceName(getIndexValue(values, deviceNameIdx));
                command.setRemark(getIndexValue(values, remarkIdx));
                result.add(command);
            }
            return result;
        } catch (IOException e) {
            throw new BizException("CSV文件解析失败: " + e.getMessage());
        }
    }

    private List<DeviceMappingCommand> parseJson(MultipartFile file) {
        try {
            String json = new String(file.getBytes(), StandardCharsets.UTF_8);
            return JacksonUtils.fromJson(json, new TypeReference<List<DeviceMappingCommand>>() {});
        } catch (Exception e) {
            throw new BizException("JSON文件解析失败: " + e.getMessage());
        }
    }

    private int findIndex(String[] headers, String name) {
        for (int i = 0; i < headers.length; i++) {
            if (name.equals(headers[i].trim())) return i;
        }
        return -1;
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString().trim() : null;
    }

    private String getIndexValue(String[] values, int index) {
        if (index < 0 || index >= values.length) return null;
        return values[index].trim();
    }

    @Override
    public void exportData(DeviceMappingQuery query, String format, HttpServletResponse response) {
        DeviceMappingQueryDTO queryDTO = convert.toQueryDTO(query);
        List<DeviceMappingEntity> list = repository.queryList(queryDTO);
        List<DeviceMappingVO> voList = convert.toVOList(list);

        switch (format.toLowerCase()) {
            case "excel":
                exportExcel(voList, response);
                break;
            case "csv":
                exportCsv(voList, response);
                break;
            case "json":
                exportJson(voList, response);
                break;
            default:
                throw new BizException("不支持的导出格式: " + format + "，仅支持 excel/csv/json");
        }
    }

    private void exportExcel(List<DeviceMappingVO> voList, HttpServletResponse response) {
        List<ExcelExportEntity> headList = new ArrayList<>();
        headList.add(new ExcelExportEntity("现场点位名称", "pointName", 25));
        headList.add(new ExcelExportEntity("点位ID", "itemId", 20));
        headList.add(new ExcelExportEntity("属性编码", "propCode", 25));
        headList.add(new ExcelExportEntity("属性名称", "propName", 20));
        headList.add(new ExcelExportEntity("设备编码", "deviceCode", 20));
        headList.add(new ExcelExportEntity("设备名称", "deviceName", 20));
        headList.add(new ExcelExportEntity("备注", "remark", 30));

        List<Map<String, Object>> dataList = new ArrayList<>();
        for (DeviceMappingVO vo : voList) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("pointName", vo.getPointName());
            item.put("itemId", vo.getItemId());
            item.put("propCode", vo.getPropCode());
            item.put("propName", vo.getPropName());
            item.put("deviceCode", vo.getDeviceCode());
            item.put("deviceName", vo.getDeviceName());
            item.put("remark", vo.getRemark());
            dataList.add(item);
        }

        ExcelUtils.exportExcel(headList, "设备点位", dataList, response);
    }

    private void exportCsv(List<DeviceMappingVO> voList, HttpServletResponse response) {
        try {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment;filename=" +
                    java.net.URLEncoder.encode("设备点位.csv", "UTF-8"));

            response.getOutputStream().write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});

            String[] headers = {"现场点位名称", "点位ID", "属性编码", "属性名称", "设备编码", "设备名称", "备注"};
            response.getOutputStream().write(String.join(",", headers).getBytes(StandardCharsets.UTF_8));
            response.getOutputStream().write("\n".getBytes(StandardCharsets.UTF_8));

            for (DeviceMappingVO vo : voList) {
                String[] row = {
                        escapeCsv(vo.getPointName()),
                        escapeCsv(vo.getItemId()),
                        escapeCsv(vo.getPropCode()),
                        escapeCsv(vo.getPropName()),
                        escapeCsv(vo.getDeviceCode()),
                        escapeCsv(vo.getDeviceName()),
                        escapeCsv(vo.getRemark())
                };
                response.getOutputStream().write(String.join(",", row).getBytes(StandardCharsets.UTF_8));
                response.getOutputStream().write("\n".getBytes(StandardCharsets.UTF_8));
            }
            response.getOutputStream().flush();
        } catch (IOException e) {
            throw new BizException("CSV导出失败: " + e.getMessage());
        }
    }

    private void exportJson(List<DeviceMappingVO> voList, HttpServletResponse response) {
        try {
            response.setCharacterEncoding("UTF-8");
            response.setContentType("application/json");
            response.setHeader("Content-Disposition", "attachment;filename=" +
                    java.net.URLEncoder.encode("设备点位.json", "UTF-8"));
            String json = JacksonUtils.toPrettyJson(voList);
            response.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
            response.getOutputStream().flush();
        } catch (IOException e) {
            throw new BizException("JSON导出失败: " + e.getMessage());
        }
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    @Override
    public void downloadImportTemplate(HttpServletResponse response) {
        List<ExcelExportEntity> headList = new ArrayList<>();
        headList.add(new ExcelExportEntity("现场点位名称", "pointName", 25));
        headList.add(new ExcelExportEntity("点位ID", "itemId", 20));
        headList.add(new ExcelExportEntity("属性编码", "propCode", 25));
        headList.add(new ExcelExportEntity("属性名称", "propName", 20));
        headList.add(new ExcelExportEntity("设备编码", "deviceCode", 20));
        headList.add(new ExcelExportEntity("设备名称", "deviceName", 20));
        headList.add(new ExcelExportEntity("备注", "remark", 30));
        ExcelUtils.exportExcel(headList, "设备点位导入模板", new ArrayList<>(), response);
    }
}
