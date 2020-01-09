package com.chick.operation.mapping;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Result {

  // 对应相应 JavaBean 中的成员变量
  private String property;
  // 对应节点的 column 属性， 对应检索出来的列名（别名）
  private String column;
  // 对应节点的 javaType 属性
  @Builder.Default
  private Class<?> javaType = Object.class;
  //对应前端table的属性名
  private String tableName;

}
