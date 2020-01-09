package com.chick.operation.repository;

import com.chick.operation.mapping.entity.Record;
import com.chick.operation.mapping.Sql;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcTemplateProxyFactory<T> {

  @Getter
  private final Class<T> mapperInterface;

  @Getter
  private final ConcurrentHashMap<JdbcTemplateProxy.ApiMethod, Sql> methodCache = new ConcurrentHashMap<>();

  @Getter
  private final JdbcTemplate jdbcTemplate;


  public JdbcTemplateProxyFactory(Class<T> mapperInterface, JdbcTemplate jdbcTemplate) {
    this.mapperInterface = mapperInterface;
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * 清空方法缓存，因为内存中sql被改变，需要刷新这里的缓存，不然执行的是修改之前的sql
   */
  protected void cleanMethodCache() {
    methodCache.clear();
  }

  /**
   * 反射， 使用 MapperProxy 对象， 生成 Mapper 的代理类
   */
  @SuppressWarnings("unchecked")
  protected T newInstance(JdbcTemplateProxy<T> jdbcTemplateProxy) {
    return (T) Proxy
        .newProxyInstance(mapperInterface.getClassLoader(), new Class[]{mapperInterface},
            jdbcTemplateProxy);
  }

  /**
   * 创建一个 MapperProxy 对象， 并调用上面的 newInstance 方法
   */
  public T newInstance(String apiName, Record record) {
    final JdbcTemplateProxy<T> jdbcTemplateProxy = new JdbcTemplateProxy<>(methodCache, apiName,
        record, jdbcTemplate);
    return newInstance(jdbcTemplateProxy);
  }


}
