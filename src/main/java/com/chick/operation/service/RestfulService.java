package com.chick.operation.service;

import com.chick.operation.dto.Pageable;
import com.chick.operation.dto.PagedResultDTO;
import com.chick.operation.dto.Sortable;
import com.chick.operation.exception.OperationException;
import com.chick.operation.mapping.Api;
import com.chick.operation.mapping.entity.Record;
import com.chick.operation.mapping.Sql;
import com.chick.operation.mapping.XmlConfiguration;
import com.chick.operation.mapping.enums.SelectType;
import com.chick.operation.repository.BaseRepository;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@Slf4j
@Transactional
public class RestfulService<T> {

  @Autowired
  private BaseRepository<T> baseRepository;

  public void insert(String apiName, Record record, T t) {
    baseRepository.insert(apiName, record, t);
  }

  public void delete(String apiName, Record record, Long id) {
    checkExist(apiName, record, id);
    baseRepository.delete(apiName, record, id);
  }

  public void update(String apiName, Record record, T t, Long id) {
    checkExist(apiName,record, id);
    baseRepository.update(apiName, record, t);
  }

  public List<T> selectAll(String apiName, Record record, T t) {
    final List<T> ts = baseRepository.selectAll(apiName, record, t);
    return ts;
  }

  public PagedResultDTO selectByPage(String apiName, Record record, T t, Pageable pageable,
      Sortable sortable) {
    final PagedResultDTO<T> pagedResultDTO = baseRepository
        .selectByPage(apiName, record, t, pageable, sortable);
    return pagedResultDTO;
  }

  public T selectOne(String apiName, Record record, Long id) {
    return baseRepository.selectOne(apiName, record, id);
  }

  protected void checkExist(String apiName, Record record, Long id) {
    final Api api = XmlConfiguration.loadApi(apiName);
    if (null == api) {
      return;
    }
    final List<Sql> sqls = api.getSqls();
    if (CollectionUtils.isEmpty(sqls)) {
      return;
    }
    boolean flag = sqls.stream().anyMatch(item -> SelectType.ONE.equals(item.getSelectType()));
    if (!flag) {
      return;
    }
    final T t = baseRepository.selectOne(apiName, record, id);
    if (null == t) {
      throw new OperationException("RestfulService.checkExist", "操作的数据不存在");
    }
  }

}
