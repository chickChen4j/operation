package com.efivestar.operation.mapping.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum SqlType {

  UPDATE("改"),
  DELETE("删"),
  INSERT("增"),
  SELECT("查"),
  UNKNOWN("未知");


  @Getter
  private String name;

}
