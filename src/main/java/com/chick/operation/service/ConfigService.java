package com.chick.operation.service;

import com.chick.operation.dto.Pageable;
import com.chick.operation.dto.PagedResultDTO;
import com.chick.operation.dto.Sortable;
import com.chick.operation.exception.OperationException;
import com.chick.operation.mapping.entity.ApiConfig;
import com.chick.operation.mapping.entity.SqlConfig;
import com.chick.operation.mapping.enums.ApiType;
import com.chick.operation.repository.BaseRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class ConfigService {

  @Autowired
  private BaseRepository baseRepository;

  public boolean addApiConfig(ApiConfig apiConfig) {
    return baseRepository.insertTable(apiConfig);
  }

  public PagedResultDTO<ApiConfig> findApiConfigByPage(ApiConfig apiConfig, Pageable pageable,Sortable sortable) {
    return baseRepository.findByPage(apiConfig, pageable,sortable);
  }

  public boolean updateApiConfig(ApiConfig apiConfig) {
    ApiConfig config = this.findByApiId(apiConfig.getId());
    if (config == null) {
      throw new OperationException("ConfigService.updateApiConfig", "该api不存在");
    }
    return baseRepository.updateTable(apiConfig);
  }

  public List<ApiConfig> findAllApi(ApiConfig apiConfig) {
    return baseRepository.findAll(apiConfig);
  }

  public ApiConfig findByApiId(Long id) {
    return (ApiConfig) baseRepository.findByTableId(ApiConfig.builder().id(id).build());
  }

  public boolean deleteApiConfig(Long id) {
    final List<SqlConfig> sqlConfigs = baseRepository
        .findAll(SqlConfig.builder().apiId(id).build());
    if (CollectionUtils.isEmpty(sqlConfigs)) {
      final ApiConfig apiConfig = this.findByApiId(id);
      if (apiConfig == null) {
        throw new OperationException("ConfigService.deleteApiConfig", "该api不存在");
      }
      return baseRepository.deleteTable(ApiConfig.builder().id(id).build());
    } else {
      throw new OperationException("ConfigService.deleteApiConfig", "该api下存在sql，无法删除");
    }
  }

  public boolean addSqlConfig(SqlConfig sqlConfig) {
    final ApiConfig apiConfig = this.findByApiId(sqlConfig.getApiId());
    if(ApiType.BASE.equals(apiConfig.getType())){
      final List<SqlConfig> sqlConfigs = this.findAllSql(SqlConfig.builder().apiId(apiConfig.getId()).build());
      if(!CollectionUtils.isEmpty(sqlConfigs)){
       throw new OperationException("ConfigService.addSqlConfig", "base风格api下只能有一个sql");
      }
    }
    return baseRepository.insertTable(sqlConfig);
  }

  public boolean updateSqlConfig(SqlConfig sqlConfig) {
    final SqlConfig config = this.findBySqlId(sqlConfig.getId());
    if (config == null) {
      throw new OperationException("ConfigService.updateSqlConfig", "该sql不存在");
    }
    return baseRepository.updateTable(sqlConfig);
  }

  public SqlConfig findBySqlId(Long id) {
    return (SqlConfig) baseRepository.findByTableId(SqlConfig.builder().id(id).build());
  }

  public List<SqlConfig> findAllSql(SqlConfig sqlConfig) {
    return baseRepository.findAll(sqlConfig);
  }

  public boolean deleteSqlConfig(Long id) {
    final SqlConfig sqlConfig = this.findBySqlId(id);
    if (sqlConfig == null) {
      throw new OperationException("ConfigService.deleteSqlConfig", "该sql不存在");
    }
    return baseRepository.deleteTable(SqlConfig.builder().id(id).build());
  }

  public PagedResultDTO<SqlConfig> findSqlConfigByPage(SqlConfig sqlConfig, Pageable pageable,Sortable sortable) {
    return baseRepository.findByPage(sqlConfig, pageable,sortable);
  }

}
