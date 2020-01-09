package com.efivestar.operation.util.xml;

import com.alibaba.fastjson.JSONObject;
import com.efivestar.operation.annotation.Condition;
import com.efivestar.operation.annotation.Describe;
import com.efivestar.operation.dto.ApiDTO;
import com.efivestar.operation.dto.SqlDTO;
import com.efivestar.operation.exception.OperationException;
import com.efivestar.operation.mapping.Api;
import com.efivestar.operation.mapping.Sql;
import com.efivestar.operation.mapping.Parameter;
import com.efivestar.operation.mapping.Result;
import com.efivestar.operation.mapping.enums.ApiType;
import com.efivestar.operation.mapping.enums.ElementType;
import com.efivestar.operation.mapping.enums.SelectType;
import com.efivestar.operation.mapping.enums.SqlType;
import com.efivestar.operation.util.StringUtils;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.Element;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.CollectionUtils;

@Slf4j
public class XmlParser {

  private static final String COMMON_GROUP = "未分类";

  private static final String SPACE = " ";

  private static ExpressionParser parser = new SpelExpressionParser();

  public static List<Api> parser(Document document) {
    Element root = document.getRootElement();
    List<Element> elements = root.elements();
    List<Api> apis = new ArrayList<>();
    for (Element element : elements) {
      final String elementName = element.getName();
      if (!StringUtils.isEmpty(elementName) && ElementType.API
          .equals(ElementType.valueOf(elementName.toUpperCase()))) {
        final String id = element.attributeValue("id");
        final String groupId = element.attributeValue("groupId");
        final String url = element.attributeValue("url");
        final String type = element.attributeValue("type");
        final String bean = element.attributeValue("bean");
        final String primaryKey = element.attributeValue("primaryKey");
        final Api api = new Api();
        if (!StringUtils.isEmpty(id)) {
          api.setId(id);
        }
        if (!StringUtils.isEmpty(groupId)) {
          api.setGroupId(groupId);
        } else {
          api.setGroupId(COMMON_GROUP);
        }
        if (!StringUtils.isEmpty(url)) {
          api.setUrl(url.startsWith("/") ? url : "/" + url);
        }
        if (!StringUtils.isEmpty(bean)) {
          api.setBean(bean);
        }
        if (!StringUtils.isEmpty(type)) {
          api.setType(ApiType.valueOf(type.toUpperCase()));
        }
        if (!StringUtils.isEmpty(primaryKey)) {
          api.setPrimaryKey(primaryKey);
        } else {
          api.setPrimaryKey("id");
        }
        final List<Element> sqlElements = element.elements();
        if (!CollectionUtils.isEmpty(sqlElements)) {
          List<Sql> sqls = new ArrayList<>();
          for (Element sqlElement : sqlElements) {
            final String sqlName = sqlElement.getName();
//            final String sqlId = sqlElement.attributeValue("id");
            final String sqlNode = sqlElement.asXML();
            final String parameterType = sqlElement.attributeValue("parameterType");
            final Sql sql = new Sql();
//            if (!StringUtils.isEmpty(sqlId)) {
//              sql.setId(sqlId);
//            }
            if (!StringUtils.isEmpty(sqlNode)) {
              final String sqlValue = sqlNode
                  .substring(sqlNode.indexOf(">") + 1, sqlNode.lastIndexOf("<")).trim();
              sql.setValue(sqlValue);
            }
            if (!StringUtils.isEmpty(parameterType)) {
              sql.setParameterType(parameterType);
            } else if (!StringUtils.isEmpty(bean)) {
              sql.setParameterType(bean);
            }
            if (!StringUtils.isEmpty(sqlName)) {
              final SqlType sqlType = SqlType.valueOf(sqlName.toUpperCase());
              sql.setType(sqlType);
              switch (sqlType) {
                case SELECT:
                  final String resultType = sqlElement.attributeValue("resultType");
                  if (!StringUtils.isEmpty(resultType)) {
                    sql.setResultType(resultType);
                  } else if (!StringUtils.isEmpty(bean)) {
                    sql.setResultType(bean);
                  }
                  final String selectType = sqlElement.attributeValue("type");
                  if (!StringUtils.isEmpty(selectType)) {
                    sql.setSelectType(SelectType.valueOf(selectType.toUpperCase()));
                  } else {
                    //默认为list
                    sql.setSelectType(SelectType.LIST);
                  }
                  sql.setId(sqlName + sql.getSelectType().getSuffix());
                  getResultFromSql(sql);
                  if (sql.getResultType() != null) {
                    getRowMapFromSql(sql);
                  }
                  break;
                case INSERT:
                case DELETE:
                case UPDATE:
                  sql.setId(sqlName);
                  break;
              }
            }
            getParamMapFromSql(sql);
            sqls.add(sql);
          }
          api.setSqls(sqls);
        }
        apis.add(api);
      }
    }
    return apis;
  }

  public static String propertyMapToColumn(String property) {
    final Pattern pattern = Pattern.compile("[A-Z]");
    StringBuffer sb = new StringBuffer();
    Matcher matcher = pattern.matcher(property);
    while (matcher.find()) {
      final String param = matcher.group();
      matcher.appendReplacement(sb, "_" + param.toLowerCase());
    }
    matcher.appendTail(sb);
    return sb.toString();
  }

  public static String convertFieldNameToMethod(Result result) {
    final String prefix = "set";
    final String property = result.getProperty();
    if (!StringUtils.isEmpty(property)) {
      char[] cs = property.toCharArray();
      cs[0] -= 32;
      return prefix + String.valueOf(cs);
    }
    return "";
  }


  public static void getParamMapFromSql(Sql sql) {
    final String sqlValue = sql.getValue();
    final Class parameterType = sql.getParameterType();
    if (StringUtils.isEmpty(sqlValue)) {
      return;
    }
    List<Parameter> parameters = new ArrayList<>();
    final String unkonwn = "?";
    Pattern pattern = Pattern.compile("[#,$]\\{\\w+\\}");
    Matcher matcher = pattern.matcher(sqlValue);
    int index = 0;
    while (matcher.find()) {
      String param = matcher.group();
      if (!StringUtils.isEmpty(param)) {
        Parameter parameter = new Parameter();
        final String name = param.substring(param.indexOf("{") + 1, param.indexOf("}"));
        parameter.setName(name);
        parameter.setIndex(++index);
        if (parameterType != null) {
          Field field = null;
          try {
            field = parameterType.getDeclaredField(name);
          } catch (NoSuchFieldException e) {
//            log.warn("未找到参数类型所对应的参数名称:{}", name);
          }
          if (field != null) {
            final Class fieldType = field.getType();
            parameter.setJavaType(fieldType);
            if (fieldType.isEnum()) {
              Object[] enums = fieldType.getEnumConstants();
              parameter.setEnums(enums);
            }
            if (field.isAnnotationPresent(Describe.class)) {
              final Describe annotation = field.getAnnotation(Describe.class);
              parameter.setDesc(annotation.desc());
              parameter.setAlias(annotation.alias());
              parameter.setRequired(annotation.required());
              Condition[] conditions = annotation.condition();
              if (conditions.length > 0) {
                Map<SqlType, String> conditionMap = new HashMap<>();
                for (Condition condition : conditions) {
                  conditionMap.put(condition.type(), condition.value());
                }
                if (!conditionMap.isEmpty()) {
                  parameter.setConditions(conditionMap);
                }
              }
              parameter.setRelate(annotation.relate());
              parameter.setRelateField(annotation.relateField());
            }
          }
        }
        parameters.add(parameter);
      }
    }
    sql.setParams(parameters);
//    sql.setValue(matcher.replaceAll(unkonwn));
  }


  public static void getResultFromSql(Sql sql) {
    final String sqlValue = sql.getValue();
    if (StringUtils.isEmpty(sqlValue)) {
      return;
    }
    List<Result> results = new ArrayList<>();
    final String lowerCaseSql = sqlValue.toLowerCase();
    final String startStr = "select";
    final String endStr = "from";
    final int start = lowerCaseSql.indexOf(startStr);
    final int end = lowerCaseSql.indexOf(endStr);
    final String result = sqlValue.substring(start + startStr.length(), end).trim();
    Class<?> t = sql.getResultType();
    if ("*".equals(result)) {
      //当查询结果是*时
      if (null != t) {
        Field[] fields = t.getDeclaredFields();
        if (fields != null && fields.length > 0) {
          for (Field field : fields) {
            final String fieldName = field.getName();
            final Class<?> type = field.getType();
            if (!StringUtils.isEmpty(fieldName)) {
              Result rs = Result.builder()
                  .property(fieldName)
                  .column(propertyMapToColumn(fieldName))
                  .tableName(propertyMapToColumn(fieldName))
                  .javaType(type)
                  .build();
              if (field.isAnnotationPresent(Describe.class)) {
                final Describe annotation = field.getAnnotation(Describe.class);
                rs.setTableName(annotation.alias());
              }
              results.add(rs);
            }
          }
        }
      }
    } else {
      //当查询结果罗列完整时，先根据sql解析为result，如果定义了返回类型，在参照返回类型，一一对应判断
      final String[] columns = result.split(",");
      if (columns != null && columns.length > 0) {
        for (String column : columns) {
          if (!StringUtils.isEmpty(column)) {
            column = column.trim();
            final String lowerCaseColumn = column.toLowerCase();
            final int index = lowerCaseColumn.indexOf(SPACE + "as" + SPACE);
            if (index > -1) {
              final String alias = column.substring(index + 3);
              if (!StringUtils.isEmpty(alias)) {
                Result rs = Result.builder()
                    .column(alias.trim())
                    .tableName(alias.trim())
                    .property(alias.trim())
                    .build();
                results.add(rs);
              }
            } else {
              if (column.indexOf(SPACE) == -1) {
                Result rs = Result.builder()
                    .column(column)
                    .tableName(column)
                    .property(column)
                    .build();
                results.add(rs);
              } else {
                final String alias = column.substring(column.lastIndexOf(SPACE) + 1);
                if (!StringUtils.isEmpty(alias)) {
                  Result rs = Result.builder()
                      .column(alias)
                      .tableName(alias)
                      .property(alias)
                      .build();
                  results.add(rs);
                }
              }

            }
          }
        }
      }
      if (!CollectionUtils.isEmpty(results)) {
        for (Result r : results) {
          if (null != t) {
            final Field[] fields = t.getDeclaredFields();
            if (fields != null && fields.length > 0) {
              for (Field field : fields) {
                if (propertyMapToColumn(field.getName()).equals(r.getColumn())) {
                  r.setProperty(field.getName());
                  r.setJavaType(field.getType());
                  if (field.isAnnotationPresent(Describe.class)) {
                    final Describe annotation = field.getAnnotation(Describe.class);
                    r.setTableName(annotation.alias());
                  }
                }
              }
            }
          }
        }
        results = results.stream().filter(rs -> !StringUtils.isEmpty(rs.getProperty()))
            .collect(Collectors.toList());
      }
    }
    sql.setResults(results);
  }

  public static void getRowMapFromSql(Sql sql) {
    //此处rowMapper可以为xml中resultMap而做配置，此版本无resultMap的解析，
    // 所以此处根据resultType进行解析，此版本rowMapper无意义
    sql.setResultMap((ResultSet rs, int rowNum) -> {
      Object object = null;
      try {
        Class<?> t = sql.getResultType();
        object = t.newInstance();
        final List<Result> results = sql.getResults();
        if (!CollectionUtils.isEmpty(results)) {
          for (Result result : results) {
            final String methodName = convertFieldNameToMethod(result);
            if (!StringUtils.isEmpty(methodName)) {
              Method method = t.getMethod(methodName, result.getJavaType());
              method.invoke(object,
                  rs.getObject(result.getColumn(), result.getJavaType()));
            }
          }
        }
      } catch (Exception e) {
        log.warn("解析结果集失败，原因为:{},{}", new Object[]{e, e.getMessage()});
        throw new OperationException("XmlParser.parser", "解析结果集失败");
      }
      return object;
    });
  }


  public static void main(String[] args) {
    Expression exp = parser.parseExpression("T(Class).forName(bean)");
//    ApiDTO apiDTO = new ApiDTO();
//    apiDTO.setName("null");

    JSONObject jsonObject = new JSONObject();
    jsonObject.put("bean", "java.lang.Stri");
    ApiDTO apiDTO = JSONObject.toJavaObject(jsonObject, ApiDTO.class);
    Class message = exp.getValue(apiDTO, Class.class);
    System.out.println(message);
  }

}
