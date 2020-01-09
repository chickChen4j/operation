package com.chick.operation.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 修饰参数，由于base风格不指定入参，故当前只考虑restful风格的api
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface Describe {

  /**
   * 入参校验规则
   */
  Condition[] condition() default {};

  /**
   * 表单水印信息
   */
  String desc() default "";

  /**
   * 表单显示文本
   */
  String alias() default "";

  /**
   * 外键关联的apiName
   */
  String relate()  default "";

  /**
   * 外键关联展示的字段
   */
  String relateField() default "";

  /**
   * 是否必须，用于前端校验
   * @return
   */
  boolean required() default false;

}
