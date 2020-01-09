package com.efivestar.operation.service.export;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.efivestar.operation.dto.ResultDTO;
import com.efivestar.operation.mapping.entity.ExportTask;
import com.efivestar.operation.mapping.entity.Record;
import com.efivestar.operation.mapping.enums.ExportStatusEnum;
import com.efivestar.operation.properties.ExportConfig;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ExportEventListener {

  ExecutorService executorThreadPool = Executors.newFixedThreadPool(10);

  @Autowired
  ExportConfig exportConfigService;

  @Autowired
  ExportTaskService exportTaskService;


  @EventListener
  public void execute(ExportEvent exportEvent) {
    Future<ResultDTO> future = null;
    ResultDTO resultDTO = ResultDTO.builder().build();

    ExportTask exportTask = exportEvent.getExportTask();
    JSONObject payload = exportEvent.getPayload();
    Record record = exportEvent.getRecord();
    String uuid = exportTask.getTaskId();
    ExportTask exportTask_return = null;
    try {
      //获取最大超时时长（秒）
      Integer overtime = exportConfigService.getOvertime();
      future = executorThreadPool.submit(new ExportService(exportTask, payload, record));
      //阻断指定的时间，超时退出！
      resultDTO = future.get(overtime, TimeUnit.SECONDS);

      //如果返回的ResultDTO的isSuccess为false，则结束并且写入数据库
      exportTask = exportTaskService.findByTaskId(uuid);
      exportTask_return = (ExportTask) resultDTO.getData();
      if (!resultDTO.getIsSuccess()) {
        if (exportTask != null) {
          exportTask.setExportStatusEnum(ExportStatusEnum.exported);
          exportTask.setProgress(100D);
          exportTask.setMessage(resultDTO.getMessage());

          if (exportTask_return != null) {
            exportTask.setTimeConsume(exportTask_return.getTimeConsume());

            JSONObject jsonObject_payload = JSON.parseObject(exportTask_return.getPayload());
            jsonObject_payload.remove("number");
            jsonObject_payload.remove("size");
//            jsonObject_payload.remove("exportTask");
//            jsonObject_payload.remove("exportCondition");
            exportTask.setPayload(JSON.toJSONString(jsonObject_payload));
          }
        }
        exportTaskService.save(exportTask);
      } else {
        //执行成功有两种情况，一种
        if (exportTask != null) {
          exportTask.setExportStatusEnum(ExportStatusEnum.exported);
          exportTask.setProgress(100D);
          if (exportTask_return != null) {
//            exportTask.setExportCondition(exportTask_return.getExportCondition());
            exportTask.setPayload(exportTask_return.getPayload());
            exportTask.setErrorLog(exportTask_return.getErrorLog());
            exportTask.setMessage(exportTask_return.getMessage());
            exportTask.setDownloadUrl(exportTask_return.getDownloadUrl());
            exportTask.setFileSize(exportTask_return.getFileSize());
            exportTask.setTimeConsume(exportTask_return.getTimeConsume());
          }
        }
        exportTaskService.save(exportTask);
      }

    } catch (InterruptedException e) {
      //线程执行异常
      handleTaskInException(exportTask, uuid, e);
    } catch (ExecutionException e) {
      //线程执行异常
      handleTaskInException(exportTask, uuid, e);
    } catch (TimeoutException e) {
      //超时异常
      handleTaskInException(exportTask, uuid, e);
    } catch (Exception e) {
      //未知异常
      handleTaskInException(exportTask, uuid, e);

    }

  }

  public void handleTaskInException(ExportTask exportTask, String uuid, Exception e) {
    exportTask = exportTaskService.findByTaskId(uuid);
    if (exportTask != null) {
      exportTask.setTaskId(uuid);
      exportTask.setExportStatusEnum(ExportStatusEnum.exported);
      exportTask.setProgress(100D);
      exportTask.setMessage(e.getMessage() != null ? e.getMessage() : e.toString());
      exportTask.setErrorLog(e.getMessage() != null ? e.getMessage() : e.toString());
      exportTaskService.save(exportTask);
    }
    e.printStackTrace();
  }


}
