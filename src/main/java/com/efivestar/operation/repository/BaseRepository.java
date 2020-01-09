package com.efivestar.operation.repository;

import com.alibaba.fastjson.JSONObject;
import com.efivestar.operation.dto.Pageable;
import com.efivestar.operation.dto.PagedResultDTO;
import com.efivestar.operation.dto.Sortable;
import com.efivestar.operation.exception.OperationException;
import com.efivestar.operation.mapping.Api;
import com.efivestar.operation.mapping.InParameter;
import com.efivestar.operation.mapping.entity.ApiConfig;
import com.efivestar.operation.mapping.entity.BaseMethod;
import com.efivestar.operation.mapping.entity.ExportTask;
import com.efivestar.operation.mapping.entity.Record;
import com.efivestar.operation.mapping.Sql;
import com.efivestar.operation.mapping.XmlConfiguration;
import com.efivestar.operation.mapping.entity.SqlConfig;
import com.efivestar.operation.mapping.enums.SelectType;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Repository
@Slf4j
public class BaseRepository<T> implements InitializingBean, ApplicationContextAware {

  private DataSource dataSource;

  private JdbcTemplate jdbcTemplate;

  private JdbcTemplateProxyFactory<T> jdbcTemplateProxyFactory;

  private static String tablePrefix;

  @Value("${operation.table.prefix:operation}")
  public void setRecordTableName(String recordTableName) {
    BaseRepository.tablePrefix = recordTableName;
  }

  public int insert(String apiName, Record record, T t) {
    return ((RestfulCrudTemplate<T>) jdbcTemplateProxyFactory
        .newInstance(apiName, record)).insert(convertParamToKV(t));
  }

  public int delete(String apiName, Record record, Long id) {
    return ((RestfulCrudTemplate<T>) jdbcTemplateProxyFactory
        .newInstance(apiName, record)).delete(convertIdToKV(id));
  }

  public int update(String apiName, Record record, T t) {
    return ((RestfulCrudTemplate<T>) jdbcTemplateProxyFactory
        .newInstance(apiName, record)).update(convertParamToKV(t));
  }

  public T selectOne(String apiName, Record record, Long id) {
    final RestfulCrudTemplate<T> crudTemplate = (RestfulCrudTemplate<T>) jdbcTemplateProxyFactory
        .newInstance(apiName, record);
    return crudTemplate.selectOne(convertIdToKV(id));
  }

  public List<T> selectAll(String apiName, Record record, T t) {
    return ((RestfulCrudTemplate<T>) jdbcTemplateProxyFactory
        .newInstance(apiName, record)).selectAll(convertParamToKV(t));
  }

  public PagedResultDTO<T> selectByPage(String apiName, Record record, T t, Pageable pageable,
      Sortable sortable) {
    return ((RestfulCrudTemplate<T>) jdbcTemplateProxyFactory.newInstance(apiName, record))
        .selectByPage(convertPageAndParamToKV(t, pageable, sortable));
  }

  public Object execute(String apiName, Record record, JSONObject jsonObject) {
    final Api api = XmlConfiguration.loadApi(apiName);
    final List<Sql> sqls = api.getSqls();
    if (CollectionUtils.isEmpty(sqls)) {
      throw new OperationException("BaseService.execute", "该api未配置sql");
    }
    final Sql sql = sqls.get(0);
    if (null == sql) {
      throw new OperationException("BaseService.execute", "该api下sql为空");
    }
    final SelectType type = sql.getSelectType();
    if (SelectType.PAGE.equals(type)) {
      handleParams(jsonObject);
    }
    final Object[] args = new Object[1];
    args[0] = convertJsonToKV(jsonObject);
    return sql.execute(jdbcTemplate, record, args);
  }

  public Object execute(Sql sql, Record record, JSONObject jsonObject) {
    final Object[] args = new Object[1];
    args[0] = convertJsonToKV(jsonObject);
    return sql.execute(jdbcTemplate, record, args);
  }

  public void handleParams(JSONObject jsonObject) {
    final Pageable pageable = Pageable.builder().build();
    if (jsonObject.containsKey(Pageable.NUMBER)) {
      pageable.setNumber(jsonObject.getInteger(Pageable.NUMBER));
    }
    if (jsonObject.containsKey(Pageable.SIZE)) {
      pageable.setSize(jsonObject.getInteger(Pageable.SIZE));
    }
    jsonObject.put(Pageable.SIZE, pageable.getSize());
    jsonObject.put(Pageable.NUMBER,
        pageable.getNumber() <= 0 ? 0 : pageable.getSize() * pageable.getNumber());
  }


  public static boolean isExistTable(String tableName, JdbcTemplate jt) throws SQLException {
    Connection conn = jt.getDataSource().getConnection();
    ResultSet rs = null;
    try {
      DatabaseMetaData dbMetaData = conn.getMetaData();
      String[] types = {"TABLE"};
      rs = dbMetaData.getTables(null, null, tableName, types);
      if (rs.next()) {
        return Boolean.TRUE;
      }

    } catch (SQLException e) {
      e.printStackTrace();
    } finally {
      rs.close();
      conn.close();
    }
    return Boolean.FALSE;
  }


  protected InParameter[] convertJsonToKV(JSONObject jsonObject) {
    List<InParameter> inParameters = new ArrayList<>();
    jsonObject.forEach((k, v) -> {
      inParameters.add(InParameter.builder().name(k).value(v).javaType(v.getClass()).build());
    });
    return inParameters.toArray(new InParameter[inParameters.size()]);
  }

  protected InParameter[] convertIdToKV(Long id) {
    InParameter[] inParameters = new InParameter[1];
    inParameters[0] = InParameter.builder().name("id").value(id).javaType(Long.class).build();
    return inParameters;
  }

  protected InParameter[] convertPageAndParamToKV(T t, Pageable pageable, Sortable sortable) {
    InParameter[] inParameters = convertParamToKV(t);
    inParameters = Arrays.copyOf(inParameters, inParameters.length + 4);
    inParameters[inParameters.length - 4] = InParameter.builder().name(Sortable.SORT_ORDER)
        .value(sortable.getSortOrder()).build();
    inParameters[inParameters.length - 3] = InParameter.builder().name(Sortable.SORT_NAME)
        .value(sortable.getSortName()).build();
    inParameters[inParameters.length - 2] = InParameter.builder().name(Pageable.SIZE)
        .value(pageable.getSize()).build();
    inParameters[inParameters.length - 1] = InParameter.builder().name(Pageable.NUMBER)
        .value(pageable.getNumber() <= 0 ? 0 : pageable.getNumber() * pageable.getSize()).build();
    return inParameters;
  }

  protected InParameter[] convertParamToKV(T t) {
    List<InParameter> inParameters = new ArrayList<>();
    Class tClass = t.getClass();
    Field[] fields = tClass.getDeclaredFields();
    try {
      for (Field field : fields) {
        field.setAccessible(Boolean.TRUE);
        InParameter inParameter = InParameter
            .builder()
            .name(field.getName())
            .javaType(field.getType())
            .value(field.get(t))
            .build();
        inParameters.add(inParameter);
      }
    } catch (Exception e) {
      throw new OperationException("BaseRepository.convertParamToKV", "入参转化错误");
    }
    return inParameters.toArray(new InParameter[inParameters.size()]);
  }


  @Override
  public void afterPropertiesSet() throws Exception {
    this.jdbcTemplate = new JdbcTemplate(this.dataSource);
    jdbcTemplateProxyFactory = new JdbcTemplateProxyFactory(
        RestfulCrudTemplate.class, jdbcTemplate);
    this.createTable(initTableName(Record.tableName), new Record());
    this.createTable(initTableName(ExportTask.tableName), new ExportTask());
    this.createTable(initTableName(ApiConfig.tableName), new ApiConfig());
    this.createTable(initTableName(SqlConfig.tableName), new SqlConfig());
    this.initConfigFromDB();
  }

  private String initTableName(String tableName) {
    return tablePrefix.concat("_").concat(tableName);
  }

  public void createTable(String tableName, BaseMethod baseMethod) {
    if (StringUtils.isEmpty(baseMethod.getTableName()) || !baseMethod.getTableName()
        .startsWith(tablePrefix)) {
      baseMethod.setTableName(tableName);
    }
    try {
      boolean flag = isExistTable(tableName, this.jdbcTemplate);
      if (!flag) {
        baseMethod.createTable(this.jdbcTemplate);
      }
    } catch (Exception e) {
      log.warn("运维管理平台创建{}表失败，原因为:{}", new Object[]{tableName, e.getMessage()});
    }
  }

  public boolean insertTable(BaseMethod baseMethod) {

    return baseMethod.insert(jdbcTemplate);
  }

  public boolean updateTable(BaseMethod baseMethod) {
    return baseMethod.update(jdbcTemplate);
  }

  public BaseMethod findByTableId(BaseMethod baseMethod) {
    return baseMethod.findById(jdbcTemplate);
  }

  public List<BaseMethod> findAll(BaseMethod baseMethod) {
    return baseMethod.findAll(jdbcTemplate);
  }

  public PagedResultDTO<BaseMethod> findByPage(BaseMethod baseMethod, Pageable pageable,
      Sortable sortable) {
    return baseMethod.findByPage(jdbcTemplate, pageable, sortable);
  }

  public boolean deleteTable(BaseMethod baseMethod) {
    return baseMethod.delete(jdbcTemplate);
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.dataSource = applicationContext.getBean(DataSource.class);
  }

  public void initConfigFromDB() {
    List<BaseMethod> apiConfigs = findAll(ApiConfig.builder().build());
    if (!CollectionUtils.isEmpty(apiConfigs)) {
      apiConfigs.stream().forEach(item -> {
        ApiConfig apiConfig = (ApiConfig) item;
        Api api = apiConfig.convertConfigToApi();
        List<BaseMethod> sqlConfigs = findAll(SqlConfig.builder().apiId(apiConfig.getId()).build());

        if (!CollectionUtils.isEmpty(sqlConfigs)) {
          List<Sql> sqls = sqlConfigs.stream().map(sql -> ((SqlConfig) sql).convertConfigToSql(api))
              .collect(Collectors.toList());
          api.setSqls(sqls);
        }
        XmlConfiguration.registApi(api.getId(), api);
      });
    }
  }

  public void refreshConfigFromDB() {
    //此方法会先清空内存，再从数据库拉数据到内存，这样做会导致无法从其他地方读取配置
    XmlConfiguration.emptyApiMap();
    XmlConfiguration.readXmlToMemory();
    this.initConfigFromDB();
    jdbcTemplateProxyFactory.cleanMethodCache();
  }

}
