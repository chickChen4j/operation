package com.chick.operation.mapping.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum SelectType {
  ONE("One"), LIST("All"), PAGE("ByPage");

  @Getter
  private String suffix;
}
