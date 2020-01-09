package com.chick.operation.repository;


import com.chick.operation.exception.OperationException;
import com.chick.operation.mapping.Api;
import com.chick.operation.mapping.entity.Record;
import com.chick.operation.mapping.Sql;
import com.chick.operation.mapping.XmlConfiguration;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;

@Slf4j
public class JdbcTemplateProxy<T> implements InvocationHandler {


  private ConcurrentHashMap<ApiMethod, Sql> methodCache;

  private String apiName;

  private Record record;

  private JdbcTemplate jdbcTemplate;

  public JdbcTemplateProxy(
      ConcurrentHashMap<ApiMethod, Sql> methodCache, String apiName, Record record,
      JdbcTemplate jdbcTemplate) {
    this.methodCache = methodCache;
    this.apiName = apiName;
    this.record = record;
    this.jdbcTemplate = jdbcTemplate;
  }

  /**
   * 从缓存中获取代理接口的实现类，如果不存在则创建并能入
   */
  private Sql cacheSql(ApiMethod method) {
    return methodCache.computeIfAbsent(method, k -> newSqlInstance(method));
  }

  private Sql newSqlInstance(ApiMethod method) {
    final Api api = XmlConfiguration.loadApi(apiName);
    if (api == null) {
      throw new OperationException("JdbcTemplateProxy.newSqlInstance", "该bean所对应的api为空");
    }
    final List<Sql> sqls = api.getSqls();
    if (!CollectionUtils.isEmpty(sqls)) {
      for (Sql sql : sqls) {
        if (method.getMethod().getName().equals(sql.getId())) {
          return sql;
        }
      }
    }
    log.warn("名为{}的api下sql方法名配置错误", apiName);
    return null;
  }


  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      // 目标方法继承自 Object, 则直接调用目标方法
      if (Object.class.equals(method.getDeclaringClass())) {
        return method.invoke(this, args);
      } else if (isDefaultMethod(method)) {
        return invokeDefaultMethod(proxy, method, args);
      }
    } catch (Throwable t) {
      log.warn("crud代理调用方法出错，原因为:{},{}", new Object[]{t, t.getMessage()});
      throw new OperationException("JdbcTemplateProxy.invoke", "crud代理调用方法出错");
    }
    final Sql sql = this.cacheSql(ApiMethod.builder().method(method).apiName(apiName).build());
//    final Sql sql = newSqlInstance(ApiMethod.builder().method(method).apiName(apiName).build());
    if (null == sql) {
      throw new OperationException("JdbcTemplateProxy.invoke", "sql语句为空");
    }
    return sql.execute(jdbcTemplate, record, args);
  }

  private Object invokeDefaultMethod(Object proxy, Method method, Object[] args)
      throws Throwable {
    final Constructor<Lookup> constructor = MethodHandles.Lookup.class
        .getDeclaredConstructor(Class.class, int.class);
    if (!constructor.isAccessible()) {
      constructor.setAccessible(true);
    }
    final Class<?> declaringClass = method.getDeclaringClass();
    return constructor
        .newInstance(declaringClass,
            MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED
                | MethodHandles.Lookup.PACKAGE | MethodHandles.Lookup.PUBLIC)
        .unreflectSpecial(method, declaringClass).bindTo(proxy).invokeWithArguments(args);
  }

  /**
   * Backport of java.lang.reflect.Method#isDefault()
   */
  private boolean isDefaultMethod(Method method) {
    return (method.getModifiers()
        & (Modifier.ABSTRACT | Modifier.PUBLIC | Modifier.STATIC)) == Modifier.PUBLIC
        && method.getDeclaringClass().isInterface();
  }

  @Builder
  @Getter
  @Setter
  public static class ApiMethod {

    private String apiName;
    private Method method;

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      ApiMethod apiMethod = (ApiMethod) o;
      return Objects.equals(apiName, apiMethod.apiName) &&
          Objects.equals(method, apiMethod.method);
    }

    @Override
    public int hashCode() {

      return Objects.hash(apiName, method);
    }
  }


}
