package com.chick.operation.mapping.entity;

import com.chick.operation.dto.Pageable;
import com.chick.operation.dto.PagedResultDTO;
import com.chick.operation.dto.Sortable;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.jdbc.core.JdbcTemplate;

@Data
@Builder
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class Record implements BaseMethod {

  public static String tableName = "record";

  private Long id;

  private String apiName;

  private String url;

  private String executeSql;

  private String executeArgs;

  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private Date executeTime;

  private String executor;

  private String ip;

  @Builder.Default
  private Boolean executeIsSuccess = Boolean.TRUE;

  @Builder.Default
  private String executeResult = "执行操作成功";


  /**
   * 创建记录表
   */
  public static boolean initalTable(JdbcTemplate jdbcTemplate) {
    final StringBuilder sb = new StringBuilder();
    sb.append("CREATE TABLE `" + tableName + "` (");
    sb.append(" `id` bigint(20) NOT NULL AUTO_INCREMENT,");
    sb.append(" `api_name` varchar(255) DEFAULT NULL,");
    sb.append(" `url` varchar(255) DEFAULT NULL,");
    sb.append(" `execute_sql` longtext DEFAULT NULL,");
    sb.append(" `execute_args` longtext DEFAULT NULL,");
    sb.append(" `execute_time` datetime DEFAULT NULL,");
    sb.append(" `executor` varchar(255) DEFAULT NULL,");
    sb.append(" `ip` varchar(255) DEFAULT NULL,");
    sb.append(" `execute_is_success` bit(1) DEFAULT NULL,");
    sb.append(" `execute_result` longtext DEFAULT NULL,");
    sb.append(" PRIMARY KEY (`id`)");
    sb.append(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;");
    try {
      jdbcTemplate.execute(sb.toString());
      return Boolean.TRUE;
    } catch (Exception e) {
      log.warn("建表失败，表名为:{},失败原因:{}", new Object[]{tableName, e.getMessage()});
    }
    return Boolean.FALSE;
  }

  @Override
  public void setTableName(String tableName) {
    Record.tableName = tableName;
  }

  @Override
  public String getTableName() {
    return Record.tableName;
  }

  @Override
  public boolean createTable(JdbcTemplate jdbcTemplate) {
    return Record.initalTable(jdbcTemplate);
  }

  @Override
  public boolean insert(JdbcTemplate jdbcTemplate) {
    final String inertSql = "insert into `" + tableName
        + "`(api_name,url,execute_sql,execute_time,executor,ip,"
        + "execute_is_success,execute_result,execute_args)values(?,?,?,?,?,?,?,?,?)";
    return jdbcTemplate
        .update(inertSql, apiName, url, executeSql,
            executeTime, executor, ip,
            executeIsSuccess, executeResult, executeArgs) > 0;

  }

  @Override
  public boolean update(JdbcTemplate jdbcTemplate) {
    return false;
  }

  @Override
  public boolean delete(JdbcTemplate jdbcTemplate) {
    return false;
  }

  @Override
  public BaseMethod findById(JdbcTemplate jdbcTemplate) {
    return null;
  }

  @Override
  public List<BaseMethod> findAll(JdbcTemplate jdbcTemplate) {
    return null;
  }

  @Override
  public PagedResultDTO findByPage(JdbcTemplate jdbcTemplate, Pageable pageable,Sortable sortable) {
    return null;
  }
}
