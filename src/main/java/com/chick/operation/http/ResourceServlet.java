package com.chick.operation.http;

import com.alibaba.fastjson.JSONObject;
import com.chick.operation.http.util.IPAddress;
import com.chick.operation.util.Utils;
import com.chick.operation.http.util.IPRange;
import lombok.Builder;
import lombok.Data;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StringUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class ResourceServlet extends HttpServlet {

  private final static Log LOG = LogFactory.getLog(ResourceServlet.class);


  public static final String SESSION_USER_KEY = "admin-console-user";
  public static final String PARAM_NAME_USERNAME = "loginUsername";
  public static final String PARAM_NAME_PASSWORD = "loginPassword";
  public static final String PARAM_NAME_ALLOW = "allow";
  public static final String PARAM_NAME_DENY = "deny";
  public static final String PARAM_REMOTE_ADDR = "remoteAddress";
  public static final String DEFAULT_PASSWORD = "Spring01";

  protected String username = null;
  protected String password = null;

  protected List<IPRange> allowList = new ArrayList<IPRange>();
  protected List<IPRange> denyList = new ArrayList<IPRange>();

  protected final String resourcePath;

  protected String remoteAddressHeader = null;


  public ResourceServlet(String resourcePath) {
    this.resourcePath = resourcePath;
  }

  public void init() throws ServletException {
    initAuthEnv();
  }

  private void initAuthEnv() {
    String paramUserName = getInitParameter(PARAM_NAME_USERNAME);
    if (!StringUtils.isEmpty(paramUserName)) {
      this.username = paramUserName;
    }

    String paramPassword = getInitParameter(PARAM_NAME_PASSWORD);
    if (!StringUtils.isEmpty(paramPassword)) {
      this.password = paramPassword;
    } else {
      this.password = DEFAULT_PASSWORD;
    }

    String paramRemoteAddressHeader = getInitParameter(PARAM_REMOTE_ADDR);
    if (!StringUtils.isEmpty(paramRemoteAddressHeader)) {
      this.remoteAddressHeader = paramRemoteAddressHeader;
    }

    try {
      String param = getInitParameter(PARAM_NAME_ALLOW);
      if (param != null && param.trim().length() != 0) {
        param = param.trim();
        String[] items = param.split(",");

        for (String item : items) {
          if (item == null || item.length() == 0) {
            continue;
          }

          IPRange ipRange = new IPRange(item);
          allowList.add(ipRange);
        }
      }
    } catch (Exception e) {
      String msg = "initParameter config error, allow : " + getInitParameter(PARAM_NAME_ALLOW);
      LOG.error(msg, e);
    }

    try {
      String param = getInitParameter(PARAM_NAME_DENY);
      if (param != null && param.trim().length() != 0) {
        param = param.trim();
        String[] items = param.split(",");

        for (String item : items) {
          if (item == null || item.length() == 0) {
            continue;
          }

          IPRange ipRange = new IPRange(item);
          denyList.add(ipRange);
        }
      }
    } catch (Exception e) {
      String msg = "initParameter config error, deny : " + getInitParameter(PARAM_NAME_DENY);
      LOG.error(msg, e);
    }


  }

  public boolean isPermittedRequest(String remoteAddress) {
    boolean ipV6 = remoteAddress != null && remoteAddress.indexOf(':') != -1;
    if (ipV6) {
      return "0:0:0:0:0:0:0:1".equals(remoteAddress) || (denyList.size() == 0
          && allowList.size() == 0);
    }

    IPAddress ipAddress = new IPAddress(remoteAddress);
    for (IPRange range : denyList) {
      if (range.isIPAddressInRange(ipAddress)) {
        return false;
      }
    }
    if (allowList.size() > 0) {
      for (IPRange range : allowList) {
        if (range.isIPAddressInRange(ipAddress)) {
          return true;
        }
      }
      return false;
    }

    return true;
  }

  protected String getFilePath(String fileName) {
    return resourcePath + fileName;
  }

  protected void returnResourceFile(String fileName, String uri, HttpServletResponse response)
      throws ServletException,
      IOException {

    String filePath = getFilePath(fileName);
    if (filePath.endsWith(".html")) {
      response.setContentType("text/html; charset=utf-8");
    }
    if (fileName.endsWith(".jpg") || fileName.endsWith(".png") || fileName.startsWith("/fonts")) {
      byte[] bytes = Utils.readByteArrayFromResource(filePath);
      if (bytes != null) {
        response.getOutputStream().write(bytes);
      }

      return;
    }

    String text = Utils.readFromResource(filePath);
    if (text == null) {
      response.sendRedirect(uri + "/index.html");
      return;
    }
    if (fileName.endsWith(".css")) {
      response.setContentType("text/css;charset=utf-8");
    } else if (fileName.endsWith(".js")) {
      response.setContentType("text/javascript;charset=utf-8");
    }
    response.getWriter().write(text);
  }

  public void service(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {
    String contextPath = request.getContextPath();
    String servletPath = request.getServletPath();
    String requestURI = request.getRequestURI();
    response.setCharacterEncoding("utf-8");

    if (contextPath == null) { // root context
      contextPath = "";
    }
    String uri = contextPath + servletPath;
    String path = requestURI.substring(contextPath.length() + servletPath.length());

    if (!isPermittedRequest(request)) {
      path = "/nopermit.html";
      returnResourceFile(path, uri, response);
      return;
    }

    if ("/submitLogin".equals(path)) {
      String usernameParam = request.getParameter(PARAM_NAME_USERNAME);
      String passwordParam = request.getParameter(PARAM_NAME_PASSWORD);
      if (!StringUtils.isEmpty(username) && username
          .equals(usernameParam) && password.equals(passwordParam)) {
        request.getSession().setAttribute(SESSION_USER_KEY, username);
        response.getWriter().print(JSONObject.toJSONString(
            LoginResult.builder().code("success").account(usernameParam).build()));
        return;
      }

      return;
    }

    if ("/logout".equals(path)) {
      try {
        request.getSession().invalidate();
        response.getWriter().print("success");
      } catch (Exception e) {
        response.getWriter().print("error");
      }
      return;
    }

    if (!ContainsUser(request)//
        && !checkLoginParam(request)//
        && !("/login.html".equals(path) //
        || path.startsWith("/css")//
        || path.startsWith("/js") //
        || path.startsWith("/img"))) {

      if (contextPath.equals("") || contextPath.equals("/")) {
        response.sendRedirect("/admin-console/login.html");
      } else {
        if ("".equals(path)) {
          response.sendRedirect("admin-console/login.html");
        } else {
          response.sendRedirect("login.html");
        }
      }
      return;
    }

    if ("".equals(path)) {
      if (contextPath.equals("") || contextPath.equals("/")) {
        response.sendRedirect("/admin-console/index.html");
      } else {
        response.sendRedirect("admin-console/index.html");
      }
      return;
    }

    if ("/".equals(path)) {
      response.sendRedirect("index.html");
      return;
    }

    if (path.contains(".json")) {
      String fullUrl = path;
      if (request.getQueryString() != null && request.getQueryString().length() > 0) {
        fullUrl += "?" + request.getQueryString();
      }
      response.getWriter().print(process(fullUrl));
      return;
    }

    // find file in resources path
    returnResourceFile(path, uri, response);
  }

  public boolean ContainsUser(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    return session != null && session.getAttribute(SESSION_USER_KEY) != null;
  }

  public boolean checkLoginParam(HttpServletRequest request) {
    String usernameParam = request.getParameter(PARAM_NAME_USERNAME);
    String passwordParam = request.getParameter(PARAM_NAME_PASSWORD);
    if (null == username || null == password) {
      if (StringUtils.isEmpty(usernameParam) || StringUtils.isEmpty(passwordParam)) {
        return false;
      }
    } else if (username.equals(usernameParam) && password.equals(passwordParam)) {
      return true;
    }
    return false;
  }



//  public boolean isRequireAuth() {
//    return this.username != null;
//  }

  public boolean isPermittedRequest(HttpServletRequest request) {
    String remoteAddress = getRemoteAddress(request);
    return isPermittedRequest(remoteAddress);
  }

  protected String getRemoteAddress(HttpServletRequest request) {
    String remoteAddress = null;

    if (remoteAddressHeader != null) {
      remoteAddress = request.getHeader(remoteAddressHeader);
    }

    if (remoteAddress == null) {
      remoteAddress = request.getRemoteAddr();
    }

    return remoteAddress;
  }

  protected abstract String process(String url);




  @Data
  @Builder
  static class LoginResult {

    private String code;
    private String account;
  }
}