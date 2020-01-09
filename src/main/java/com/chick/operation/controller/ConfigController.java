package com.chick.operation.controller;

import com.chick.operation.dto.Pageable;
import com.chick.operation.dto.PagedResultDTO;
import com.chick.operation.dto.ResultDTO;
import com.chick.operation.dto.Sortable;
import com.chick.operation.exception.OperationException;
import com.chick.operation.http.ResourceServlet;
import com.chick.operation.mapping.Api;
import com.chick.operation.mapping.XmlConfiguration;
import com.chick.operation.mapping.entity.ApiConfig;
import com.chick.operation.mapping.entity.SqlConfig;
import com.chick.operation.mapping.enums.ApiType;
import com.chick.operation.mapping.enums.SelectType;
import com.chick.operation.mapping.enums.SqlType;
import com.chick.operation.service.ConfigService;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

@RestController
@RequestMapping(value = "${operation.prefix:/operation}/config")
public class ConfigController {

  @Value("${operation.prefix:/operation}")
  private String prefix;

  @Autowired
  private ConfigService configService;


  @GetMapping("/xml")
  public ResultDTO getXmlConfig() {
    final ConcurrentHashMap map = XmlConfiguration.getApiMap();
    if (CollectionUtils.isEmpty(map)) {
      return ResultDTO.builder().build();
    }
    map.forEach((key, value) -> {
      ((Api) value).setPrefix(prefix);
    });
    final Object apis = map.values().stream().sorted(Comparator.comparing(Api::getGroupId))
        .collect(Collectors.toList());
    return ResultDTO.builder()
        .data(apis)
        .build();
  }

  @PostMapping("/api")
  public ResultDTO addApi(@RequestBody ApiConfig apiConfig, HttpServletRequest request) {
    apiConfig.setId(null);
    Object createdBy = request.getSession().getAttribute(ResourceServlet.SESSION_USER_KEY);
    apiConfig.setCreatedBy(
        createdBy == null ? null : String.valueOf(createdBy));
    apiConfig.setCreateTime(new Date());
    checkApiConfig(apiConfig, request, true);
    configService.addApiConfig(apiConfig);
    return ResultDTO.builder().message("新增api成功").build();
  }

  @PutMapping("/api/{id}")
  public ResultDTO updateApi(@RequestBody ApiConfig apiConfig, @PathVariable Long id,
      HttpServletRequest request) {
    apiConfig.setId(id);
    apiConfig.setLastModifiedTime(new Date());
    Object lastModifiedBy = request.getSession().getAttribute(ResourceServlet.SESSION_USER_KEY);
    apiConfig.setLastModifiedBy(
        lastModifiedBy == null ? null :
            String.valueOf(lastModifiedBy));
    checkApiConfig(apiConfig, request, false);
    configService.updateApiConfig(apiConfig);
    return ResultDTO.builder().message("修改api成功").build();
  }

  @DeleteMapping("/api/{id}")
  public ResultDTO deleteApi(@PathVariable Long id) {
    configService.deleteApiConfig(id);
    return ResultDTO.builder().message("删除api成功").build();
  }

  @GetMapping("/api")
  public ResultDTO selectApiByPage(ApiConfig apiConfig, Pageable pageable, Sortable sortable) {
    Pageable page = Pageable.builder().build();
    if (pageable.getNumber() != null) {
      page.setNumber(pageable.getNumber());
    }
    if (pageable.getSize() != null) {
      page.setSize(pageable.getSize());
    }
    PagedResultDTO<ApiConfig> pagedResultDTO = configService
        .findApiConfigByPage(apiConfig, page, sortable);
    return ResultDTO.builder().data(pagedResultDTO).message("分页查询api成功").build();
  }

  @PostMapping("/sql")
  public ResultDTO addSql(@RequestBody SqlConfig sqlConfig, HttpServletRequest request) {
    sqlConfig.setId(null);
    Object createdBy = request.getSession().getAttribute(ResourceServlet.SESSION_USER_KEY);
    sqlConfig.setCreatedBy(
        createdBy != null ? String.valueOf(createdBy) : null);
    sqlConfig.setCreateTime(new Date());
    checkSqlConfig(sqlConfig, request);
    configService.addSqlConfig(sqlConfig);
    return ResultDTO.builder().message("新增sql成功").build();
  }

  @PutMapping("/sql/{id}")
  public ResultDTO updateSql(@RequestBody SqlConfig sqlConfig, @PathVariable Long id,
      HttpServletRequest request) {
    sqlConfig.setId(id);
    sqlConfig.setLastModifiedTime(new Date());
    Object lastModifiedBy = request.getSession().getAttribute(ResourceServlet.SESSION_USER_KEY);
    sqlConfig.setLastModifiedBy(
        lastModifiedBy == null ? null : String.valueOf(lastModifiedBy));
    checkSqlConfig(sqlConfig, request);
    configService.updateSqlConfig(sqlConfig);
    return ResultDTO.builder().message("修改sql成功").build();
  }

  @DeleteMapping("/sql/{id}")
  public ResultDTO deleteSql(@PathVariable Long id) {
    configService.deleteSqlConfig(id);
    return ResultDTO.builder().message("删除sql成功").build();
  }

  @GetMapping("/sql")
  public ResultDTO selectSqlByPage(SqlConfig sqlConfig, Pageable pageable, Sortable sortable) {
    Pageable page = Pageable.builder().build();
    if (pageable.getNumber() != null) {
      page.setNumber(pageable.getNumber());
    }
    if (pageable.getSize() != null) {
      page.setSize(pageable.getSize());
    }
    PagedResultDTO<SqlConfig> pagedResultDTO = configService
        .findSqlConfigByPage(sqlConfig, page, sortable);
    return ResultDTO.builder().data(pagedResultDTO).message("分页查询sql成功").build();
  }


  void checkApiConfig(ApiConfig apiConfig, HttpServletRequest request, boolean isInsert) {

    if (StringUtils.isEmpty(apiConfig.getName())) {
      throw new OperationException("ConfigController.checkApiConfig", "API名称不能为空");
    }
    if (isInsert) {
      List<ApiConfig> list = configService
          .findAllApi(ApiConfig.builder().name(apiConfig.getName()).build());
      if (!CollectionUtils.isEmpty(list)) {
        throw new OperationException("ConfigController.checkApiConfig", "API的名称不能重复");
      }
    }
    if (StringUtils.isEmpty(apiConfig.getGroupId())) {
      apiConfig.setGroupId("未分类");
    }
    if (!StringUtils.isEmpty(apiConfig.getUrl()) && !apiConfig.getUrl().startsWith("/")) {
      apiConfig.setUrl("/" + apiConfig.getUrl());

    }
    if (apiConfig.getType() == null) {
      apiConfig.setType(ApiType.BASE);
    }
    if (!StringUtils.isEmpty(apiConfig.getBean())) {
      try {
        Thread.currentThread().getContextClassLoader().loadClass(apiConfig.getBean());
      } catch (ClassNotFoundException e) {
        throw new OperationException("ConfigController.checkApiConfig", "bean类型不存在");
      }
//      throw new OperationException("ConfigController.checkConfig", "bean不能为空");
    }
    if (StringUtils.isEmpty(apiConfig.getPrimaryKey())) {
      apiConfig.setPrimaryKey("id");
    }
  }

  void checkSqlConfig(SqlConfig sqlConfig, HttpServletRequest request) {

    if (sqlConfig.getType() == null) {
      throw new OperationException("ConfigController.checkSqlConfig", "type不能为空");
    }
    if (sqlConfig.getApiId() == null) {
      throw new OperationException("ConfigController.checkSqlConfig", "apiId不能为空");
    }
    final ApiConfig apiConfig = configService.findByApiId(sqlConfig.getApiId());
    if (apiConfig == null) {
      throw new OperationException("ConfigController.checkSqlConfig", "关联的api不存在");
    }

    if (StringUtils.isEmpty(sqlConfig.getValue())) {
      throw new OperationException("ConfigController.checkSqlConfig", "sql的值不能为空");
    }
    if (SqlType.UNKNOWN.equals(sqlConfig.getType())) {
      final String value = sqlConfig.getValue().toLowerCase().trim();
      if (value.startsWith("select")) {
        sqlConfig.setType(SqlType.SELECT);
      } else if (value.startsWith("insert")) {
        sqlConfig.setType(SqlType.INSERT);
      } else if (value.startsWith("update")) {
        sqlConfig.setType(SqlType.UPDATE);
      } else if (value.startsWith("delete")) {
        sqlConfig.setType(SqlType.DELETE);
      }
    }

    String bean = apiConfig.getBean();
    String parameterType = sqlConfig.getParameterType();
    String resultType = sqlConfig.getResultType();
    if (StringUtils.isEmpty(parameterType)) {
      sqlConfig.setParameterType(bean);
    } else {
      try {
        Thread.currentThread().getContextClassLoader().loadClass(parameterType);
      } catch (ClassNotFoundException e) {
        throw new OperationException("ConfigController.checkSqlConfig", "参数类型不存在");
      }
    }
    if (SqlType.SELECT.equals(sqlConfig.getType())) {
      if (StringUtils.isEmpty(resultType)) {
        sqlConfig.setResultType(bean);
      } else {
        try {
          Thread.currentThread().getContextClassLoader().loadClass(resultType);
        } catch (ClassNotFoundException e) {
          throw new OperationException("ConfigController.checkSqlConfig", "返回值类型不存在");
        }
      }
      if (sqlConfig.getSelectType() == null) {
        sqlConfig.setSelectType(SelectType.LIST);
      }
    } else {
      sqlConfig.setResultType(null);
      sqlConfig.setSelectType(null);
    }

    sqlConfig.setValue(sqlConfig.getValue().trim());
  }

}
