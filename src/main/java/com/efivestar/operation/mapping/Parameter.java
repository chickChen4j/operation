package com.efivestar.operation.mapping;

import com.efivestar.operation.mapping.enums.SqlType;
import java.util.Map;
import lombok.Data;

@Data
public class Parameter {

  // 传入的参数名称
  private String name;
  // 参数的Java类型
  private Class<?> javaType = Object.class;
  // 参数的 jdbc 类型
//  private JdbcType jdbcType;
  // 浮点参数的精度
//  private Integer numericScale;

  //别名
  private String alias;

  //描述
  private String desc;

  //判断条件，用于接口校验
  private Map<SqlType,String> conditions;

  //是否必填
  private boolean required;

  //前端下拉选可能用到的枚举值
  private Object[] enums;

  private String relate;

  private String relateField;

  //参数的序号
  private Integer index;

}
