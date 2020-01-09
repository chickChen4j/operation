package com.efivestar.operation.mapping.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum ApiType {

  RESTFUL("restful风格"), BASE("基础风格");

  @Getter
  private String name;
}
