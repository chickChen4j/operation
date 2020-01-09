package com.efivestar.operation.mapping.entity;

import com.alibaba.fastjson.JSON;
import com.efivestar.operation.dto.Pageable;
import com.efivestar.operation.dto.PagedResultDTO;
import com.efivestar.operation.dto.Sortable;
import com.efivestar.operation.mapping.Api;
import com.efivestar.operation.mapping.enums.ApiType;
import com.efivestar.operation.util.StringUtils;
import com.efivestar.operation.util.dynamic.Bindings;
import com.efivestar.operation.util.dynamic.SqlMeta;
import com.efivestar.operation.util.dynamic.SqlTemplate;
import com.efivestar.operation.util.dynamic.SqlTemplateEngin;
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
public class ApiConfig implements BaseMethod {

  public static String tableName = "api_config";

  /**
   * 主键
   */
  private Long id;

  /**
   * 名称(可唯一标注API，相当于xml配置中的id)
   */
  private String name;

  /**
   * 分组ID
   */
  private String groupId;

  /**
   * base类型的api配置的访问路径
   */
  private String url;

  /**
   * 类型 分为restful和base
   */
  private ApiType type;

  /**
   * restful类型时操作的bean类型
   */
  private String bean;

  /**
   * bean的主键名
   */
  private String primaryKey;

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
    ApiConfig.tableName = tableName;
  }

  @Override
  public String getTableName() {
    return ApiConfig.tableName;
  }

  public static boolean initalTable(JdbcTemplate jdbcTemplate) {
    final StringBuilder sb = new StringBuilder();
    sb.append("CREATE TABLE `" + tableName + "` (");
    sb.append(" `id` bigint(20) NOT NULL AUTO_INCREMENT,");
    sb.append(" `name` varchar(150) NOT NULL UNIQUE,");
    sb.append(" `group_id` varchar(255) DEFAULT NULL,");
    sb.append(" `url` varchar(255) DEFAULT NULL,");
    sb.append(" `type` varchar(255) DEFAULT NULL,");
    sb.append(" `bean` varchar(255) DEFAULT NULL,");
    sb.append(" `primary_key` varchar(255) DEFAULT 'id',");
    sb.append(" `create_time` datetime DEFAULT NULL,");
    sb.append(" `created_by` varchar(255) DEFAULT NULL,");
    sb.append(" `last_modified_time` datetime DEFAULT NULL,");
    sb.append(" `last_modified_by` varchar(255) DEFAULT NULL,");
    sb.append(" PRIMARY KEY (`id`),");
    sb.append(" UNIQUE KEY `INDEX_API_NAME` (`name`)");
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
    insertSql.append("<if test=\"name!=null and name!=''\">name,</if>");
    insertSql.append("<if test=\"groupId!=null and groupId!=''\">group_id,</if>");
    insertSql.append("<if test=\"url!=null and url!=''\">url,</if>");
    insertSql.append("<if test=\"type!=null\">type,</if>");
    insertSql.append("<if test=\"bean!=null\">bean,</if>");
    insertSql.append("<if test=\"primaryKey!=null\">primary_key,</if>");
    insertSql.append("<if test=\"createTime!=null\">create_time,</if>");
    insertSql.append("<if test=\"createdBy!=null and createdBy!=''\">created_by,</if>");
    insertSql.append("<if test=\"lastModifiedTime!=null\">last_modified_time,</if>");
    insertSql
        .append("<if test=\"lastModifiedBy!=null and lastModifiedBy!=''\">last_modified_by,</if>");
    insertSql.append("</trim>");
    insertSql.append(" values");
    insertSql.append(" <trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
    insertSql.append("<if test=\"name!=null\">#{name},</if>");
    insertSql.append("<if test=\"groupId!=null and groupId!=''\">#{groupId},</if>");
    insertSql.append("<if test=\"url!=null and url!=''\">#{url},</if>");
    insertSql.append("<if test=\"type!=null\">#{type},</if>");
    insertSql.append("<if test=\"bean!=null\">#{bean},</if>");
    insertSql.append("<if test=\"primaryKey!=null\">#{primaryKey},</if>");
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
    bindings.bind("name", name);
    bindings.bind("groupId", groupId);
    bindings.bind("url", url);
    bindings.bind("type", type != null ? type.name() : null);
    bindings.bind("bean", bean);
    bindings.bind("primaryKey", primaryKey);
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
//    ApiConfig apiConfig = (ApiConfig) this.findById(jdbcTemplate);
//    if (apiConfig == null) {
//      return false;
//    }
    final StringBuffer updateSql = new StringBuffer();
    updateSql.append("update `" + tableName + "`");
    updateSql.append("<set>");
    updateSql.append("<if test=\"name!=null and name!=''\">name = #{name},</if>");
    updateSql.append("<if test=\"groupId!=null and groupId!=''\">group_id = #{groupId},</if>");
    updateSql.append("<if test=\"url!=null and url!=''\">url = #{url},</if>");
    updateSql.append("<if test=\"type!=null\">type = #{type},</if>");
    updateSql.append("<if test=\"bean!=null\">bean = #{bean},</if>");
    updateSql.append("<if test=\"bean==null\">bean = null,</if>");
    updateSql.append(
        "<if test=\"primaryKey!=null and primaryKey!=''\">primary_key = #{primaryKey},</if>");
    updateSql.append(
        "<if test=\"lastModifiedTime!=null\">last_modified_time = #{lastModifiedTime},</if>");
    updateSql.append(
        "<if test=\"lastModifiedBy!=null and lastModifiedBy!=''\">last_modified_by = #{lastModifiedBy},</if>");
    updateSql.append("</set>");
    updateSql.append("where id =#{id}");
    SqlTemplateEngin sqlTemplateEngin = new SqlTemplateEngin();
    SqlTemplate sqlTemplate = sqlTemplateEngin.getSqlTemplate(updateSql.toString());
    Bindings bindings = new Bindings();
    bindings.bind("name", name);
    bindings.bind("groupId", groupId);
    bindings.bind("url", url);
    bindings.bind("type", type != null ? type.name() : null);
    bindings.bind("bean", bean);
    bindings.bind("primaryKey", primaryKey);
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
//    ApiConfig apiConfig = (ApiConfig) this.findById(jdbcTemplate);
//    if (apiConfig == null) {
//      return false;
//    }
    final String deleteSql = "DELETE FROM " + tableName + " WHERE id=?";
    Object[] args = new Object[]{id};
    return jdbcTemplate.update(deleteSql, args) > 0;
  }

  @Override
  public BaseMethod findById(JdbcTemplate jdbcTemplate) {
    String selectSql = "SELECT * FROM " + tableName + " WHERE ";
    final Object[] args = new Object[1];
    if (!StringUtils.isEmpty(name)) {
      selectSql += "name = ?";
      args[0] = name;
    } else {
      selectSql += "id=?";
      args[0] = id;
    }
    if (args[0] == null) {
      return null;
    }
    ApiConfig apiConfig = null;
    try {
      apiConfig = (ApiConfig) jdbcTemplate
          .queryForObject(selectSql, args, new BeanPropertyRowMapper(ApiConfig.class));
    } catch (EmptyResultDataAccessException e) {
      return null;
    }
    return apiConfig;
  }

  @Override
  public List<BaseMethod> findAll(JdbcTemplate jdbcTemplate) {
    final StringBuilder selectSql = initSelectSql();
    SqlTemplateEngin sqlTemplateEngin = new SqlTemplateEngin();
    SqlTemplate sqlTemplate = sqlTemplateEngin.getSqlTemplate(selectSql.toString());
    Bindings bindings = new Bindings();
    bindings.bind("id", id);
    bindings.bind("name", name);
    bindings.bind("groupId", groupId);
    bindings.bind("url", url);
    bindings.bind("type", type != null ? type.name() : null);
    bindings.bind("bean", bean);
    bindings.bind("primaryKey", primaryKey);
    bindings.bind("createTime", createTime);
    SqlMeta sqlMeta = sqlTemplate.process(bindings);
    String sql = sqlMeta.getSql();
    Object[] params = sqlMeta.getParameter().toArray(new Object[sqlMeta.getParameter().size()]);
    List<Map<String, Object>> apis = jdbcTemplate
        .queryForList(sql, params);
    List<BaseMethod> apiConfigs = apis.stream()
        .map(item -> JSON.parseObject(JSON.toJSONString(item), ApiConfig.class))
        .collect(Collectors
            .toList());
    return apiConfigs;
  }

  @Override
  public PagedResultDTO<ApiConfig> findByPage(JdbcTemplate jdbcTemplate, Pageable pageable,Sortable sortable) {
    final int number = pageable.getNumber();
    final int size = pageable.getSize();
    final StringBuilder findByPageSql = initSelectSql();
    findByPageSql.append("<if test='sortName!=null'>order by ${sortName} ${sortOrder}</if>");
    findByPageSql.append("limit #{number},#{size}");
    SqlTemplateEngin sqlTemplateEngin = new SqlTemplateEngin();
    SqlTemplate sqlTemplate = sqlTemplateEngin.getSqlTemplate(findByPageSql.toString());
    Bindings bindings = new Bindings();
    bindings.bind("id", id);
    bindings.bind("name", name);
    bindings.bind("groupId", groupId);
    bindings.bind("url", url);
    bindings.bind("type", type != null ? type.name() : null);
    bindings.bind("bean", bean);
    bindings.bind("primaryKey", primaryKey);
    bindings.bind("createTime", createTime);
    bindings.bind("sortName",sortable.getSortName());
    bindings.bind("sortOrder",sortable.getSortOrder());
    bindings
        .bind("number", number <= 0 ? 0 : number * size);
    bindings.bind("size", size);
    SqlMeta sqlMeta = sqlTemplate.process(bindings);
    String sql = sqlMeta.getSql();
    Object[] params = sqlMeta.getParameter().toArray(new Object[sqlMeta.getParameter().size()]);
    List<Map<String, Object>> apis = jdbcTemplate
        .queryForList(sql, params);
    List<ApiConfig> apiConfigs = apis.stream()
        .map(item -> JSON.parseObject(JSON.toJSONString(item), ApiConfig.class))
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
    return PagedResultDTO.<ApiConfig>builder()
        .content(apiConfigs)
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
    selectSql.append("SELECT id,name,group_id as groupId,url,type,");
    selectSql.append("bean,primary_key as primaryKey,create_time as createTime,");
    selectSql.append("created_by as createdBy,last_modified_time as lastModifiedTime,");
    selectSql.append("last_modified_by as lastModifiedBy FROM " + tableName + "");
    selectSql.append("<where>");
    selectSql.append("<if test=\"id!=null\">id=#{id}</if>");
    selectSql.append("<if test=\"name!=null and name!=''\">AND name = #{name}</if>");
    selectSql.append("<if test=\"groupId!=null and groupId!=''\">AND group_id like concat('%',#{groupId},'%')</if>");
    selectSql.append("<if test=\"url!=null and url!=''\">AND url=#{url}</if>");
    selectSql.append("<if test=\"type!=null\">AND type=#{type}</if>");
    selectSql.append("<if test=\"bean!=null\">AND bean like concat('%',#{bean},'%')</if>");
    selectSql.append("<if test=\"primaryKey!=null\">AND primary_key=#{primaryKey}</if>");
    selectSql.append("<if test=\"createTime!=null\">AND create_time=#{createTime}</if>");
    selectSql.append("</where>");
    return selectSql;
  }

  public Api convertConfigToApi() {
    final Api api = new Api();
    api.setId(this.name);
    if (!StringUtils.isEmpty(this.url)) {
      api.setUrl(this.url.startsWith("/") ? this.url : "/" + this.url);
    }
    if (!StringUtils.isEmpty(this.bean)) {
      api.setBean(this.bean);
    }
    if (StringUtils.isEmpty(this.groupId)) {
      api.setGroupId(this.groupId);
    } else {
      api.setGroupId(this.groupId);
    }
    if (this.type == null) {
      api.setType(ApiType.BASE);
    } else {
      api.setType(this.type);
    }
    if (StringUtils.isEmpty(this.primaryKey)) {
      api.setPrimaryKey("id");
    } else {
      api.setPrimaryKey(this.primaryKey);
    }
    return api;
  }


}
