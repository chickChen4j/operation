package com.chick.operation.util.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class ApplicationContextHelper implements ApplicationContextAware {

  private static ApplicationContext applicationContext;

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  public static ApplicationContext getApplicationContext() {
    return applicationContext;
  }

  public static <T> T getBean(String beanName, Class<T> tClass) {
    return applicationContext.getBean(beanName, tClass);
  }

  public static <T> T getBean(Class<T> tClass) {
    return applicationContext.getBean(tClass);
  }

  public static Object getBean(String beanName) {
    return applicationContext.getBean(beanName);
  }
}
