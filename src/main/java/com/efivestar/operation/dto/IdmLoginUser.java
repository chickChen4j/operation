package com.efivestar.operation.dto;


import lombok.ToString;

@ToString
public class IdmLoginUser implements java.io.Serializable {

  private static final long serialVersionUID = 6534726073667446518L;

  private String userId;
  private String password;
  private String systemId;
  private String domain;

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getSystemId() {
    return systemId;
  }

  public void setSystemId(String systemId) {
    this.systemId = systemId;
  }

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }

}
