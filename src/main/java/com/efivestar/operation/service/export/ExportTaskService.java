package com.efivestar.operation.service.export;

import com.efivestar.operation.mapping.entity.ExportTask;
import com.efivestar.operation.repository.BaseRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExportTaskService {

  @Autowired
  private BaseRepository baseRepository;

  public void save(ExportTask task) {
    ExportTask exportTaskExist = (ExportTask) baseRepository
        .findByTableId(ExportTask.builder().taskId(task.getTaskId()).build());
    if (exportTaskExist != null) {
      baseRepository.updateTable(task);
    } else {
      baseRepository.insertTable(task);
    }
//    try {
//      ExportTaskController.sendInfo(JSONObject.toJSONString(task), "Guest1");
//    } catch (IOException e) {
//      log.warn("导出任务信息推送异常：{}", new Object[]{e.getMessage()});
//    }
  }

  public void justSave(ExportTask exportTask) {
    ExportTask exportTaskExist = (ExportTask) baseRepository
        .findByTableId(ExportTask.builder().id(exportTask.getId()).build());
    if (exportTaskExist != null) {
      baseRepository.updateTable(exportTask);
    } else {
      baseRepository.insertTable(exportTask);
    }
  }

  public ExportTask findByTaskId(String taskId) {
    return (ExportTask) baseRepository.findByTableId(ExportTask.builder().taskId(taskId).build());
  }

  public List<ExportTask> findAll() {
    return baseRepository.findAll(ExportTask.builder().build());
  }

  public boolean delete(Integer id) {
    return baseRepository.deleteTable(ExportTask.builder().id(id).build());
  }

}
