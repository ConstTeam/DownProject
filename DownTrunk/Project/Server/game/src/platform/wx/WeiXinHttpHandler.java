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
 * ΢��SDK
 * 
 * @author HSW
 *
 */

public class WeiXinHttpHandler {

	private static final Logger logger = LoggerFactory.getLogger(WeiXinHttpHandler.class);
	
	private static String appId = "wxa9b60d30be15fe37";
    private static String appSecret = "095e697451ab0d52f62593f951d1b28f";
	/** ͨ��code��ȡaccess_token */
	private static final String url_get_access_token= "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";
	/** ˢ�»�����access_token */
	private static final String url_refresh_token = "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid=%s&grant_type=refresh_token&refresh_token=%s";
	/** ��ȡ�û���Ϣ */
	private static final String url_user_info = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
	
	/**
	 * ΢�ŵ�¼��֤
	 * 
	 * @return
	 */
	public static WXUserInfoEntity login(String code) {
		WXTokenEntity token = WeiXinHttpHandler.getToken(code);
		if (token == null) {
			logger.error("΢�ŵ�¼ʧ�ܣ�code��֤ʧ�ܣ�");
			return null;
		}
//		WXTokenEntity token = new WXTokenEntity();
//		token.setAccess_token("9_jXgrvaX8TXc8tnpivQvvVnRSode1RoZZ0bk3aSCxVFKbysE7UHkdYTf13HavFPjavADbsdgJ08KMuz_tQirOMiWStRpPsf9DBCiDZxwpxJ0");
//		token.setOpenid("olOM-1jWvfAlutRrOFatdRNIgYBM");
		WXUserInfoEntity userInfo = WeiXinHttpHandler.getUserInfo(token);
		if (userInfo == null) {
			logger.error("΢�ŵ�¼ʧ�ܣ���ȡ�û���Ϣʧ�ܣ�");
			return null;
		}
		return userInfo;
	}
	
	/**
	 * ΢�ŵ�¼��֤
	 * 
	 * @return
	 */
	private static WXTokenEntity getToken(String code) {

		String url = String.format(url_get_access_token, appId, appSecret, code);
		HttpURLConnection connection = null;
		BufferedReader reader = null;
		try {
			logger.info("΢�ŵ�¼��֤url��"+url);
			URL getUrl = new URL(url);
			connection = (HttpURLConnection) getUrl.openConnection();
			
			// �������ӣ�����ʵ����get requestҪ����һ���connection.getInputStream()�����вŻ���������
			connection.connect();
			connection.setConnectTimeout(3000);
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));// ���ñ���,������������
			String lines;
			JSONObject json;
			while ((lines = reader.readLine()) != null) {
				//{"msg": "success"}
				json = JSONObject.parseObject(lines);
				if (json.getString("errcode") != null) {
					logger.error("΢�ŵ�¼ʧ�ܡ������룺" + json.toJSONString());
				} else {
					logger.info("΢�ŵ�¼�ɹ���" + json.toJSONString());
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
				// �Ͽ�����
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
	 * ΢��Token����
	 * 
	 * @param refreshToken
	 * @return
	 */
	public static WXTokenEntity refreshToken(String refreshToken) {

		String url = String.format(url_refresh_token, appId, refreshToken);
		HttpURLConnection connection = null;
		BufferedReader reader = null;
		try {
			logger.info("΢��Token����url��"+url);
			URL getUrl = new URL(url);
			connection = (HttpURLConnection) getUrl.openConnection();
			
			// �������ӣ�����ʵ����get requestҪ����һ���connection.getInputStream()�����вŻ���������
			connection.connect();
			connection.setConnectTimeout(3000);
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));// ���ñ���,������������
			String lines;
			JSONObject json;
			while ((lines = reader.readLine()) != null) {
				//{"msg": "success"}
				json = JSONObject.parseObject(lines);
				if (json.getString("errcode") != null) {
					logger.error("΢��Token����ʧ�ܡ������룺" + json.toJSONString());
				} else {
					logger.info("΢��Token���ڳɹ���" + json.toJSONString());
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
				// �Ͽ�����
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
	 * ��ȡ�û���Ϣ
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
			logger.info("΢�Ż�ȡ�û���Ϣurl��"+url);
			URL getUrl = new URL(url);
			connection = (HttpURLConnection) getUrl.openConnection();
			
			// �������ӣ�����ʵ����get requestҪ����һ���connection.getInputStream()�����вŻ���������
			connection.connect();
			connection.setConnectTimeout(3000);
			reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));// ���ñ���,������������
			String lines;
			JSONObject json;
			while ((lines = reader.readLine()) != null) {
				//{"msg": "success"}
				json = JSONObject.parseObject(lines);
				if (json.getString("errcode") != null) {
					logger.error("΢�Ż�ȡ�û���Ϣʧ�ܡ������룺" + json.toJSONString());
				} else {
					logger.info("΢�Ż�ȡ�û���Ϣ�ɹ���" + json.toJSONString());
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
				// �Ͽ�����
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
