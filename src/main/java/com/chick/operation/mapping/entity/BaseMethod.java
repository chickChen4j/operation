package com.chick.operation.mapping.entity;


import com.chick.operation.dto.Pageable;
import com.chick.operation.dto.PagedResultDTO;
import com.chick.operation.dto.Sortable;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;

public interface BaseMethod {

  void setTableName(String tableName);

  String getTableName();

  boolean createTable(JdbcTemplate jdbcTemplate);

  boolean insert(JdbcTemplate jdbcTemplate);

  boolean update(JdbcTemplate jdbcTemplate);

  boolean delete(JdbcTemplate jdbcTemplate);

  BaseMethod findById(JdbcTemplate jdbcTemplate);

  List<BaseMethod> findAll(JdbcTemplate jdbcTemplate);

  PagedResultDTO findByPage(JdbcTemplate jdbcTemplate,Pageable pageable,Sortable sortable);

}
