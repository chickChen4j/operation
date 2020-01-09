package com.efivestar.operation.config;

import com.efivestar.operation.properties.AdminConsoleProperties;
import com.efivestar.operation.http.ConsoleViewServlet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@ConditionalOnWebApplication
@Configuration
@Import(AdminConsoleProperties.class)
@Slf4j
public class OperationServletConfiguration {

  private static final String DEFAULT_ALLOW_IP = "127.0.0.1";

  @Bean
  public ServletRegistrationBean operationRegistrationBean(AdminConsoleProperties properties) {
    AdminConsoleProperties.StatViewServlet config = properties.getStatViewServlet();
    ServletRegistrationBean registrationBean = new ServletRegistrationBean();
    ConsoleViewServlet consoleViewServlet = new ConsoleViewServlet();
    registrationBean.setServlet(consoleViewServlet);
    registrationBean.addUrlMappings("/admin-console/*");
    if (config.getAllow() != null) {
      registrationBean.addInitParameter("allow", config.getAllow());
    }
//    else {
//      registrationBean.addInitParameter("allow", DEFAULT_ALLOW_IP);
//    }
    if (config.getDeny() != null) {
      registrationBean.addInitParameter("deny", config.getDeny());
    }
    if (config.getLoginUsername() != null) {
      registrationBean.addInitParameter("loginUsername", config.getLoginUsername());
    }
    if (config.getLoginPassword() != null) {
      registrationBean.addInitParameter("loginPassword", config.getLoginPassword());
    }
    if (config.getResetEnable() != null) {
      registrationBean.addInitParameter("resetEnable", config.getResetEnable());
    }
    return registrationBean;
  }


}
