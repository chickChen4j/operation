package com.efivestar.operation.mapping.entity;

import com.alibaba.fastjson.JSON;
import com.efivestar.operation.dto.Pageable;
import com.efivestar.operation.dto.PagedResultDTO;
import com.efivestar.operation.dto.Sortable;
import com.efivestar.operation.mapping.Api;
import com.efivestar.operation.mapping.Sql;
import com.efivestar.operation.mapping.enums.SelectType;
import com.efivestar.operation.mapping.enums.SqlType;
import com.efivestar.operation.util.StringUtils;
import com.efivestar.operation.util.dynamic.Bindings;
import com.efivestar.operation.util.dynamic.SqlMeta;
import com.efivestar.operation.util.dynamic.SqlTemplate;
import com.efivestar.operation.util.dynamic.SqlTemplateEngin;
import com.efivestar.operation.util.xml.XmlParser;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.util.Arrays;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

@Builder
@Data
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class SqlConfig implements BaseMethod {

  public static String tableName = "sql_config";

  /**
   * 主键
   */
  private Long id;

  /**
   * 类型(insert,update,delete,select四种)
   */
  private SqlType type;

  /**
   * 具体的sql语句
   */
  private String value;


  /**
   * 参数类型
   */
  private String parameterType;


  /**
   * 查询的类型(one,list,page三种)
   */
  private SelectType selectType;

  /**
   * 返回值类型
   */
  private String resultType;

  /**
   * 外键(api与sql是一对多关系)
   */
  private Long apiId;

  private String apiName;

  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private Date createTime;

  private String createdBy;

  @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
  private Date lastModifiedTime;

  private String lastModifiedBy;

  @Override
  public void setTableName(String tableName) {
    SqlConfig.tableName = tableName;
  }

  @Override
  public String getTableName() {
    return tableName;
  }

  public static boolean initalTable(JdbcTemplate jdbcTemplate) {
    final String fk = "FK_" + tableName.toUpperCase() + "_API_ID";
    final StringBuilder sb = new StringBuilder();
    sb.append("CREATE TABLE `" + tableName + "` (");
    sb.append(" `id` bigint(20) NOT NULL AUTO_INCREMENT,");
    sb.append(" `type` varchar(255) DEFAULT NULL,");
    sb.append(" `value`longtext DEFAULT NULL,");
    sb.append(" `parameter_type` varchar(255) DEFAULT NULL,");
    sb.append(" `select_type` varchar(255) DEFAULT NULL,");
    sb.append(" `result_type` varchar(255) DEFAULT NULL,");
    sb.append(" `api_id` bigint(20) DEFAULT NULL,");
    sb.append(" `create_time` datetime DEFAULT NULL,");
    sb.append(" `created_by` varchar(255) DEFAULT NULL,");
    sb.append(" `last_modified_time` datetime DEFAULT NULL,");
    sb.append(" `last_modified_by` varchar(255) DEFAULT NULL,");
    sb.append(" PRIMARY KEY (`id`),");
    sb.append(" KEY `" + fk + "` (`api_id`),");
    sb.append(
        " CONSTRAINT `" + fk + "` FOREIGN KEY (`api_id`) REFERENCES `operation_api_config` (`id`)");
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
  public boolean createTable(JdbcTemplate jdbcTemplate) {
    return initalTable(jdbcTemplate);
  }

  @Override
  public boolean insert(JdbcTemplate jdbcTemplate) {
    final StringBuilder insertSql = new StringBuilder();
    insertSql.append("insert into `" + tableName + "` ");
    insertSql.append(" <trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
    insertSql.append("<if test=\"type!=null\">type,</if>");
    insertSql.append("<if test=\"value!=null and value!=''\">value,</if>");
    insertSql.append("<if test=\"parameterType!=null and parameterType!=''\">parameter_type,</if>");
    insertSql.append("<if test=\"selectType!=null\">select_type,</if>");
    insertSql.append("<if test=\"resultType!=null and resultType!=''\">result_type,</if>");
    insertSql.append("<if test=\"apiId!=null\">api_id,</if>");
    insertSql.append("<if test=\"createTime!=null\">create_time,</if>");
    insertSql.append("<if test=\"createdBy!=null and createdBy!=''\">created_by,</if>");
    insertSql.append("<if test=\"lastModifiedTime!=null\">last_modified_time,</if>");
    insertSql
        .append("<if test=\"lastModifiedBy!=null and lastModifiedBy!=''\">last_modified_by,</if>");
    insertSql.append("</trim>");
    insertSql.append(" values");
    insertSql.append(" <trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
    insertSql.append("<if test=\"type!=null\">#{type},</if>");
    insertSql.append("<if test=\"value!=null and value!=''\">#{value},</if>");
    insertSql
        .append("<if test=\"parameterType!=null and parameterType!=''\">#{parameterType},</if>");
    insertSql.append("<if test=\"selectType!=null\">#{selectType},</if>");
    insertSql.append("<if test=\"resultType!=null and resultType!=''\">#{resultType},</if>");
    insertSql.append("<if test=\"apiId!=null\">#{apiId},</if>");
    insertSql.append("<if test=\"createTime!=null\">#{createTime},</if>");
    insertSql.append("<if test=\"createdBy!=null and createdBy!=''\">#{createdBy},</if>");
    insertSql.append(
        "<if test=\"lastModifiedTime!=null and lastModifiedTime!=''\">#{lastModifiedTime},</if>");
    insertSql
        .append("<if test=\"lastModifiedBy!=null and lastModifiedBy!=''\">#{lastModifiedBy},</if>");
    insertSql.append("</trim>");
    SqlTemplateEngin sqlTemplateEngin = new SqlTemplateEngin();
    SqlTemplate sqlTemplate = sqlTemplateEngin.getSqlTemplate(insertSql.toString());
    Bindings bindings = new Bindings();
    bindings.bind("type", type != null ? type.name() : null);
    bindings.bind("value", value);
    bindings.bind("parameterType", parameterType);
    bindings.bind("selectType", selectType != null ? selectType.name() : null);
    bindings.bind("resultType", resultType);
    bindings.bind("apiId", apiId);
    bindings.bind("createTime", createTime);
    bindings.bind("createdBy", createdBy);
    bindings.bind("lastModifiedTime", lastModifiedTime);
    bindings.bind("lastModifiedBy", lastModifiedBy);
    SqlMeta sqlMeta = sqlTemplate.process(bindings);
    String sql = sqlMeta.getSql();
    Object[] params = sqlMeta.getParameter().toArray(new Object[sqlMeta.getParameter().size()]);
    return jdbcTemplate.update(sql, params) > 0;
  }

  @Override
  public boolean update(JdbcTemplate jdbcTemplate) {
//    SqlConfig sqlConfig = (SqlConfig) this.findById(jdbcTemplate);
//    if (sqlConfig == null) {
//      return false;
//    }
    final StringBuffer updateSql = new StringBuffer();
    updateSql.append("update `" + tableName + "`");
    updateSql.append("<set>");
    updateSql.append("<if test=\"type!=null\">type = #{type},</if>");
    updateSql.append("<if test=\"value!=null and value!=''\">value = #{value},</if>");
    updateSql.append(
        "<if test=\"parameterType!=null and parameterType!=''\">parameter_type = #{parameterType},</if>");
    updateSql.append(
        "<if test=\"parameterType==null\">parameter_type = null,</if>");
    updateSql.append("<if test=\"selectType!=null\">select_type = #{selectType},</if>");
    updateSql.append("<if test=\"selectType==null\">select_type = null,</if>");
    updateSql.append(
        "<if test=\"resultType!=null and resultType!=''\">result_type = #{resultType},</if>");
    updateSql.append(
        "<if test=\"resultType==null\">result_type = null,</if>");
    updateSql.append(
        "<if test=\"apiId!=null\">api_id = #{apiId},</if>");
    updateSql.append(
        "<if test=\"lastModifiedTime!=null\">last_modified_time = #{lastModifiedTime},</if>");
    updateSql.append(
        "<if test=\"lastModifiedBy!=null and lastModifiedBy!=''\">last_modified_by = #{lastModifiedBy},</if>");
    updateSql.append("</set>");
    updateSql.append("where id =#{id}");
    SqlTemplateEngin sqlTemplateEngin = new SqlTemplateEngin();
    SqlTemplate sqlTemplate = sqlTemplateEngin.getSqlTemplate(updateSql.toString());
    Bindings bindings = new Bindings();
    bindings.bind("type", type != null ? type.name() : null);
    bindings.bind("value", value);
    bindings.bind("parameterType", parameterType);
    bindings.bind("selectType", selectType != null ? selectType.name() : null);
    bindings.bind("resultType", resultType);
    bindings.bind("apiId", apiId);
    bindings.bind("lastModifiedTime", lastModifiedTime);
    bindings.bind("lastModifiedBy", lastModifiedBy);
    bindings.bind("id", id);
    SqlMeta sqlMeta = sqlTemplate.process(bindings);
    String sql = sqlMeta.getSql();
    Object[] params = sqlMeta.getParameter().toArray(new Object[sqlMeta.getParameter().size()]);
    return jdbcTemplate
        .update(sql, params) > 0;
  }

  @Override
  public boolean delete(JdbcTemplate jdbcTemplate) {
//    SqlConfig sqlConfig = (SqlConfig) this.findById(jdbcTemplate);
//    if (sqlConfig == null) {
//      return false;
//    }
    final String deleteSql = "DELETE FROM " + tableName + " WHERE id=?";
    Object[] args = new Object[]{id};
    return jdbcTemplate.update(deleteSql, args) > 0;
  }

  @Override
  public BaseMethod findById(JdbcTemplate jdbcTemplate) {
    final String selectSql = "SELECT * FROM " + tableName + " WHERE id = ?";
    Object[] args = new Object[]{id};
    SqlConfig sqlConfig = null;
    try {
      sqlConfig = (SqlConfig) jdbcTemplate
          .queryForObject(selectSql, args, new BeanPropertyRowMapper(SqlConfig.class));
    } catch (EmptyResultDataAccessException e) {
      return null;
    }
    return sqlConfig;
  }

  @Override
  public List<BaseMethod> findAll(JdbcTemplate jdbcTemplate) {
    final StringBuilder selectSql = initSelectSql();
    SqlTemplateEngin sqlTemplateEngin = new SqlTemplateEngin();
    SqlTemplate sqlTemplate = sqlTemplateEngin.getSqlTemplate(selectSql.toString());
    Bindings bindings = new Bindings();
    bindings.bind("id", id);
    bindings.bind("type", type != null ? type.name() : null);
    bindings.bind("parameterType", parameterType);
    bindings.bind("selectType", selectType != null ? selectType.name() : null);
    bindings.bind("resultType", resultType);
    bindings.bind("apiId", apiId);
    bindings.bind("createTime", createTime);
    SqlMeta sqlMeta = sqlTemplate.process(bindings);
    String sql = sqlMeta.getSql();
    Object[] params = sqlMeta.getParameter().toArray(new Object[sqlMeta.getParameter().size()]);
    List<Map<String, Object>> sqls = jdbcTemplate
        .queryForList(sql, params);
    List<BaseMethod> sqlConfigs = sqls.stream()
        .map(item -> JSON.parseObject(JSON.toJSONString(item), SqlConfig.class))
        .collect(Collectors
            .toList());
    return sqlConfigs;
  }

  @Override
  public PagedResultDTO findByPage(JdbcTemplate jdbcTemplate, Pageable pageable,
      Sortable sortable) {
    final int number = pageable.getNumber();
    final int size = pageable.getSize();
    final StringBuilder findByPageSql = initSelectSql();
    findByPageSql.append("<if test='sortName!=null'>order by ${sortName} ${sortOrder}</if>");
    findByPageSql.append("limit #{number},#{size}");
    SqlTemplateEngin sqlTemplateEngin = new SqlTemplateEngin();
    SqlTemplate sqlTemplate = sqlTemplateEngin.getSqlTemplate(findByPageSql.toString());
    Bindings bindings = new Bindings();
    bindings.bind("id", id);
    bindings.bind("type", type != null ? type.name() : null);
    bindings.bind("parameterType", parameterType);
    bindings.bind("selectType", selectType != null ? selectType.name() : null);
    bindings.bind("resultType", resultType);
    bindings.bind("apiId", apiId);
    bindings.bind("createTime", createTime);
    bindings.bind("sortName", sortable.getSortName());
    bindings.bind("sortOrder", sortable.getSortOrder());
    bindings
        .bind("number", number <= 0 ? 0 : number * size);
    bindings.bind("size", size);
    SqlMeta sqlMeta = sqlTemplate.process(bindings);
    String sql = sqlMeta.getSql();
    Object[] params = sqlMeta.getParameter().toArray(new Object[sqlMeta.getParameter().size()]);
    List<Map<String, Object>> sqls = jdbcTemplate
        .queryForList(sql, params);
    List<SqlConfig> sqlConfigs = sqls.stream()
        .map(item -> JSON.parseObject(JSON.toJSONString(item), SqlConfig.class))
        .collect(Collectors
            .toList());
    final int limitIndex = sql.lastIndexOf("limit");
    final String rowSql =
        "select count(1) from (" + sql
            .substring(0, limitIndex)
            + ") table_";
    params = Arrays.copyOf(params, params.length - 2);
    Long total = null;
    if (params.length > 0) {
      total = jdbcTemplate.queryForObject(rowSql, params, Long.class);
    } else {
      total = jdbcTemplate.queryForObject(rowSql, Long.class);
    }
    final long totalPage = total % size == 0 ? total / size : total / size + 1;
    return PagedResultDTO.<SqlConfig>builder()
        .content(sqlConfigs)
        .size(pageable.getSize())
        .number(pageable.getNumber())
        .totalElements(total)
        .totalPages(totalPage)
        .first(pageable.getNumber() == 0 ? true : false)
        .last(number + 1 == totalPage)
        .build();
  }

  public StringBuilder initSelectSql() {
    final StringBuilder selectSql = new StringBuilder();
    selectSql
        .append(
            "SELECT s.id,s.type,s.value,s.parameter_type as parameterType,s.select_type as selectType,");
    selectSql.append(
        "s.result_type as resultType,s.api_id as apiId,a.name as apiName,s.create_time as createTime,");
    selectSql.append("s.created_by as createdBy,s.last_modified_time as lastModifiedTime,");
    selectSql.append("s.last_modified_by as lastModifiedBy FROM " + tableName + " s ");
    selectSql.append("LEFT JOIN " + ApiConfig.tableName + " a ");
    selectSql.append("ON s.api_id = a.id");
    selectSql.append("<where>");
    selectSql.append("<if test=\"id!=null\">s.id=#{id}</if>");
    selectSql.append("<if test=\"type!=null\">AND s.type=#{type}</if>");
    selectSql.append(
        "<if test=\"parameterType!=null and parameterType!=''\">AND s.parameter_type like concat('%',#{parameterType},'%')</if>");
    selectSql
        .append(
            "<if test=\"selectType!=null and selectType!=''\">AND s.select_type=#{selectType}</if>");
    selectSql
        .append(
            "<if test=\"resultType!=null and resultType!=''\">AND s.result_type like concat('%',#{resultType},'%')</if>");
    selectSql.append("<if test=\"apiId!=null\">AND s.api_id=#{apiId}</if>");
    selectSql.append("<if test=\"createTime!=null\">AND s.create_time=#{createTime}</if>");
    selectSql.append("</where>");
    return selectSql;
  }

  public Sql convertConfigToSql(Api api) {
    boolean isSelect = SqlType.SELECT.equals(this.type);
    Class bean = api.getBean();
    if (isSelect && this.selectType == null) {
      this.selectType = SelectType.LIST;
    }
    final Sql sql = new Sql();
    sql.setType(this.type);
    sql.setValue(this.value);
    if (!StringUtils.isEmpty(this.parameterType)) {
      sql.setParameterType(this.parameterType);
    } else if (bean != null) {
      sql.setParameterType(api.getBean());
    }
    XmlParser.getParamMapFromSql(sql);
    if (isSelect) {
      sql.setId(this.type.name().toLowerCase() + this.selectType
          .getSuffix());
      sql.setSelectType(this.selectType);
      if (!StringUtils.isEmpty(this.resultType)) {
        sql.setResultType(this.resultType);
      } else if (bean != null && SqlType.SELECT.equals(sql.getType())) {
        sql.setResultType(api.getBean());
      }
      XmlParser.getResultFromSql(sql);
//      XmlParser.getRowMapFromSql(sql);
    } else {
      sql.setId(this.type.name().toLowerCase());
    }
    return sql;
  }
}
