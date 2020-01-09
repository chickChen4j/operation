package com.chick.operation.controller;

import com.alibaba.fastjson.JSONObject;
import com.chick.operation.dto.Pageable;
import com.chick.operation.dto.PagedResultDTO;
import com.chick.operation.dto.ResultDTO;
import com.chick.operation.dto.Sortable;
import com.chick.operation.exception.OperationException;
import com.chick.operation.mapping.Api;
import com.chick.operation.mapping.Parameter;
import com.chick.operation.mapping.Sql;
import com.chick.operation.mapping.entity.Record;
import com.chick.operation.mapping.XmlConfiguration;
import com.chick.operation.mapping.enums.ApiType;
import com.chick.operation.mapping.enums.SqlType;
import com.chick.operation.service.RestfulService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * api所对应的restful接口
 */
@RestController
@RequestMapping("${operation.prefix:/operation}/rest/{apiName}")
public class RestfulController<T> {

  @Autowired
  private RestfulService<T> restfulService;

  @PostMapping
  public ResultDTO insert(@PathVariable String apiName, @RequestBody JSONObject jsonObject,
      HttpServletRequest request) {
    checkIsRest(apiName);
    T bean = convertObjectToBean(apiName, jsonObject);
    checkParamValid(bean, apiName, SqlType.INSERT);
    restfulService
        .insert(apiName, initRecord(request, apiName), bean);
    return ResultDTO.builder()
        .message("controller-新增操作成功")
        .build();
  }

  @DeleteMapping("/{id}")
  public ResultDTO delete(@PathVariable String apiName, @PathVariable Long id,
      HttpServletRequest request) {
    checkIsRest(apiName);
    restfulService.delete(apiName, initRecord(request, apiName), id);
    return ResultDTO.builder().message("controller-删除操作成功").build();
  }

  @PutMapping("/{id}")
  public ResultDTO update(@PathVariable String apiName, @PathVariable Long id,
      @RequestBody JSONObject jsonObject, HttpServletRequest request) {
    checkIsRest(apiName);
    T bean = convertObjectToBean(apiName, jsonObject);
    checkParamValid(bean, apiName, SqlType.UPDATE);
    setBeanId(apiName, jsonObject, id);
    restfulService
        .update(apiName, initRecord(request, apiName), bean,
            id);
    return ResultDTO.builder().message("controller-修改操作成功").build();
  }

  @GetMapping("/{id}")
  public ResultDTO selectOne(@PathVariable String apiName, @PathVariable Long id,
      HttpServletRequest request) {
    checkIsRest(apiName);
    final T t = restfulService.selectOne(apiName, initRecord(request, apiName), id);
    return ResultDTO.builder().data(t).message("controller-查询操作成功").build();
  }

  @GetMapping
  public ResultDTO selectAll(@PathVariable String apiName, HttpServletRequest request) {
    checkIsRest(apiName);
    final JSONObject jsonObject = convertParams(request);
    final List<T> tList = restfulService
        .selectAll(apiName, initRecord(request, apiName), convertObjectToBean(apiName, jsonObject));
    return ResultDTO.builder().data(tList).message("controller-查询操作成功").build();
  }

  @GetMapping("/page")
  public ResultDTO selectByPage(@PathVariable String apiName, HttpServletRequest request) {
    checkIsRest(apiName);
    final JSONObject jsonObject = convertParams(request);
    final Pageable pageable = Pageable.builder().build();
    if (jsonObject.containsKey(Pageable.SIZE)) {
      if (jsonObject.getInteger(Pageable.SIZE) != null) {
        pageable.setSize(jsonObject.getInteger(Pageable.SIZE));
      }
      jsonObject.remove(Pageable.SIZE);
    }
    if (jsonObject.containsKey(Pageable.NUMBER)) {
      if (jsonObject.getInteger(Pageable.NUMBER) != null) {
        pageable.setNumber(jsonObject.getInteger(Pageable.NUMBER));
      }
      jsonObject.remove(Pageable.NUMBER);
    }
    final Sortable sortable = Sortable.builder().build();
    if (jsonObject.containsKey(Sortable.SORT_NAME)) {
      if (!StringUtils.isEmpty(jsonObject.getString(Sortable.SORT_NAME))) {
        sortable.setSortName(jsonObject.getString(Sortable.SORT_NAME));
      }
      jsonObject.remove(Sortable.SORT_NAME);
    }
    if (jsonObject.containsKey(Sortable.SORT_ORDER)) {
      if (!StringUtils.isEmpty(jsonObject.getString(Sortable.SORT_ORDER))) {
        sortable.setSortOrder(jsonObject.getString(Sortable.SORT_ORDER));
      }
      jsonObject.remove(Sortable.SORT_ORDER);
    }
    final PagedResultDTO<T> pagedResultDTO = restfulService
        .selectByPage(apiName, initRecord(request, apiName),
            convertObjectToBean(apiName, jsonObject), pageable, sortable);
    return ResultDTO.builder().data(pagedResultDTO).message("controller-分页查询操作成功").build();
  }

  JSONObject convertParams(ServletRequest servletRequest) {
    final Map<String, String[]> params = servletRequest.getParameterMap();
    final Map<String, Object> map = new HashMap<>();
    if (!CollectionUtils.isEmpty(params)) {
      params.forEach((k, v) -> {
        if (v.length > 0) {
          map.put(k, v[0]);
        }
      });
    }
    return new JSONObject(map);
  }

  T convertObjectToBean(String apiName, JSONObject jsonObject) {
    final Api api = XmlConfiguration.loadApi(apiName);
    if (null == api) {
      throw new OperationException("RestfulController.convertObjectToBean", "未配置此api");
    }
    final Class<?> beanCalss = api.getBean();
    if (null == beanCalss) {
      throw new OperationException("RestfulController.convertObjectToBean", "未配置此api的bean属性");
    }
    return (T) JSONObject.parseObject(jsonObject.toJSONString(), beanCalss);

  }

  /**
   * 检查参数(TO_DO_可结合xml配置，比如属性名前加!来进行必要性检测) 此版本暂不考虑 此处使用el表达式来制定每个参数的校验规则
   */
  protected void checkParamValid(T bean, String apiName, SqlType sqlType) {
//    jsonObject.forEach((k, v) -> {
//      if ("id".equals(k) && null == v) {
//        throw new OperationException("RestfulController.checkParamValid", "id不能为空");
//      }
//    });
    ExpressionParser parser = new SpelExpressionParser();
//    EvaluationContext evaluationContext = new StandardEvaluationContext(jsonObject);
    final Api api = XmlConfiguration.loadApi(apiName);
    if (api != null) {
      List<Sql> sqls = api.getSqls();
      if (!CollectionUtils.isEmpty(sqls)) {
        Optional<Sql> sql = sqls.stream()
            .filter(item -> sqlType.equals(item.getType())).findFirst();
        if (sql.isPresent()) {
          List<Parameter> parameters = sql.get().getParams();
          if (!CollectionUtils.isEmpty(parameters)) {
            parameters.stream().forEach(parameter -> {
              try {
                Map<SqlType, String> conditionMap = parameter.getConditions();
                if (conditionMap != null && !conditionMap.isEmpty()) {
                  if (!StringUtils.isEmpty(conditionMap.get(sqlType))) {
                    Expression expression = parser.parseExpression(conditionMap.get(sqlType));
                    Boolean legal = expression.getValue(bean, Boolean.class);
                    if (!legal) {
                      throw new OperationException("RestfulController.checkParamValid",
                          parameter.getName() + "不合法");
                    }
                  }
                }
              } catch (OperationException e) {
                throw e;
              } catch (EvaluationException e) {
                throw new OperationException("RestfulController.checkParamValid",
                    parameter.getName() + "的新增接口校验规则EL表达式不合法");
              } catch (ParseException e) {
                throw new OperationException("RestfulController.checkParamValid",
                    parameter.getName() + "的接口校验规则EL表达式不合法");
              }

            });
          }
        }
      }
    }
  }

  protected void checkApiNameExist(String apiName) {
    if (!XmlConfiguration.existApi(apiName)) {
      throw new OperationException("RestfulController.checkApiNameExist", "未配置此api");
    }
  }

  protected void checkIsRest(String apiName) {
    this.checkApiNameExist(apiName);
    final Api api = XmlConfiguration.loadApi(apiName);
    if (!ApiType.RESTFUL.equals(api.getType())) {
      throw new OperationException("RestfulController.checkIsRest", "该api不是restful风格");
    }
  }

  protected void setBeanId(String apiName, JSONObject jsonObject, Long id) {
    final Api api = XmlConfiguration.loadApi(apiName);
    jsonObject.put(api.getPrimaryKey(), id);
  }

  Record initRecord(HttpServletRequest request, String apiName) {
    return BaseController.initRecord(request, apiName);
  }

}
