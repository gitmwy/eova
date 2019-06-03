/**
 * 
 */
package com.eova.common.utils.util;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * 
 * @author Qian Wenjin
 *
 */
public class HttpClientUtil {

    private static final Logger LOG = LogManager.getLogger(HttpClientUtil.class);

    private static final int CONNECT_TIMEOUT = 5000;//设置超时毫秒数

    private static final int SOCKET_TIMEOUT = 10000;//设置传输毫秒数

    private static final int REQUESTCONNECT_TIMEOUT = 3000;//获取请求超时毫秒数

    private static final int CONNECT_TOTAL = 50;//最大连接数

    private static final int CONNECT_ROUTE = 20;//设置每个路由的基础连接数

    private static final int VALIDATE_TIME = 30000;//设置重用连接时间

    private static final String RESPONSE_CONTENT = "通信失败";

    private static PoolingHttpClientConnectionManager manager = null;

    private static CloseableHttpClient client = null;

    static {
        ConnectionSocketFactory csf = PlainConnectionSocketFactory.getSocketFactory();
//        LayeredConnectionSocketFactory lsf = createSSLConnSocketFactory();
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", csf)
//                .register("https", lsf)
                .build();
        manager = new PoolingHttpClientConnectionManager(registry);
        manager.setMaxTotal(CONNECT_TOTAL);
        manager.setDefaultMaxPerRoute(CONNECT_ROUTE);
        manager.setValidateAfterInactivity(VALIDATE_TIME);
        SocketConfig config = SocketConfig.custom().setSoTimeout(SOCKET_TIMEOUT).build();
        manager.setDefaultSocketConfig(config);
        RequestConfig requestConf = RequestConfig.custom().setConnectTimeout(CONNECT_TIMEOUT)
                .setConnectionRequestTimeout(REQUESTCONNECT_TIMEOUT).setSocketTimeout(SOCKET_TIMEOUT).build();
        
        HttpRequestRetryHandler httpRequestRetryHandler = new HttpRequestRetryHandler() {
        	public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
        	if (executionCount >= 3) {// 如果已经重试了3次，就放弃
        	return false;
        	}
        	if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
        	return true;
        	}
        	if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常
        	return false;
        	}
        	if (exception instanceof InterruptedIOException) {// 超时
        	return true;
        	}
        	if (exception instanceof UnknownHostException) {// 目标服务器不可达
        	return false;
        	}
        	if (exception instanceof ConnectTimeoutException) {// 连接被拒绝
        	return false;
        	}
        	if (exception instanceof SSLException) {// ssl握手异常
        	return false;
        	}
        	HttpClientContext clientContext = HttpClientContext.adapt(context);
        	HttpRequest request = clientContext.getRequest();
        	// 如果请求是幂等的，就再次尝试
        	if (!(request instanceof HttpEntityEnclosingRequest)) {
        	return true;
        	}
        	return false;
        	}
        	};
        
        client = HttpClients.custom().setConnectionManager(manager).setDefaultRequestConfig(requestConf).setRetryHandler(
                //实现了HttpRequestRetryHandler接口的
        		httpRequestRetryHandler).build();
        if(manager!=null && manager.getTotalStats()!=null)
            LOG.info("客户池状态："+manager.getTotalStats().toString());
    }
    private HttpClientUtil(){} 
    private static HttpClientUtil instance = new HttpClientUtil(); 

    public static HttpClientUtil getInstance(){  
        return instance;  
    } 
    
    private static SSLConnectionSocketFactory createSSLConnSocketFactory() {
        SSLConnectionSocketFactory sslsf = null;
        SSLContext context;
        try {
            context = SSLContext.getInstance(SSLConnectionSocketFactory.TLS);
            sslsf = new SSLConnectionSocketFactory(context, NoopHostnameVerifier.INSTANCE);
        } catch (NoSuchAlgorithmException e) {
            LOG.error("SSL上下文创建失败，由于" + e.getLocalizedMessage());
            e.printStackTrace();
        }
        return sslsf;
    }

    private String res(HttpRequestBase method) {
        HttpClientContext context = HttpClientContext.create();
        CloseableHttpResponse response = null;
        String content = RESPONSE_CONTENT;
        try {
            response = client.execute(method, context);//执行GET/POST请求
            HttpEntity entity = response.getEntity();//获取响应实体
            if(entity!=null) {
                Charset charset = ContentType.getOrDefault(entity).getCharset();
                content = EntityUtils.toString(entity, charset);
                EntityUtils.consume(entity);
            }
        } catch(ConnectTimeoutException cte) {
            LOG.error("请求连接超时，由于 " + cte.getLocalizedMessage());
            cte.printStackTrace();
        } catch(SocketTimeoutException ste) {
            LOG.error("请求通信超时，由于 " + ste.getLocalizedMessage());
            ste.printStackTrace();
        } catch(ClientProtocolException cpe) {
            LOG.error("协议错误（比如构造HttpGet对象时传入协议不对(将'http'写成'htp')or响应内容不符合），由于 " + cpe.getLocalizedMessage());
            cpe.printStackTrace();
        } catch(IOException ie) {
            LOG.error("实体转换异常或者网络异常， 由于 " + ie.getLocalizedMessage());
            ie.printStackTrace();
        } finally {
            try {
                if(response!=null) {
                    response.close();
                }

            } catch(IOException e) {
                LOG.error("响应关闭异常， 由于 " + e.getLocalizedMessage());
            }

            if(method!=null) {
                method.releaseConnection();
            } 

        }

        return content;
    }

    public String get(String url) {
        HttpGet get = new HttpGet(url);
        return res(get);
    }

    public String get(String url, String cookie) {
        HttpGet get = new HttpGet(url);
        if(StringUtils.isNotBlank(cookie))
            get.addHeader("cookie", cookie);
        return res(get);
    }

    public byte[] getAsByte(String url) {
        return get(url).getBytes();
    }

    public String getHeaders(String url, String cookie, String headerName) {
        HttpGet get = new HttpGet(url);
        if(StringUtils.isNotBlank(cookie))
            get.addHeader("cookie", cookie);
        res(get);
        Header[] headers = get.getHeaders(headerName);
        return headers == null ? null : headers.toString();
    }

    public String getWithRealHeader(String url) {
        HttpGet get = new HttpGet(url);
        get.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;");
        get.addHeader("Accept-Language", "zh-cn");
        get.addHeader("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.0.3) Gecko/2008092417 Firefox/3.0.3");
        get.addHeader("Keep-Alive", "300");
        get.addHeader("Connection", "Keep-Alive");
        get.addHeader("Cache-Control", "no-cache");
        return res(get);
    }

//    public String post(String url, Map<String, String> param, String cookie) {
//        HttpPost post = new HttpPost(url);
//        post.
//        param.keySet().stream().forEach(key -> {
//            String value = param.get(key);
//            if(value!=null)
//                post.addHeader(key, value);
//        });
//        if(StringUtils.isNotBlank(cookie))
//            post.addHeader("cookie", cookie);
//        return res(post);
//    }

    public String post(String url, String data) {
        HttpPost post = new HttpPost(url);
        if(StringUtils.isNotBlank(data))
            post.addHeader("Content-Type", "application/json");
        post.setEntity(new StringEntity(data,ContentType.APPLICATION_JSON));
        return res(post);
    }

    public String post(String url, Map<String, String> param, String cookie, Map<String, String> headers) {
        HttpPost post = new HttpPost(url);
        String reqEntity = "";
        for(Entry<String, String> entry:param.entrySet()) {
            post.addHeader(entry.getKey(), entry.getValue());
            try {
                reqEntity += entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "utf-8") + "&";
            } catch (UnsupportedEncodingException e) {
                LOG.error("请求实体转换异常，不支持的字符集，由于 " + e.getLocalizedMessage());
                e.printStackTrace();
            }
        }

        try {
            post.setEntity(new StringEntity(reqEntity));
        } catch (UnsupportedEncodingException e) {
            LOG.error("请求设置实体异常，不支持的字符集， 由于 " + e.getLocalizedMessage());
            e.printStackTrace();
        }

        if(StringUtils.isNotEmpty(cookie))
            post.addHeader("cookie", cookie);

        return res(post);
    } 
    
    
    public static void main(String [] args){
    	String query = "http://ip.taobao.com/service/getIpInfo.php?ip=";
    	//query = "http://ip.taobao.com/service/getIpInfo.php";
		
    	String url = query+"220.163.11.18";
    	Map param = new HashMap();
    	param.put("ip", "220.163.11.18");
    	
		String json = HttpClientUtil.getInstance().getWithRealHeader(url);
		System.out.println(json);
		
		
		url = "http://127.0.0.1:801/single_grid/list/demo2_test-1?template=lay";
		json = HttpClientUtil.getInstance().get(url,"JSESSIONID=1iex1j5qctdax156k1v5d7xy2e");
		System.out.println(json);
    }
}