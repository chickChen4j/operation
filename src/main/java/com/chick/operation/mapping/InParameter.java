package com.chick.operation.mapping;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InParameter {

  // 传入的参数名称
  private String name;
  // 参数的Java类型
  private Class<?> javaType;

  private Object value;
}
