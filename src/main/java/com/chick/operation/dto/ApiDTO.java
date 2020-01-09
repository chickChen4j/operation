package com.chick.operation.dto;

import com.chick.operation.annotation.Condition;
import com.chick.operation.annotation.Describe;
import com.chick.operation.mapping.enums.ApiType;
import com.chick.operation.mapping.enums.SqlType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiDTO {

  /**
   * 主键
   */
  private Long id;

  /**
   * 名称(可唯一标注API，相当于xml配置中的id)
   */
  @Describe(alias = "API名称", desc = "请填写API名称", required = true, condition = {
      @Condition(type = SqlType.INSERT, value = "name!=null&&name!=''")})
  private String name;

  /**
   * 分组ID
   */
  @Describe(alias = "组名", desc = "请填写组名", required = true, condition = {
      @Condition(type = SqlType.INSERT, value = "groupId!=null&&groupId!=''")
  })
  private String groupId;

  /**
   * base类型的api配置的访问路径
   */
  @Describe(alias = "路径", desc = "restful风格可忽略")
  private String url;

  /**
   * 类型 分为restful和base
   */
  @Describe(alias = "API类型", required = true, desc = "请选择API类型")
  private ApiType type;

  /**
   * restful类型时操作的bean类型
   */
  @Describe(alias = "数据类型", desc = "base风格可忽略")
  private String bean;

  /**
   * bean的主键名
   */
  @Describe(alias = "主键名", desc = "请填写资源类型的主键名")
  private String primaryKey;

}
