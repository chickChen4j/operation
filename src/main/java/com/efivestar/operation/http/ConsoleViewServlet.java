package com.efivestar.operation.http;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ConsoleViewServlet extends ResourceServlet {

    private final static Log LOG                     = LogFactory.getLog(ConsoleViewServlet.class);

    private static final long     serialVersionUID        = 1L;

    public static final String    PARAM_NAME_RESET_ENABLE = "resetEnable";

    public static final String    PARAM_NAME_JMX_URL      = "jmxUrl";
    public static final String    PARAM_NAME_JMX_USERNAME = "jmxUsername";
    public static final String    PARAM_NAME_JMX_PASSWORD = "jmxPassword";


    /** web.xml中配置的jmx的连接地址 */
    private String                jmxUrl                  = null;
    /** web.xml中配置的jmx的用户名 */
    private String                jmxUsername             = null;
    /** web.xml中配置的jmx的密码 */
    private String                jmxPassword             = null;
    private MBeanServerConnection conn                    = null;

    public ConsoleViewServlet(){
        super("operation/http/resources");
    }

    public void init() throws ServletException {
        super.init();
        try {
            String param = getInitParameter(PARAM_NAME_RESET_ENABLE);
            if (param != null && param.trim().length() != 0) {
                param = param.trim();
                boolean resetEnable = Boolean.parseBoolean(param);
            }
        } catch (Exception e) {
            String msg = "initParameter config error, resetEnable : " + getInitParameter(PARAM_NAME_RESET_ENABLE);
            LOG.error(msg, e);
        }

        // 获取jmx的连接配置信息
        String param = readInitParam(PARAM_NAME_JMX_URL);
        if (param != null) {
            jmxUrl = param;
            jmxUsername = readInitParam(PARAM_NAME_JMX_USERNAME);
            jmxPassword = readInitParam(PARAM_NAME_JMX_PASSWORD);
            try {
                initJmxConn();
            } catch (IOException e) {
                LOG.error("init jmx connection error", e);
            }
        }

    }

    /**
     * 读取servlet中的配置参数.
     *
     * @param key 配置参数名
     * @return 配置参数值，如果不存在当前配置参数，或者为配置参数长度为0，将返回null
     */
    private String readInitParam(String key) {
        String value = null;
        try {
            String param = getInitParameter(key);
            if (param != null) {
                param = param.trim();
                if (param.length() > 0) {
                    value = param;
                }
            }
        } catch (Exception e) {
            String msg = "initParameter config [" + key + "] error";
            LOG.warn(msg, e);
        }
        return value;
    }

    /**
     * 初始化jmx连接
     *
     * @throws IOException
     */
    private void initJmxConn() throws IOException {
        if (jmxUrl != null) {
            JMXServiceURL url = new JMXServiceURL(jmxUrl);
            Map<String, String[]> env = null;
            if (jmxUsername != null) {
                env = new HashMap<String, String[]>();
                String[] credentials = new String[] { jmxUsername, jmxPassword };
                env.put(JMXConnector.CREDENTIALS, credentials);
            }
            JMXConnector jmxc = JMXConnectorFactory.connect(url, env);
            conn = jmxc.getMBeanServerConnection();
        }
    }


    /**
     * 程序首先判断是否存在jmx连接地址，如果不存在，则直接调用本地的duird服务； 如果存在，则调用远程jmx服务。在进行jmx通信，首先判断一下jmx连接是否已经建立成功，如果已经
     * 建立成功，则直接进行通信，如果之前没有成功建立，则会尝试重新建立一遍。.
     *
     * @param url 要连接的服务地址
     * @return 调用服务后返回的json字符串
     */
    protected String process(String url) {
        String resp = null;

        return resp;
    }

}