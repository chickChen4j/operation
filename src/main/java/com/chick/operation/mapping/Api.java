package com.chick.operation.mapping;

import com.chick.operation.mapping.enums.ApiType;
import com.chick.operation.mapping.enums.SqlType;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.CollectionUtils;

@EqualsAndHashCode
@ToString
//@Component
@NoArgsConstructor
@ManagedResource(objectName = "bean:name=Api", description = "根据xml解析接口配置的bean")
@Slf4j
public class Api {

  private String id;

  private String groupId;

  private String url;

  private ApiType type;

  private Class<?> bean;

  private List<Sql> sqls;

  private String prefix;

  private String primaryKey;

  /**
   * 判断当前api是不是正在被访问 (因有并发问题，故做废弃处理)
   */
  @Deprecated
  private boolean isUsed;

  @ManagedAttribute(description = "得到api的id")
  public String getId() {
    return id;
  }

  @ManagedAttribute(description = "设置api的id")
  public void setId(String id) {
    this.id = id;
  }

  @ManagedAttribute
  public String getUrl() {
    return url;
  }

  @ManagedAttribute
  public void setUrl(String url) {
    this.url = url;
  }

  @ManagedAttribute
  public ApiType getType() {
    return type;
  }

  @ManagedAttribute()
  public void setType(ApiType type) {
    this.type = type;
  }

  public List<Sql> getSqls() {
    return sqls;
  }

  public void setSqls(List<Sql> sqls) {
    this.sqls = sqls;
  }


  public Class<?> getBean() {
    return bean;
  }

  public void setBean(Class<?> bean) {
    this.bean = bean;
  }

  public String getPrefix() {
    return prefix;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }

  public void setBean(String bean) {
    try {
      this.bean = Class.forName(bean);
    } catch (ClassNotFoundException e) {
      log.warn("设置api的bean类型是出错，原因为:{}", new Object[]{e, e.getMessage()});
    }
  }

  public String getPrimaryKey() {
    return primaryKey;
  }

  public void setPrimaryKey(String primaryKey) {
    this.primaryKey = primaryKey;
  }

  @Deprecated
  public boolean isUsed() {
    return isUsed;
  }

  @Deprecated
  public void setUsed(boolean used) {
    isUsed = used;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public Sql selectSqlIsUsed() {
    if (!CollectionUtils.isEmpty(this.sqls)) {
      final List<Sql> selectSqls = this.sqls.stream()
          .filter(sql -> SqlType.SELECT.equals(sql.getType()))
          .collect(Collectors.toList());
      if (!CollectionUtils.isEmpty(selectSqls)) {
        final Optional<Sql> sql = selectSqls.stream()
            .sorted(Comparator.comparing(Sql::getSelectType).reversed())
            .findFirst();
        if (sql.isPresent()) {
          return sql.get();
        }

      }
    }
    return null;
  }
}
