package platform.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.sun.net.httpserver.HttpExchange;

import util.ErrorPrint;

/**
 * HttpHandler获取浏览器参数类
 * 
 * @author ZY
 *
 */
public class HttpHandlerMethod {
	private static final Logger logger = LoggerFactory.getLogger(HttpHandlerMethod.class);
	public static final int NORMAL_WAY = 1;
	public static final int JSON_WAY = 2;

	/**
	 * Get方式
	 * 
	 * @param exchange
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String parseGetParameters(HttpExchange exchange, int type) throws UnsupportedEncodingException {

		Map<String, Object> parameters = new HashMap<String, Object>();
		URI requestedUri = exchange.getRequestURI();
		String query = requestedUri.getRawQuery();
		if (query != null) {
			query = URLDecoder.decode(query, "utf-8");
		}
		logger.info(query);
		if (type == NORMAL_WAY) {
			parseQuery(query, parameters);
		} else if (type == JSON_WAY) {
			parseJson(query, parameters);
		}
		exchange.setAttribute("parameters", parameters);
		exchange.setAttribute("get", query);
		return query;
	}

	/**
	 * Post方式
	 * 
	 * @param exchange
	 * @throws IOException
	 */
	public static void parsePostParameters(HttpExchange exchange, int type) throws IOException {
		if ("post".equalsIgnoreCase(exchange.getRequestMethod())) {
			Map<String, Object> parameters = new HashMap<String, Object>();
			InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), "UTF8");
			BufferedReader br = new BufferedReader(isr);
			String query = br.readLine();
			if (query != null) {
				query = URLDecoder.decode(query, "utf-8");
			}
			logger.info(query);
			if (type == NORMAL_WAY) {
				parseQuery(query, parameters);
			} else if (type == JSON_WAY) {
				parseJson(query, parameters);
			}
			exchange.setAttribute("parameters", parameters);
			exchange.setAttribute("get", query);
		}
	}

	public static void parseQuery(String query, Map<String, Object> parameters) throws UnsupportedEncodingException {

		if (query != null) {
			String pairs[] = query.split("[&]");
			for (String pair : pairs) {
				String param[] = pair.split("[=]");
				String key = null;
				String value = null;
				if (param.length > 0) {
					key = URLDecoder.decode(param[0], System.getProperty("file.encoding"));
				}

				if (param.length > 1) {
					value = URLDecoder.decode(param[1], "UTF8");
				}
				if (parameters.containsKey(key)) {
					Object obj = parameters.get(key);
					if (obj instanceof List<?>) {
						@SuppressWarnings("unchecked")
						List<String> values = (List<String>) obj;
						values.add(value);
					} else if (obj instanceof String) {
						List<String> values = new ArrayList<String>();
						values.add((String) obj);
						values.add(value);
						parameters.put(key, values);
					}
				} else {
					parameters.put(key, value);
				}
			}
		}
	}

	public static void parseJson(String query, Map<String, Object> parameters) throws UnsupportedEncodingException {
		JSONObject json = JSONObject.parseObject(query);
		if (json != null && json.size() > 0) {
			Iterator<Entry<String, Object>> iter = json.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, Object> entry = iter.next();
				String value = entry.getValue().toString();
				parameters.put(entry.getKey(), value);
			}
		}
	}

	public static String[] split(String orderId) {
		return orderId.split("\\|");
	}

	/**
	 * HTTP 向指定 URL发送POST方法的请求
	 * 
	 * @param goUrl
	 *            请求地址
	 * @param param
	 *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式
	 * @return 所代表远程资源的响应结果
	 */
	public static String sendPost(String goUrl, String param) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		try {
			URL realUrl = new URL(goUrl);
			// 打开和URL之间的连接
			URLConnection conn = realUrl.openConnection();
			// 设置通用的请求属性
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// 获取URLConnection对象对应的输出流
			out = new PrintWriter(conn.getOutputStream());
			// 发送请求参数
			out.print(param);
			// flush输出流的缓冲
			out.flush();
			// 定义BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			System.out.println("发送 POST 请求出现异常！" + e);
			ErrorPrint.print(e);
		}
		// 使用finally块来关闭输出流、输入流
		finally {
			try {
				if (out != null) {
					out.close();
				}
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				ErrorPrint.print(ex);
			}
		}
		return result;
	}
	public static String sendPost(String url,Map<String, Object> sParaTemp) {
    	String response = null;
    	try {
    		HttpClient httpClient = new HttpClient();

             PostMethod post = new PostMethod(url);
             post.setRequestHeader("Content-Type","application/x-www-form-urlencoded;charset=utf-8");
             List<String> keys = new ArrayList<String>(sParaTemp.keySet());
             NameValuePair[] param = new NameValuePair[keys.size()+1];
             for (int i = 0; i < keys.size(); i++) {
                 String name = keys.get(i);
                 Object object = sParaTemp.get(name);
                 String value = "";
                 if (object != null) {
                     value =String.valueOf(sParaTemp.get(name));
                 }
                 //添加参数
                 param[i] = new NameValuePair(name, value);
                 post.setParameter(param[i].getName(),param[i].getValue());
             }
             HttpMethod method = post;
             httpClient.executeMethod(method);
             response = method.getResponseBodyAsString();
             post.releaseConnection();
		} catch (Exception e) {
			ErrorPrint.print(e);
		}
        return response;
     }
	@SuppressWarnings("deprecation")
	public static String sendPostByHttps(String url, String body) {
        String result = "";
        Protocol https = new Protocol("https", new HTTPSSecureProtocolSocketFactory(), 443);
        Protocol.registerProtocol("https", https);
        PostMethod post = new PostMethod(url);
        HttpClient client = new HttpClient();
        try {
            post.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
            post.setRequestBody(body);
            client.executeMethod(post);
            InputStream inputStream = post.getResponseBodyAsStream();  
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream,"UTF-8"));  
            StringBuffer stringBuffer = new StringBuffer();  
            String str= "";  
            while((str = br.readLine()) != null){  
            	stringBuffer.append(str);  
            }  
            result = stringBuffer.toString();
            Protocol.unregisterProtocol("https");
            return result;
        } catch (HttpException e) {
            ErrorPrint.print(e);
        } catch (IOException e) {
            ErrorPrint.print(e);
        } catch(Exception e) {
            ErrorPrint.print(e);
        }
     
        return "error";
    }
    

	public static String sendGet(String goUrl) {

		BufferedReader reader = null;
		HttpURLConnection connection = null;
		String result = "";
		try {

			URL getUrl = new URL(goUrl);
			connection = (HttpURLConnection) getUrl.openConnection();
			// 进行连接，但是实际上get request要在下一句的connection.getInputStream()函数中才会真正发到
			connection.connect();
			connection.setConnectTimeout(3000);
			// 取得输入流，并使用Reader读取
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));// 设置编码,否则中文乱码
			String line;
			while ((line = reader.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			System.out.println("发送 get 请求出现异常！" + e);
			ErrorPrint.print(e);
		}
		// 使用finally块来关闭输出流、输入流
		finally {
			try {
				if (reader != null) {
					reader.close();
				}
				// 断开连接
				if (connection != null) {
					connection.disconnect();
				}
			} catch (IOException ex) {
				ErrorPrint.print(ex);
			}
		}
		return result;
	}
}
