package com.chick.operation.controller;

import com.alibaba.fastjson.JSONObject;
import com.chick.operation.exception.OperationException;
import com.chick.operation.http.ResourceServlet;
import com.chick.operation.mapping.Api;
import com.chick.operation.mapping.entity.Record;
import com.chick.operation.mapping.XmlConfiguration;
import com.chick.operation.mapping.enums.ApiType;
import com.chick.operation.service.BaseService;
import com.chick.operation.dto.ResultDTO;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * api所对应的单一sql接口
 */
@RestController
@RequestMapping("${operation.prefix:/operation}/{apiName}")
public class BaseController {

  @Autowired
  private BaseService baseService;

  @RequestMapping("/**")
  public ResultDTO baseHandler(@PathVariable String apiName,
      @RequestBody(required = false) JSONObject paramBody,
      HttpServletRequest request) {
    final Map<String, String[]> params = request.getParameterMap();
    final Map<String, Object> map = new HashMap<>();
    if (!CollectionUtils.isEmpty(params)) {
      params.forEach((k, v) -> {
        if (v.length > 0) {
          map.put(k, v[0]);
        }
      });
    }
    final JSONObject paramUrl = new JSONObject(map);
    final String requestURI = request.getRequestURI();
    if (!StringUtils.isEmpty(requestURI)) {
      final String path = requestURI.substring(requestURI.indexOf(apiName) + apiName.length());
      checkExistApi(apiName, path);
    }
    final Record record = initRecord(request,apiName);
    Object result = null;
    if (null != paramBody && !paramBody.isEmpty()) {
      result = baseService.execute(apiName,record, paramBody);
    } else if (null != paramUrl && !paramUrl.isEmpty()) {
      result = baseService.execute(apiName,record, paramUrl);
    } else {
      result = baseService.execute(apiName,record, new JSONObject());
    }
    return ResultDTO.builder().data(result).message("base-操作成功").build();
  }

  void checkExistApi(String apiName, String url) {
    final Api api = XmlConfiguration.loadApi(apiName);
    if (api == null) {
      throw new OperationException("BaseController.checkExistApi", "未配置此api");

    }
    final String urlStr = api.getUrl();
    if (!StringUtils.isEmpty(urlStr)) {
      if (!urlStr.equals(url)) {
        throw new OperationException("BaseController.checkExistApi", "未找到匹配的url");
      }
    } else {
      if (!StringUtils.isEmpty(url)) {
        throw new OperationException("BaseController.checkExistApi", "未找到匹配的url");
      }
    }
    final ApiType apiType = api.getType();
    if (null != apiType) {
      if (!apiType.equals(ApiType.BASE)) {
        throw new OperationException("BaseController.checkExistApi", "该api不是base风格");
      }
    }
  }

  protected static Record initRecord(HttpServletRequest request, String apiName) {
   return Record.builder()
        .apiName(apiName)
        .url(request.getRequestURI())
        .ip(request.getRemoteAddr())
        .executor(
            String.valueOf(request.getSession().getAttribute(ResourceServlet.SESSION_USER_KEY)))
        .build();

  }

}
