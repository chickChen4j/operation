package com.efivestar.operation.service.export;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.efivestar.operation.dto.PagedResultDTO;
import com.efivestar.operation.dto.ResultDTO;
import com.efivestar.operation.exception.OperationException;
import com.efivestar.operation.mapping.Api;
import com.efivestar.operation.mapping.entity.ExportTask;
import com.efivestar.operation.mapping.entity.Record;
import com.efivestar.operation.mapping.Sql;
import com.efivestar.operation.mapping.XmlConfiguration;
import com.efivestar.operation.mapping.enums.ExportStatusEnum;
import com.efivestar.operation.mapping.enums.SelectType;
import com.efivestar.operation.properties.ExportConfig;
import com.efivestar.operation.repository.BaseRepository;
import com.efivestar.operation.util.spring.ApplicationContextHelper;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;


@Slf4j
public class ExportService implements Callable<ResultDTO> {

  private BaseRepository baseRepository;

  private POIService poiService;

  private ExportConfig exportConfigService;

  private ExportTaskService exportTaskService;

  private ExportTask exportTask;

  private JSONObject payload;

  private Record record;

  public ExportService(ExportTask exportTask, JSONObject payload, Record record) {
    this.exportTask = exportTask;
    this.payload = payload;
    this.record = record;
  }

  //  //后端返回的ResultDTO
  ResultDTO result = null;

  Object resultData = null;
  //data中的分页data
  PagedResultDTO pageData = null;
  //data中的List
  List<Object> contentArray = null;

  List<String> titles = new ArrayList<>();
  //当前页码
  Integer currentPage = 0;
  //总页数
  Long totalPage = 0L;
  //总条数
  Long totalCount = 0L;
  //每个sheet页最大行数
  Integer sheetRowCount = 0;
  //写入的行数（当前页的行数）
  Integer writeRowCount = 0;
  //分页请求的行数
  Integer maxWriteRowCount = 0;

  @Override
  public ResultDTO call() throws Exception {
    Long startTime = System.currentTimeMillis();

    baseRepository = ApplicationContextHelper.getBean(BaseRepository.class);

    poiService = ApplicationContextHelper.getBean(POIService.class);

    exportConfigService = ApplicationContextHelper.getBean(ExportConfig.class);

    exportTaskService = ApplicationContextHelper.getBean(ExportTaskService.class);
    //导出默认
    //文件名（唯一）
    String uuid = exportTask.getTaskId();
    String apiName = exportTask.getApiName();
    //分页请求的行数
    maxWriteRowCount = exportConfigService.getRequestCount();
    //每页最大行数
    sheetRowCount = exportConfigService.getMaxRow();
    payload.put("number", currentPage);
    payload.put("size", maxWriteRowCount);
    //导出请求参数(json)
    exportTask.setPayload(payload.toJSONString());

    ResultDTO resultDTOPOI = null;
    try {
      result = this.findExportData(apiName, payload, record);

      //如果返回的ResultDTO的isSuccess为false
      if (result == null && !result.getIsSuccess()) {
        return ResultDTO.builder().isSuccess(Boolean.FALSE).data(exportTask).build();
      } else {
        resultData = result.getData();
        if (resultData instanceof PagedResultDTO) {
          pageData = (PagedResultDTO) resultData;
          contentArray = pageData.getContent();
          totalPage = pageData.getTotalPages();
          totalCount = pageData.getTotalElements();
          writeRowCount = pageData.getSize();
        } else if (resultData instanceof List) {
          contentArray = (List) resultData;
          totalPage = 1l;
          totalCount = Long.valueOf(contentArray.size());
          writeRowCount = contentArray.size();
        } else {
          contentArray = new ArrayList() {{
            add(resultData);
          }};
          totalPage = 1l;
          totalCount = 1l;
          writeRowCount = 1;
        }
        if (!CollectionUtils.isEmpty(contentArray)) {
          Object demo = contentArray.get(0);
          if (demo instanceof Map) {
            titles.addAll(((Map) demo).keySet());
          } else {
            Class dClass = demo.getClass();
            Field[] fields = dClass.getDeclaredFields();
            titles = Arrays.asList(fields).stream().map(field -> field.getName())
                .collect(Collectors.toList());
          }
        }

        if (CollectionUtils.isEmpty(contentArray)) {
          exportTask.setMessage("数据为空");
          return ResultDTO.builder().isSuccess(Boolean.FALSE).data(exportTask).build();
        }

      }
      resultDTOPOI = poiService
          .uploadExcel(totalCount, maxWriteRowCount, sheetRowCount, totalPage, uuid, titles,
              (sheetIndex, eachSheet, startRowCount, endRowCount, currentPage, pageSize) -> {
                payload.put("number", currentPage);
                ResultDTO result = null;
                try {
                  result = findExportData(apiName, payload, record);
                } catch (Exception e) {
                  exportTask.setTaskId(uuid);
                  exportTask.setExportStatusEnum(ExportStatusEnum.exported);
                  exportTask.setProgress(100D);
                  exportTask.setMessage(e.getMessage() != null ? e.getMessage() : e.toString());
                  exportTaskService.save(exportTask);
                }

                if (result != null) {
                  //如果返回的ResultDTO的isSuccess为false，则结束并且写入数据库
                  if (!result.getIsSuccess()) {
                    exportTask.setExportStatusEnum(ExportStatusEnum.exported);
                    exportTask.setProgress(100D);
                    exportTask.setMessage(result.getMessage());
                    exportTaskService.save(exportTask);
                  } else if (result.getIsSuccess()) {
                    Object resultData = result.getData();

                    if (resultData != null) {
                      if (resultData instanceof PagedResultDTO) {
                        pageData = (PagedResultDTO) resultData;
                        contentArray = pageData.getContent();
                        totalCount = pageData.getTotalElements();
                        totalPage = pageData.getTotalPages();
                        writeRowCount = pageData.getSize();
                      } else if (resultData instanceof List) {
                        contentArray = (List) resultData;
                        totalPage = 1l;
                        totalCount = Long.valueOf(contentArray.size());
                        writeRowCount = contentArray.size();
                      } else {
                        contentArray = new ArrayList() {{
                          add(resultData);
                        }};
                        totalPage = 1l;
                        totalCount = 1l;
                        writeRowCount = 1;
                      }

                      if (!CollectionUtils.isEmpty(contentArray)) {
                        Object demo = contentArray.get(0);
                        if (demo instanceof Map) {
                          titles = new ArrayList<String>(((Map) demo).keySet());
                        } else {
                          Class dClass = demo.getClass();
                          Field[] fields = dClass.getDeclaredFields();
                          titles = Arrays.asList(fields).stream().map(field -> field.getName())
                              .collect(Collectors.toList());
                        }
                      }
                      if (contentArray.size() == 0) {
                        //数据为空异常
                        throw new OperationException("ExportService.asyncExport", "导出数据为空");
                      }
                    } else {
                      //数据为空异常
                      throw new OperationException("ExportService.asyncExport", "导出数据为空");
                    }
                  }
                }

                if (contentArray != null && contentArray.size() > 0) {
                  SXSSFRow eachDataRow;
                  for (int i = startRowCount; i <= endRowCount; i++) {
                    eachDataRow = eachSheet
                        .createRow(i - sheetIndex * sheetRowCount);
                    Object element = contentArray.get(
                        i - currentPage * maxWriteRowCount - 1);
                    int columnIndex = -1;
                    //遍历map中的键
                    for (String key : titles) {
                      String cellValue = "";
                      if (element instanceof Map) {
                        cellValue = String.valueOf(((Map) element).get(key));
                      } else {
                        Field field = element.getClass().getDeclaredField(key);
                        field.setAccessible(Boolean.TRUE);
                        cellValue =
                            element != null ? String
                                .valueOf(field.get(element)) : "";
                      }
                      columnIndex++;
                      eachDataRow.createCell(columnIndex)
                          .setCellValue(cellValue == null ? "" : cellValue);
                      eachDataRow.getCell(columnIndex).setCellType(Cell.CELL_TYPE_STRING);
                    }
                  }
                }
              });
    } catch (Exception e) {
      e.printStackTrace();
      exportTask.setMessage(e.getMessage() != null ? e.getMessage() : e.toString());
      result.setData(exportTask);
      result.setIsSuccess(false);
      result.setMessage(e.getMessage() != null ? e.getMessage() : e.toString());
      return result;
    }
    ExportTask exportTask_result = (ExportTask) resultDTOPOI.getData();
    if (!StringUtils.isEmpty(exportTask_result.getDownloadUrl())) {

      exportTask.setDownloadUrl(exportTask_result.getDownloadUrl());

      Long endTime = System.currentTimeMillis();
      Long timeConsume = endTime - startTime;
      //导出时长(s)
      exportTask_result.setTimeConsume(timeConsume / 1000);

//      exportTask_result.setExportCondition(
//          payload.get("exportCondition") != null ? payload.get("exportCondition").toString() : "");
      Map<String, Object> payload_search = payload;
      payload_search.remove("size");
      payload_search.remove("number");
//      payload_search.remove("exportTask");
//      payload_search.remove("exportCondition");
      exportTask_result.setPayload(JSON.toJSONString(payload_search));

      return ResultDTO.builder().data(exportTask_result).build();
    } else {
      return ResultDTO.builder().data(resultDTOPOI).build();
    }
  }


  public ResultDTO findExportData(String apiName, JSONObject payload, Record record) {
    if (!XmlConfiguration.existApi(apiName)) {
      throw new OperationException("ExportController.exportExcel", "未配置此api");
    }
    final Api api = XmlConfiguration.loadApi(apiName);
    final Sql sql = api.selectSqlIsUsed();
    if (sql != null) {
      if (SelectType.PAGE.equals(sql.getSelectType())) {
        baseRepository.handleParams(payload);
      } else {
        if (payload.containsKey("size")) {
          payload.remove("size");
        }
        if (payload.containsKey("number")) {
          payload.remove("number");
        }
      }
      Object result = null;
      try {
        result = baseRepository.execute(sql, record, payload);
      } catch (Exception e) {
        return ResultDTO.builder().isSuccess(Boolean.FALSE).message(e.getMessage()).build();
      }
      return ResultDTO.builder().data(result).build();
    }
    return ResultDTO.builder().build();
  }


}
