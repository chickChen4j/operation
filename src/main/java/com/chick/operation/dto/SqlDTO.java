package com.chick.operation.dto;

import com.chick.operation.annotation.Condition;
import com.chick.operation.annotation.Describe;
import com.chick.operation.mapping.enums.SelectType;
import com.chick.operation.mapping.enums.SqlType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SqlDTO {

  /**
   * 主键
   */
  private Long id;

  /**
   * 类型(insert,update,delete,select四种)
   */
  @Describe(alias = "SQL类型", required = true, desc = "请选择SQL类型")
  private SqlType type;

  /**
   * 具体的sql语句
   */
  @Describe(alias = "SQL语句", desc = "请填写SQL语句", required = true, condition = {
      @Condition(type = SqlType.INSERT, value = "value!=null&&value!=''")})
  private String value;


  /**
   * 参数类型
   */
  @Describe(alias = "参数类型", desc = "请填写全限定类名")
  private String parameterType;


  /**
   * 查询的类型(one,list,page三种)
   */
  @Describe(alias = "查询方式", required = true, desc = "请选择查询类型")
  private SelectType selectType;

  /**
   * 返回值类型
   */
  @Describe(alias = "返回类型", desc = "请填写返回类型")
  private String resultType;

  /**
   * 外键(api与sql是一对多关系)
   */
  @Describe(alias = "API", desc = "请选择对应的API", required = true, relate = "api",relateField = "name")
  private Long apiId;

}
