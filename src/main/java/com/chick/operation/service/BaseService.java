package com.chick.operation.service;

import com.alibaba.fastjson.JSONObject;
import com.chick.operation.mapping.entity.Record;
import com.chick.operation.repository.BaseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class BaseService<T> {

  @Autowired
  private BaseRepository<T> baseRepository;

  public Object execute(String apiName,Record record, JSONObject jsonObject) {
    //TO_DO 业务代码
    return baseRepository.execute(apiName,record, jsonObject);
  }

//  Object[] convertJsonToArray(String apiName, JSONObject jsonObject) {
//    final Api api = XmlConfiguration.loadApi(apiName);
//    final List<Sql> sqls = api.getSqls();
//    if (CollectionUtils.isEmpty(sqls)) {
//      throw new OperationException("BaseService.convertJsonToArray", "该api未配置sql");
//    }
//    final Sql sql = sqls.get(0);
//    final List<Parameter> parameters = sql.getParams();
//    if (CollectionUtils.isEmpty(parameters)) {
//      throw new OperationException("BaseService.convertJsonToArray", "该sql参数未知");
//    }
//    List<Object> objects = parameters.stream().sorted(Comparator.comparing(Parameter::getIndex))
//        .map(parameter ->
//            jsonObject.get(parameter.getName())
//        ).filter(item -> item != null).collect(Collectors.toList());
//    return objects.toArray(new Object[objects.size()]);
//  }

}
