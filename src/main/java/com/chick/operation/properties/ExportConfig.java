package com.chick.operation.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties("operation.export")
@Configuration
@Data
public class ExportConfig {

  /**
   * 下载中心地址
   */
  private String url;

  /**
   * 每个sheet页最大行数
   */
  private Integer maxRow = 5000;

  /**
   * 每次请求的行数
   */
  private Integer requestCount = 100;

  /**
   * 超时时长(s)
   */
  private Integer overtime = 1800;

  /**
   * 最大重试次数
   */
  private Integer retry;

  /**
   * 超期天数
   */
  private Integer overdueDays = 10;
}
