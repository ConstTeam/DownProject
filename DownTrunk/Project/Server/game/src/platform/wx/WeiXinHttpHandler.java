package platform.wx;

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

/**
 * 微信SDK
 * 
 * @author HSW
 *
 */

public class WeiXinHttpHandler {

	private static final Logger logger = LoggerFactory.getLogger(WeiXinHttpHandler.class);
	
	private static String appId = "wxa9b60d30be15fe37";
    private static String appSecret = "095e697451ab0d52f62593f951d1b28f";
	/** 通过code获取access_token */
	private static final String url_get_access_token= "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
	/** 刷新或续期access_token */
	private static final String url_refresh_token = "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid=%s&grant_type=refresh_token&refresh_token=%s";
	/** 获取用户信息 */
	private static final String url_user_info = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
	
	/**
	 * 微信登录验证
	 * 
	 * @return
	 */
	public static WXUserInfoEntity login(String code) {
		WXTokenEntity token = WeiXinHttpHandler.getToken(code);
		if (token == null) {
			logger.error("微信登录失败！code验证失败！");
			return null;
		}
//		WXTokenEntity token = new WXTokenEntity();
//		token.setAccess_token("9_jXgrvaX8TXc8tnpivQvvVnRSode1RoZZ0bk3aSCxVFKbysE7UHkdYTf13HavFPjavADbsdgJ08KMuz_tQirOMiWStRpPsf9DBCiDZxwpxJ0");
//		token.setOpenid("olOM-1jWvfAlutRrOFatdRNIgYBM");
		WXUserInfoEntity userInfo = WeiXinHttpHandler.getUserInfo(token);
		if (userInfo == null) {
			logger.error("微信登录失败！获取用户信息失败！");
			return null;
		}
		return userInfo;
	}
	
	/**
	 * 微信登录验证
	 * 
	 * @return
	 */
	private static WXTokenEntity getToken(String code) {

		String url = String.format(url_get_access_token, appId, appSecret, code);
		HttpURLConnection connection = null;
		BufferedReader reader = null;
		try {
			logger.info("微信登录验证url："+url);
			URL getUrl = new URL(url);
			connection = (HttpURLConnection) getUrl.openConnection();
			
			// 进行连接，但是实际上get request要在下一句的connection.getInputStream()函数中才会真正发到
			connection.connect();
			connection.setConnectTimeout(3000);
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));// 设置编码,否则中文乱码
			String lines;
			JSONObject json;
			while ((lines = reader.readLine()) != null) {
				//{"msg": "success"}
				json = JSONObject.parseObject(lines);
				if (json.getString("errcode") != null) {
					logger.error("微信登录失败。错误码：" + json.toJSONString());
				} else {
					logger.info("微信登录成功。" + json.toJSONString());
					WXTokenEntity vo = (WXTokenEntity) JSON.parseObject(lines, WXTokenEntity.class);
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
	
	/**
	 * 微信Token续期
	 * 
	 * @param refreshToken
	 * @return
	 */
	public static WXTokenEntity refreshToken(String refreshToken) {

		String url = String.format(url_refresh_token, appId, refreshToken);
		HttpURLConnection connection = null;
		BufferedReader reader = null;
		try {
			logger.info("微信Token续期url："+url);
			URL getUrl = new URL(url);
			connection = (HttpURLConnection) getUrl.openConnection();
			
			// 进行连接，但是实际上get request要在下一句的connection.getInputStream()函数中才会真正发到
			connection.connect();
			connection.setConnectTimeout(3000);
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));// 设置编码,否则中文乱码
			String lines;
			JSONObject json;
			while ((lines = reader.readLine()) != null) {
				//{"msg": "success"}
				json = JSONObject.parseObject(lines);
				if (json.getString("errcode") != null) {
					logger.error("微信Token续期失败。错误码：" + json.toJSONString());
				} else {
					logger.info("微信Token续期成功。" + json.toJSONString());
					WXTokenEntity vo = (WXTokenEntity) JSON.parseObject(lines, WXTokenEntity.class);
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
	
	/**
	 * 获取用户信息
	 * 
	 * @param accessToken
	 * @param openId
	 * @return
	 */
	public static WXUserInfoEntity getUserInfo(WXTokenEntity token) {

		String url = String.format(url_user_info, token.getAccess_token(), token.getOpenid());
		HttpURLConnection connection = null;
		BufferedReader reader = null;
		try {
			logger.info("微信获取用户信息url："+url);
			URL getUrl = new URL(url);
			connection = (HttpURLConnection) getUrl.openConnection();
			
			// 进行连接，但是实际上get request要在下一句的connection.getInputStream()函数中才会真正发到
			connection.connect();
			connection.setConnectTimeout(3000);
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));// 设置编码,否则中文乱码
			String lines;
			JSONObject json;
			while ((lines = reader.readLine()) != null) {
				//{"msg": "success"}
				json = JSONObject.parseObject(lines);
				if (json.getString("errcode") != null) {
					logger.error("微信获取用户信息失败。错误码：" + json.toJSONString());
				} else {
					logger.info("微信获取用户信息成功。" + json.toJSONString());
					WXUserInfoEntity vo = (WXUserInfoEntity) JSON.parseObject(lines, WXUserInfoEntity.class);
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
