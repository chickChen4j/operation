package com.efivestar.operation.service.export;


import com.alibaba.fastjson.JSONObject;
import com.efivestar.operation.exception.OperationException;
import com.efivestar.operation.properties.ExportConfig;
import com.efivestar.operation.properties.StaticConfig;
import com.efivestar.operation.repository.BaseRepository;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.net.URL;
import java.nio.charset.Charset;
import org.springframework.util.StringUtils;

@Service
public class DownloadCenterService {

  @Autowired
  BaseRepository baseRepository;

  @Autowired
  private ExportConfig exportConfig;

  @Value("${operation.export.url}")
  private String downloadCenterUrl;


  public JSONObject uploadFile(File tmpfile) {
    String downloadCenterUrl = exportConfig.getUrl();
    if (StringUtils.isEmpty(downloadCenterUrl)) {
      if (StringUtils.isEmpty(downloadCenterUrl)) {
        throw new OperationException("DownloadCenterService.uploadFile", "下载中心地址为空");
      }
    }
    CloseableHttpClient httpClient = HttpClientBuilder.create().build();
    HttpPost httpPost = new HttpPost(downloadCenterUrl);
    RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(60000)
        .setSocketTimeout(120000).build();
    httpPost.setConfig(requestConfig);
    FileBody bin = new FileBody(tmpfile);
    HttpEntity req = MultipartEntityBuilder.create()
        .setCharset(Charset.forName("utf-8"))
        .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
        .addPart("file", bin).build();
    httpPost.setEntity(req);
    System.out.println("executing request " + httpPost.getRequestLine());
    CloseableHttpResponse response = null;
    HttpEntity res = null;
    try {
      response = httpClient.execute(httpPost);
      System.out.println(response.getStatusLine());
      res = response.getEntity();
      if (res != null) {
        String responseStr = EntityUtils.toString(res);
        System.out.println("res:" + responseStr);
        JSONObject httpEntityJSONObject = JSONObject.parseObject(responseStr);
        EntityUtils.consume(res);
        response.close();
        httpClient.close();
        tmpfile.delete();
        return httpEntityJSONObject;
      }
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } finally {
      try {
        EntityUtils.consume(res);
        response.close();
        httpClient.close();
        tmpfile.delete();
      } catch (Exception e) {
        e.printStackTrace();
        return null;
      }
    }
    return null;
  }

  public void downLoadFile(DownLoadDTO downLoadDTO, HttpServletResponse response) {
    BufferedInputStream dis = null;
    BufferedOutputStream fos = null;

    String urlString = downLoadDTO.getFileDownloadUri();
    String fileName = downLoadDTO.getFileName();

    try {
      URL url = new URL(urlString);
      response.setContentType("application/octet-stream");
      response.setHeader("Content-disposition",
          "attachment; filename=" + new String(fileName.getBytes("utf-8"), "ISO8859-1"));
      response.setHeader("Content-Length", String.valueOf(url.openConnection().getContentLength()));
      dis = new BufferedInputStream(url.openStream());
      fos = new BufferedOutputStream(response.getOutputStream());

      byte[] buff = new byte[2048];
      int bytesRead;
      while (-1 != (bytesRead = dis.read(buff, 0, buff.length))) {
        fos.write(buff, 0, bytesRead);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (dis != null) {
        try {
          dis.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      if (fos != null) {
        try {
          fos.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

}

