package com.chick.operation.properties;

import java.lang.reflect.Field;

public interface StaticConfig {

  default <T> T getFieldValue(String fieldName, Class<T> tClass) {
    final Class selfClass = this.getClass();
    try {
      Field field = selfClass.getField(fieldName);
      Object result = field.get(selfClass);
      if (tClass.isInstance(result)) {
        return tClass.cast(result);
      }
    } catch (NoSuchFieldException e) {
      return null;
    } catch (IllegalAccessException e) {
      return null;
    }
    return null;
  }
}
