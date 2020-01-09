package com.chick.operation.annotation;

import com.chick.operation.mapping.enums.SqlType;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 入参校验规则
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface Condition {

  /**
   * 校验接口类型
   */
  SqlType type() default SqlType.INSERT;

  /**
   * 校验规则，el表达式
   */
  String value() default "";
}
