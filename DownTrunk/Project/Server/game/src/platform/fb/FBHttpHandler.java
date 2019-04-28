package platform.fb;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import util.ErrorPrint;

public class FBHttpHandler {
	private static final Logger logger = LoggerFactory.getLogger(FBHttpHandler.class);
	private static String appId = "2424049784276417";
    private static String appSecret = "b419766b5eb97a16561fa1e43b749f3e";
    private static String url_get_access_token = "https://graph.facebook.com/oauth/access_token?grant_type=fb_exchange_token&client_id=%s&client_secret=%s&fb_exchange_token=%s&access_token=%s";
    private static String url_get_user_info = "https://graph.facebook.com/me?fields=id,name,birthday,email&access_token=%s";
	/**
	 * 微信登录验证
	 * 
	 * @return
	 */
	public static FBUserInfoEntity login(String access_token) {
		String token = FBHttpHandler.getLongLivedAccessToken(access_token);
		if (token == null) {
			logger.error("Facebook登录失败！Facebook获取长期口令失败！");
			return null;
		}

		FBUserInfoEntity userInfo = FBHttpHandler.getUserInfo(token);
		if (userInfo == null) {
			logger.error("Facebook登录失败！获取用户信息失败！");
			return null;
		}
		return userInfo;
	}
	
    
	/**
	 * 获取长期有效的token
	 * @param access_token
	 * @return
	 */
    
    public static String getLongLivedAccessToken(String access_token) {
    	
    	//设定以下 是防止后面的fb的https请求访问不到 1080端口是本地vpn客户端监听端口
    	/*解决javax.net.ssl.SSLHandshakeException*/
		System.setProperty("socksProxyHost", "127.0.0.1");
    	System.setProperty("socksProxyPort", "1080");
    	/*解决javax.net.ssl.SSLHandshakeException:Remote host closed connection during handshake*/
    	System.setProperty("https.protocols", "TLSv1,TLSv1.1,TLSv1.2");
    	
    	//信任所有证书
//		HostnameVerifier hv = new HostnameVerifier() {
//	        public boolean verify(String urlHostName, SSLSession session) {
//	        	logger.info("Warning: URL Host: " + urlHostName + " vs. "	
//	        + session.getPeerHost());
//	            return true;
//	        }
//	    };
//	    TrustCertManager.trustAllHttpsCertificates();
//    	HttpsURLConnection.setDefaultHostnameVerifier(hv);

    	String longlived_accesstoken = "";
		String url = String.format(url_get_access_token, appId, appSecret, access_token,access_token);
		HttpURLConnection connection = null;
		BufferedReader reader = null;
		try {
			logger.info("Facebook获取长期口令："+url);
			URL getUrl = new URL(url);
			connection = (HttpURLConnection) getUrl.openConnection();
		
			
			connection.setRequestProperty("Content-Type", "application/json");
			connection.setRequestProperty("Accept-Charset", "UTF-8");
			connection.setRequestProperty("User-Agent",
		                "Mozilla/5.0 (Windows NT 5.1; rv:19.0) Gecko/20100101 Firefox/19.0");
            // 建立实际的连接
            connection.connect();
            
			connection.setConnectTimeout(3000);
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
			String lines;
			JSONObject json;
			while ((lines = reader.readLine()) != null) {
				json = JSONObject.parseObject(lines);
				if (json.getString("errcode") != null) {
					logger.error("Facebook获取长期口令错误码：" + json.toJSONString());
				} else {
					logger.error("Facebook获取长期口令成功：" + json.toJSONString());
					if (json.getString("access_token") != null) {
						longlived_accesstoken = json.getString("access_token");
						return longlived_accesstoken;
					}
				}	
			}
		} catch (Exception e) {
			ErrorPrint.print(e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				// 断开连接
				if (connection != null) {
					connection.disconnect();
				}
			} catch (IOException e) {
				ErrorPrint.print(e);
			}
		}
		return null;
	}
    
    /**
     * 获取用户信息
     * @param access_token
     * @return
     */
    
    private static FBUserInfoEntity getUserInfo(String access_token) {

		String url = String.format(url_get_user_info, access_token);
		HttpURLConnection connection = null;
		BufferedReader reader = null;
		try {
			logger.info("获取 Facebook 用户信息url："+url);
			URL getUrl = new URL(url);
			connection = (HttpURLConnection) getUrl.openConnection();
			connection.connect();
			connection.setConnectTimeout(3000);
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));// 设置编码,否则中文乱码
			String lines;
			JSONObject json;
			while ((lines = reader.readLine()) != null) {
				json = JSONObject.parseObject(lines);
				if (json.getString("errcode") != null) {
					logger.error("获取 Facebook 用户信息。错误码：" + json.toJSONString());
				} else {
					logger.info("获取 Facebook 用户信息成功。" + json.toJSONString());
					FBUserInfoEntity vo = (FBUserInfoEntity) JSON.parseObject(lines, FBUserInfoEntity.class);
	   				return vo;
				}
			}
		} catch (Exception e) {
			ErrorPrint.print(e);
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				// 断开连接
				if (connection != null) {
					connection.disconnect();
				}
			} catch (IOException e) {
				ErrorPrint.print(e);
			}
		}
		return null;
	}
}
