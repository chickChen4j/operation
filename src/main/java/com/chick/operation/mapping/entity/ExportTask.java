package com.chick.operation.mapping.entity;

import com.alibaba.fastjson.JSON;
import com.chick.operation.dto.Pageable;
import com.chick.operation.dto.PagedResultDTO;
import com.chick.operation.dto.Sortable;
import com.chick.operation.mapping.enums.ExportStatusEnum;
import com.chick.operation.util.StringUtils;
import com.chick.operation.util.dynamic.Bindings;
import com.chick.operation.util.dynamic.SqlMeta;
import com.chick.operation.util.dynamic.SqlTemplate;
import com.chick.operation.util.dynamic.SqlTemplateEngin;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.CollectionUtils;

@Builder
@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class ExportTask implements BaseMethod {

  public static String tableName = "export_task";

  /**
   * 主键
   */
  private Integer id;

  /**
   * 任务id(UUID)
   */
  private String taskId;

  /**
   * 对应apiName
   */
  private String apiName;

//  /**
//   * 导出条件（用于客户显示）
//   */
//  private String exportCondition;

  /**
   * 导出请求参数(json)
   */
  private String payload;

  /**
   * 错误日志
   */
  private String errorLog;

  /**
   * 错误信息（正常结束：值为空；超时/error等记录日志）
   */
  private String message;

//  /**
//   * 错误编码
//   */
//  private String code;

  /**
   * 处理状态
   */
  @Builder.Default
  private ExportStatusEnum exportStatusEnum = ExportStatusEnum.initial;

  /**
   * 进度
   */
  @Builder.Default
  private Double progress = 0D;

  /**
   * 下载url
   */
  private String downloadUrl;

  /**
   * 文件大小(kb)
   */
  @Builder.Default
  private Long fileSize = 0L;

  /**
   * 导出时长(s)
   */
  @Builder.Default
  private Long timeConsume = 0L;

  /**
   * 重试次数
   */
  @Builder.Default
  private Integer retry = 0;

  @Builder.Default
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private Date createTime = new Date();


  public static boolean initalTable(JdbcTemplate jdbcTemplate) {
    final StringBuilder sb = new StringBuilder();
    sb.append("CREATE TABLE `" + tableName + "` (");
    sb.append(" `id` bigint(20) NOT NULL AUTO_INCREMENT,");
    sb.append(" `task_id` varchar(150) DEFAULT NULL,");
    sb.append(" `api_name` varchar(255) DEFAULT NULL,");
//    sb.append(" `export_condition` longtext DEFAULT NULL,");
    sb.append(" `payload` longtext DEFAULT NULL,");
    sb.append(" `error_log` longtext DEFAULT NULL,");
    sb.append(" `message` longtext DEFAULT NULL,");
//    sb.append(" `code` varchar(255) DEFAULT NULL,");
    sb.append(" `export_status_enum` varchar(255) DEFAULT NULL,");
    sb.append(" `progress` double DEFAULT NULL,");
    sb.append(" `download_url` varchar(255) DEFAULT NULL,");
    sb.append(" `file_size` bigint(20) DEFAULT NULL,");
    sb.append(" `time_consume` bigint(20) DEFAULT NULL,");
    sb.append(" `retry` int(11) DEFAULT NULL,");
    sb.append(" `create_time` datetime DEFAULT NULL,");
    sb.append(" PRIMARY KEY (`id`),");
    sb.append(" UNIQUE KEY `INDEX_TASK_ID` (`task_id`)");
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
    ExportTask.tableName = tableName;
  }

  @Override
  public String getTableName() {
    return ExportTask.tableName;
  }

  @Override
  public boolean createTable(JdbcTemplate jdbcTemplate) {
    return ExportTask.initalTable(jdbcTemplate);
  }

  @Override
  public boolean insert(JdbcTemplate jdbcTemplate) {

    final StringBuffer insertSql = new StringBuffer();
    insertSql.append("insert into `" + tableName + "` ");
    insertSql.append(" <trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
    insertSql.append("<if test=\"taskId!=null\">task_id,</if>");
    insertSql.append("<if test=\"apiName!=null and apiName!=''\">api_name,</if>");
    insertSql.append("<if test=\"payload!=null and payload!=''\">payload,</if>");
    insertSql.append("<if test=\"errorLog!=null and errorLog!=''\">error_log,</if>");
    insertSql.append("<if test=\"message!=null and message!=''\">message,</if>");
    insertSql.append("<if test=\"exportStatusEnum!=null\">export_status_enum,</if>");
    insertSql.append("<if test=\"progress!=null\">progress,</if>");
    insertSql.append("<if test=\"downloadUrl!=null and downloadUrl!=''\">download_url,</if>");
    insertSql.append("<if test=\"fileSize!=null\">file_size,</if>");
    insertSql.append("<if test=\"timeConsume!=null\">time_consume,</if>");
    insertSql.append("<if test=\"retry!=null\">retry,</if>");
    insertSql.append("<if test=\"createTime!=null\">create_time,</if>");
    insertSql.append("</trim>");
    insertSql.append(" values");
    insertSql.append(" <trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
    insertSql.append("<if test=\"taskId!=null\">#{taskId},</if>");
    insertSql.append("<if test=\"apiName!=null and apiName!=''\">#{apiName},</if>");
    insertSql.append("<if test=\"payload!=null and payload!=''\">#{payload},</if>");
    insertSql.append("<if test=\"errorLog!=null and errorLog!=''\">#{errorLog},</if>");
    insertSql.append("<if test=\"message!=null and message!=''\">#{message},</if>");
    insertSql.append("<if test=\"exportStatusEnum!=null\">#{exportStatusEnum},</if>");
    insertSql.append("<if test=\"progress!=null\">#{progress},</if>");
    insertSql.append("<if test=\"downloadUrl!=null and downloadUrl!=''\">#{downloadUrl},</if>");
    insertSql.append("<if test=\"fileSize!=null\">#{fileSize},</if>");
    insertSql.append("<if test=\"timeConsume!=null\">#{timeConsume},</if>");
    insertSql.append("<if test=\"retry!=null\">#{retry},</if>");
    insertSql.append("<if test=\"createTime!=null\">#{createTime},</if>");
    insertSql.append("</trim>");
    SqlTemplateEngin sqlTemplateEngin = new SqlTemplateEngin();
    SqlTemplate sqlTemplate = sqlTemplateEngin.getSqlTemplate(insertSql.toString());
    Bindings bindings = new Bindings();
    bindings.bind("taskId", taskId);
    bindings.bind("apiName", apiName);
    bindings.bind("payload", payload);
    bindings.bind("errorLog", errorLog);
    bindings.bind("message", message);
    bindings.bind("exportStatusEnum", exportStatusEnum.toString());
    bindings.bind("progress", progress);
    bindings.bind("downloadUrl", downloadUrl);
    bindings.bind("fileSize", fileSize);
    bindings.bind("timeConsume", timeConsume);
    bindings.bind("retry", retry);
    bindings.bind("createTime", createTime);
    SqlMeta sqlMeta = sqlTemplate.process(bindings);
    String sql = sqlMeta.getSql();
    Object[] params = sqlMeta.getParameter().toArray(new Object[sqlMeta.getParameter().size()]);
    return jdbcTemplate.update(sql, params) > 0;
  }

  @Override
  public boolean update(JdbcTemplate jdbcTemplate) {
    ExportTask exportTask = (ExportTask) findById(jdbcTemplate);
    if (exportTask == null) {
      return false;
    }
    final StringBuffer updateSql = new StringBuffer();
    updateSql.append("update `" + tableName + "`");
    updateSql.append("<set>");
    updateSql.append("<if test=\"payload!=null and payload!=''\">payload = #{payload},</if>");
    updateSql.append("<if test=\"errorLog!=null and errorLog!=''\">error_log = #{errorLog},</if>");
    updateSql.append("<if test=\"message!=null and message!=''\">message = #{message},</if>");
    updateSql.append(
        "<if test=\"exportStatusEnum!=null and exportStatusEnum!=''\">export_status_enum = #{exportStatusEnum},</if>");
    updateSql.append("<if test=\"progress!=null\">progress = #{progress},</if>");
    updateSql.append(
        "<if test=\"downloadUrl!=null and downloadUrl!=''\">download_url = #{downloadUrl},</if>");
    updateSql.append("<if test=\"fileSize!=null\">file_size = #{fileSize},</if>");
    updateSql.append("<if test=\"timeConsume!=null\">time_consume = #{timeConsume},</if>");
    updateSql.append("<if test=\"retry!=null\">retry = #{retry},</if>");
    updateSql.append("</set>");
    updateSql.append("where id =#{id}");
    SqlTemplateEngin sqlTemplateEngin = new SqlTemplateEngin();
    SqlTemplate sqlTemplate = sqlTemplateEngin.getSqlTemplate(updateSql.toString());
    Bindings bindings = new Bindings();
    bindings.bind("payload", payload);
    bindings.bind("errorLog", errorLog);
    bindings.bind("message", message);
    bindings.bind("exportStatusEnum", exportStatusEnum.toString());
    bindings.bind("progress", progress);
    bindings.bind("downloadUrl", downloadUrl);
    bindings.bind("fileSize", fileSize);
    bindings.bind("timeConsume", timeConsume);
    bindings.bind("retry", retry);
    bindings.bind("id", id);
    SqlMeta sqlMeta = sqlTemplate.process(bindings);
    String sql = sqlMeta.getSql();
    Object[] params = sqlMeta.getParameter().toArray(new Object[sqlMeta.getParameter().size()]);
    return jdbcTemplate
        .update(sql, params) > 0;
  }

  @Override
  public boolean delete(JdbcTemplate jdbcTemplate) {
    ExportTask exportTask = (ExportTask) findById(jdbcTemplate);
    if (exportTask == null) {
      return false;
    }
    if (!ExportStatusEnum.exported.equals(exportTask.getExportStatusEnum())) {
      return false;
    }
    String deleteSql = "delete from `" + tableName + "` where ";
    final Object[] args = new Object[1];
    if (StringUtils.isEmpty(taskId)) {
      deleteSql += "id=?";
      args[0] = id;
    } else {
      deleteSql += "task_id = ?";
      args[0] = taskId;
    }
    return jdbcTemplate.update(deleteSql, args) > 0;
  }

  @Override
  public BaseMethod findById(JdbcTemplate jdbcTemplate) {
    String selectSql = "select * from `" + tableName + "` where ";
    final Object[] args = new Object[1];
    if (StringUtils.isEmpty(taskId)) {
      selectSql += "id=?";
      args[0] = id;
    } else {
      selectSql += "task_id = ?";
      args[0] = taskId;
    }
    if (args[0] == null) {
      return null;
    }
    ExportTask exportTask = null;
    try {
      exportTask = (ExportTask) jdbcTemplate
          .queryForObject(selectSql, args, new BeanPropertyRowMapper(ExportTask.class));
    } catch (EmptyResultDataAccessException e) {
      return null;
    }
    return exportTask;
  }

  @Override
  public List<BaseMethod> findAll(JdbcTemplate jdbcTemplate) {
    String selectSql = "select id,task_id as taskId,api_name as apiName,payload,"
        + "error_log as errorLog,message,export_status_enum as exportStatusEnum,"
        + "progress,download_url as downloadUrl,file_size as fileSize,time_consume as timeConsume,"
        + "retry,create_time as createTime from `"
        + tableName + "`";
    List<Map<String, Object>> exportTasks = jdbcTemplate.queryForList(selectSql);
    if (CollectionUtils.isEmpty(exportTasks)) {
      return new ArrayList<>();
    }
    return exportTasks.stream()
        .map(item -> JSON.parseObject(JSON.toJSONString(item), ExportTask.class))
        .collect(Collectors
            .toList());
  }

  @Override
  public PagedResultDTO findByPage(JdbcTemplate jdbcTemplate, Pageable pageable,Sortable sortable) {
    return null;
  }
}
