package com.efivestar.operation.mapping;

import com.efivestar.operation.util.StringUtils;
import com.efivestar.operation.util.xml.Resources;
import com.efivestar.operation.util.xml.XmlParser;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
@Slf4j
public class XmlConfiguration implements InitializingBean {

  @Getter
  private static ConcurrentHashMap<String, Api> apiMap = new ConcurrentHashMap<>();

  private static String XML_CONFIGURATION_NAME;


  private static final String XML = ".xml";

  private static String CLIENT_XML;

  @Value("${operation.xml.name:operation}")
  public void setXmlConfigurationName(String xmlConfigurationName) {
    XML_CONFIGURATION_NAME = xmlConfigurationName;
    CLIENT_XML = XML_CONFIGURATION_NAME.concat(XML);
  }

  private static SAXReader reader = new SAXReader();

  public static Api registApi(String apiName, Api api) {
    return apiMap.put(apiName, api);
  }

  public static void removeApi(String apiName) {
    apiMap.remove(apiName);
  }

  public static void emptyApiMap() {
    apiMap.clear();
  }

  public static Api loadApi(String apiName) {
    return apiMap.get(apiName);
  }

  public static Document readXml(String name) throws IOException, DocumentException {
    InputStream inputStream = Resources.getResourceAsStream(name);
    return reader.read(inputStream);
  }

  public static List<Api> convertDocToList(String name) {
    List<Api> apis = new ArrayList<>();
    try {
      apis = XmlParser.parser(readXml(name));
    } catch (IOException e) {
      log.info("读取xml失败：{},message:{}", new Object[]{e, e.getMessage()});
    } catch (DocumentException e) {
      log.info("读取xml失败：{},message:{}", new Object[]{e, e.getMessage()});
    }
    return apis;
  }

  public static boolean isExistXml(String name) {
    return Resources.isExistResource(name);
  }

  public static boolean existApi(String apiName) {
    return apiMap.containsKey(apiName);
  }

  public static void initXmlToMemory() {
    //TO_DO 将map的key设置为id+groupId
    handleRegistApi(convertDocToList(CLIENT_XML));
  }

  public static void readXmlToMemory() {
    if (isExistXml(CLIENT_XML)) {
      handleRegistApi(convertDocToList(CLIENT_XML));
    }
  }

  public static void handleRegistApi(List<Api> apis) {
    try {
      if (!CollectionUtils.isEmpty(apis)) {
        for (Api api : apis) {
          final String apiId = api.getId();
          if (StringUtils.isEmpty(apiId)) {
            log.warn("API的id不能为空");
            continue;
          }
          registApi(apiId, api);
        }
      }
    } catch (Exception e) {
      log.warn("xml解析错误：{},message:{}", new Object[]{e, e.getMessage()});
    }
  }

  @Override
  public void afterPropertiesSet() {
    initXmlToMemory();
  }
}
