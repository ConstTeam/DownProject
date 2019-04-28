package platform.proxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import platform.util.HttpHandlerMethod;
import util.ErrorPrint;

/**
 * 
 * @author HSW
 *
 */

public class ProxyHttpHandler {

	private static final Logger logger = LoggerFactory.getLogger(ProxyHttpHandler.class);
	
	/** UserInfo注册 */
	private static final String user_info_register_url= "http://39.104.24.128:8081/proxy_manager/register.do";
	
	/**
	 * UserInfo注册
	 * 
	 * @return
	 */
	public static void UserRegister(String unionId, String nickname, int playerId, int sex) {

		HttpURLConnection connection = null;
		BufferedReader reader = null;
		try {
			Map<String, Object> param = new HashMap<>();
			param.put("unionId", unionId);
			param.put("userName", nickname);
			param.put("gameId", playerId);
			param.put("sex", sex);
			String result = HttpHandlerMethod.sendPost(user_info_register_url, param);
			logger.info("服务器接收到的数据：" + result);
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
	}
}