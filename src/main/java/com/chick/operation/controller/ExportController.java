package com.chick.operation.controller;

import com.alibaba.fastjson.JSONObject;
import com.chick.operation.dto.ResultDTO;
import com.chick.operation.http.ResourceServlet;
import com.chick.operation.mapping.entity.ExportTask;
import com.chick.operation.mapping.entity.Record;
import com.chick.operation.service.export.ExportEvent;
import com.chick.operation.service.export.ExportTaskService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * 文件导出接口
 */
@RestController
@RequestMapping("${operation.prefix:/operation}/export")
public class ExportController {

  @Autowired
  private ExportTaskService exportTaskService;


  @Autowired
  private ApplicationEventPublisher applicationEventPublisher;

  @RequestMapping(value = "/{apiName}/excel")
  public ResultDTO exportExcel(@PathVariable String apiName, HttpServletRequest request,
      HttpServletResponse response) throws Exception {
    final Record record = Record.builder()
        .ip(request.getRemoteAddr())
        .url(request.getRequestURI())
        .executor(
            String.valueOf(request.getSession().getAttribute(ResourceServlet.SESSION_USER_KEY)))
        .apiName(apiName)
        .build();
    final ExportTask exportTask = ExportTask.builder()
        .taskId(UUID.randomUUID().toString())
        .apiName(apiName)
        .build();

    exportTaskService.justSave(exportTask);
    applicationEventPublisher
        .publishEvent(new ExportEvent(this, exportTask, getParamsFromRequest(request), record));
    return ResultDTO.builder().message("导出任务创建成功").build();
  }

  @RequestMapping(value = "/tasks")
  public ResultDTO findTask() {
    final List<ExportTask> exportTasks = exportTaskService.findAll();
    return ResultDTO.builder().data(exportTasks).message("查询成功").build();
  }

  @RequestMapping(value = "/task/{id}", method = RequestMethod.DELETE)
  public ResultDTO deleteTask(@PathVariable("id") Integer id) {
    exportTaskService.delete(id);
    return ResultDTO.builder().message("删除任务成功").build();
  }


  protected JSONObject getParamsFromRequest(HttpServletRequest request) {
    final Map<String, String[]> params = request.getParameterMap();
    final Map<String, Object> map = new HashMap<>();
    if (!CollectionUtils.isEmpty(params)) {
      params.forEach((k, v) -> {
        if (v.length > 0) {
          map.put(k, v[0]);
        }
      });
    }
    return new JSONObject(map);
  }

}
