package com.efivestar.operation;

import com.efivestar.operation.aop.HandlerAspect;
import com.efivestar.operation.config.OperationServletConfiguration;
import com.efivestar.operation.controller.ConfigController;
import com.efivestar.operation.controller.ExportController;
import com.efivestar.operation.mapping.XmlConfiguration;
import com.efivestar.operation.properties.AdminConsoleProperties;
import com.efivestar.operation.properties.ExportConfig;
import com.efivestar.operation.repository.BaseRepository;
import com.efivestar.operation.controller.BaseController;
import com.efivestar.operation.controller.RestfulController;
import com.efivestar.operation.service.BaseService;
import com.efivestar.operation.service.ConfigService;
import com.efivestar.operation.service.RestfulService;
import com.efivestar.operation.service.export.DownloadCenterService;
import com.efivestar.operation.service.export.ExportEventListener;
import com.efivestar.operation.service.export.ExportTaskService;
import com.efivestar.operation.service.export.POIService;
import com.efivestar.operation.util.spring.ApplicationContextHelper;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;

@Configuration
@Import(value = {OperationServletConfiguration.class, AdminConsoleProperties.class,
    ExportConfig.class})
public class OperationAutoConfiguration {

//  @Bean("OperationExceptionTranslator")
//  public ExceptionTranslator exceptionTranslator() {
//    return new ExceptionTranslator();
//  }

  @Bean
  public XmlConfiguration xmlConfiguration() {
    return new XmlConfiguration();
  }

  @Bean
  @DependsOn("xmlConfigController")
  public BaseRepository baseRepository() {
    return new BaseRepository();
  }

  @Bean
  public BaseController baseController() {
    return new BaseController();
  }

  @Bean
  public RestfulController restfulController() {
    return new RestfulController();
  }

  @Bean
  public ConfigController xmlConfigController() {
    return new ConfigController();
  }

  @Bean
  public ExportController exportController() {
    return new ExportController();
  }

  @Bean
  public BaseService baseService() {
    return new BaseService();
  }

  @Bean
  public RestfulService restfulService() {
    return new RestfulService();
  }

  @Bean
  @ConditionalOnMissingBean(name = "exportTaskService")
  public ExportTaskService exportTaskService() {
    return new ExportTaskService();
  }

  @Bean
  @ConditionalOnMissingBean(name = "poiService")
  public POIService poiService() {
    return new POIService();
  }

  @Bean
  @ConditionalOnMissingBean(name = "exportEventListener")
  public ExportEventListener exportEventListener() {
    return new ExportEventListener();
  }

  @Bean
  @ConditionalOnMissingBean(name = "downloadCenterService")
  public DownloadCenterService downloadCenterService() {
    return new DownloadCenterService();
  }

  @Bean
  @ConditionalOnMissingBean(ApplicationContextHelper.class)
  public ApplicationContextHelper applicationContextHelper() {
    return new ApplicationContextHelper();
  }

  @Bean
  public ConfigService configService() {
    return new ConfigService();
  }

  @Bean
  public HandlerAspect handlerAspect() {
    return new HandlerAspect();
  }
}
