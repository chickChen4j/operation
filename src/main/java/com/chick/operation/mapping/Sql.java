package com.chick.operation.mapping;

import com.chick.operation.dto.PagedResultDTO;
import com.chick.operation.exception.OperationException;
import com.chick.operation.mapping.entity.Record;
import com.chick.operation.mapping.enums.SelectType;
import com.chick.operation.mapping.enums.SqlType;
import com.chick.operation.util.StringUtils;
import com.chick.operation.util.dynamic.Bindings;
import com.chick.operation.util.dynamic.SqlMeta;
import com.chick.operation.util.dynamic.SqlTemplate;
import com.chick.operation.util.dynamic.SqlTemplateEngin;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.CollectionUtils;

@EqualsAndHashCode
@ToString
//@Component
@NoArgsConstructor
@Slf4j
@ManagedResource(objectName = "bean:name=Sql", description = "解析某api下的sql语句")
public class Sql {

  /**
   * 方法名
   */
  private String id;

  /**
   * sql类型
   */
  private SqlType type;

  /**
   * sql语句
   */
  private String value;

  /**
   * 参数
   */
  private List<Parameter> params;

  /**
   * 参数类型
   */
  private Class<?> parameterType;

  /**
   * 返回的结果集处理器
   */
  @JsonIgnore
  private RowMapper<?> resultMap;

  /**
   * 结果值
   */
  private List<Result> results;

  /**
   * 查询的类型
   */
  private SelectType selectType;

  /**
   * 返回值类型
   */
  private Class<?> resultType;


  @ManagedOperation(description = "设置参数类型")
  public void setParameterType(String className) {
    try {
      this.parameterType = Class.forName(className);
    } catch (ClassNotFoundException e) {
      log.warn("设置参数类型出错，原因为:{},{}", new Object[]{e, e.getMessage()});
    }
  }

  @ManagedOperation(description = "设置返回结果类型")
  public void setResultType(String className) {
    try {
      this.resultType = Class.forName(className);
    } catch (ClassNotFoundException e) {
      log.warn("设置返回结果类型出错，原因为:{},{}", new Object[]{e, e.getMessage()});
    }
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public SqlType getType() {
    return type;
  }

  public void setType(SqlType type) {
    this.type = type;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public List<Parameter> getParams() {
    return params;
  }

  public void setParams(List<Parameter> params) {
    this.params = params;
  }

  public Class<?> getParameterType() {
    return parameterType;
  }

  public void setParameterType(Class<?> parameterType) {
    this.parameterType = parameterType;
  }

  public Class<?> getResultType() {
    return resultType;
  }

  public void setResultType(Class<?> resultType) {
    this.resultType = resultType;
  }

  public SelectType getSelectType() {
    return selectType;
  }

  public void setSelectType(SelectType selectType) {
    this.selectType = selectType;
  }

  public RowMapper<?> getResultMap() {
    return resultMap;
  }

  public void setResultMap(RowMapper<?> resultMap) {
    this.resultMap = resultMap;
  }

  public List<Result> getResults() {
    return results;
  }

  public void setResults(List<Result> results) {
    this.results = results;
  }

  public Object execute(JdbcTemplate jdbcTemplate, Record record, Object[] args) {
    Object result = null;
    try {
      if (StringUtils.isEmpty(this.value)) {
        throw new OperationException("Sql.execute", "sql语句不存在");
      }
      if (args.length != 1) {
        throw new OperationException("Sql.execute", "参数错误");
      }
      //动态sql解析工具
      SqlTemplateEngin sqlTemplateEngin = new SqlTemplateEngin();
      SqlTemplate sqlTemplate = sqlTemplateEngin.getSqlTemplate(this.value);
      Bindings bindings = new Bindings();
      //因为在repository中已经对所有入参做了转化，所以此处直接强转
      InParameter[] inParameters = (InParameter[]) args[0];
      for (InParameter parameter : inParameters) {
        if (parameter.getJavaType() != null && parameter.getJavaType().isEnum()) {
          bindings.bind(parameter.getName(),
              parameter.getValue() != null ? parameter.getValue().toString()
                  : parameter.getValue());
          continue;
        }
        bindings.bind(parameter.getName(), parameter.getValue());
      }
      SqlMeta sqlMeta = sqlTemplate.process(bindings);
      final String executeSqlVal = sqlMeta.getSql();
      args = sqlMeta.getParameter().toArray(new Object[sqlMeta.getParameter().size()]);
      if (StringUtils.isEmpty(executeSqlVal)) {
        throw new OperationException("Sql.execute", "sql语句转化错误");
      }
      record.setExecuteArgs(sqlMeta.getParameter().toString());
      record.setExecuteSql(executeSqlVal.trim());
      record.setExecuteTime(new Date());
      switch (this.type) {
        case SELECT:
          if (SelectType.ONE.equals(this.selectType)) {
            try {
              if (resultType != null) {
                result = jdbcTemplate
                    .queryForObject(executeSqlVal, args,
                        new BeanPropertyRowMapper(this.resultType));
              } else if (this.resultMap != null) {
                result = jdbcTemplate.queryForObject(executeSqlVal, this.resultMap, args);
              } else {
                List<Object> list = (List) jdbcTemplate.queryForList(executeSqlVal, args);
                if (!CollectionUtils.isEmpty(list)) {
                  result = list.get(0);
                }
              }
            } catch (EmptyResultDataAccessException e) {
              return null;
            }
          } else if (SelectType.LIST.equals(this.selectType)) {
            result = executeSql(jdbcTemplate, executeSqlVal, args);
          } else if (SelectType.PAGE.equals(this.selectType)) {
            result = executeSql(jdbcTemplate, executeSqlVal, args);
            final int limitIndex = executeSqlVal.lastIndexOf("limit");
            if (limitIndex == -1) {
              throw new OperationException("Sql.execute", "未找到limit关键词");
            }
            final String rowSql =
                "select count(1) from (" + executeSqlVal
                    .substring(0, limitIndex)
                    + ") table_";
            final int size = (Integer) args[args.length - 1];
            final int number = (Integer) args[args.length - 2] / size;
            args = Arrays.copyOf(args, args.length - 2);
            final Long total = (Long) this
                .queryOneColumnForSigetonRow(jdbcTemplate, rowSql, args, Long.class);
            final long totalPage = total % size == 0 ? total / size : total / size + 1;
            PagedResultDTO pagedResultDTO = PagedResultDTO.builder()
                .content((List) result)
                .totalElements(total)
                .totalPages(totalPage)
                .size(size)
                .number(number)
                .first(number == 0 ? true : false)
                .last(number + 1 == totalPage)
                .build();
            return pagedResultDTO;
          }

          break;
        case UPDATE:
        case DELETE:
        case INSERT:
          result = jdbcTemplate.update(executeSqlVal, args);
          break;
        default:
          throw new OperationException("Sql.execute", "不存在的sql类型");
      }
    } catch (Exception e) {
      record.setExecuteIsSuccess(Boolean.FALSE);
      record.setExecuteResult(e.getMessage());
      log.warn("执行sql操作失败,原因为:{}", e.getMessage());
      throw new OperationException("Sql.execute", "执行sql语句出现错误");
    } finally {
      try {
        record.insert(jdbcTemplate);
      } catch (Exception e) {
        log.warn("插入操作记录失败，原因为:{}", new Object[]{e.getMessage()});
      }
    }
    return result;
  }

  /**
   * 根据有无返回值类型定义来执行sql
   */
  protected Object executeSql(JdbcTemplate jdbcTemplate, String executeSqlVal, Object[] args) {
    if (this.resultType != null) {
      return jdbcTemplate
          .query(executeSqlVal, args, new BeanPropertyRowMapper(this.resultType));
    } else if (this.resultMap != null) {
      //durid不支持rowMapper，故优先级推后
      return jdbcTemplate.query(executeSqlVal, args, this.resultMap);
    } else {
      return jdbcTemplate.queryForList(executeSqlVal, args);
    }
  }

  /**
   * 只查询一列数据类型对象。用于只有一行查询结果的数据
   *
   * @param cla Integer.class,Float.class,Double.class,Long.class,Boolean.class,Char.class,Byte.class,Short.class
   */
  protected Object queryOneColumnForSigetonRow(JdbcTemplate jdbcTemplate, String sql,
      Object[] params,
      Class cla) {
    Object result = null;
    try {
      if (params == null || params.length > 0) {
        result = jdbcTemplate.queryForObject(sql, params, cla);
      } else {
        result = jdbcTemplate.queryForObject(sql, cla);
      }
      if (result == null) {
        result = 0;
      }
    } catch (Exception ex) {
      throw new OperationException("Sql.queryOneColumnForSigetonRow", "查询结果总数失败");
    }
    return result;
  }

}
