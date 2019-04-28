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
 * HttpHandler��ȡ�����������
 * 
 * @author ZY
 *
 */
public class HttpHandlerMethod {
	private static final Logger logger = LoggerFactory.getLogger(HttpHandlerMethod.class);
	public static final int NORMAL_WAY = 1;
	public static final int JSON_WAY = 2;

	/**
	 * Get��ʽ
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
	 * Post��ʽ
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
	 * HTTP ��ָ�� URL����POST����������
	 * 
	 * @param goUrl
	 *            �����ַ
	 * @param param
	 *            ����������������Ӧ���� name1=value1&name2=value2 ����ʽ
	 * @return ������Զ����Դ����Ӧ���
	 */
	public static String sendPost(String goUrl, String param) {
		PrintWriter out = null;
		BufferedReader in = null;
		String result = "";
		try {
			URL realUrl = new URL(goUrl);
			// �򿪺�URL֮�������
			URLConnection conn = realUrl.openConnection();
			// ����ͨ�õ���������
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// ����POST�������������������
			conn.setDoOutput(true);
			conn.setDoInput(true);
			// ��ȡURLConnection�����Ӧ�������
			out = new PrintWriter(conn.getOutputStream());
			// �����������
			out.print(param);
			// flush������Ļ���
			out.flush();
			// ����BufferedReader����������ȡURL����Ӧ
			in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			System.out.println("���� POST ��������쳣��" + e);
			ErrorPrint.print(e);
		}
		// ʹ��finally�����ر��������������
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
                 //��Ӳ���
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
			// �������ӣ�����ʵ����get requestҪ����һ���connection.getInputStream()�����вŻ���������
			connection.connect();
			connection.setConnectTimeout(3000);
			// ȡ������������ʹ��Reader��ȡ
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));// ���ñ���,������������
			String line;
			while ((line = reader.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			System.out.println("���� get ��������쳣��" + e);
			ErrorPrint.print(e);
		}
		// ʹ��finally�����ر��������������
		finally {
			try {
				if (reader != null) {
					reader.close();
				}
				// �Ͽ�����
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
