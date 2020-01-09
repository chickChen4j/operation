package com.chick.operation.mapping.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ExportStatusEnum {
  initial("初始化"), start("开始"), exporting("正在导出"),
  stop("暂停"), cancel("取消"), exported("完成"), failure("失败");

  private String name;
}
