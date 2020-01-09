package com.chick.operation.service.export;

import com.alibaba.fastjson.JSONObject;
import com.chick.operation.mapping.entity.ExportTask;
import com.chick.operation.mapping.entity.Record;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;


public class ExportEvent extends ApplicationEvent {

  @Getter
  private ExportTask exportTask;

  @Getter
  private JSONObject payload;

  @Getter
  private Record record;

  public ExportEvent(Object source, ExportTask exportTask, JSONObject payload, Record record) {
    super(source);
    this.exportTask = exportTask;
    this.payload = payload;
    this.record = record;
  }


}