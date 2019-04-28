package handler.gm;

import java.io.IOException;
import java.sql.Connection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import app.ServerManager;
import platform.util.HttpHandlerMethod;
import util.ErrorPrint;
import db.AccountDao;
import module.Account;
import util.Tools;
import com.alibaba.fastjson.JSONObject;
import util.Base64;
import handler.gm.GMConst;
/**
 * 查询账号信息
 *
 */
public class QueryAccountHttpHandler implements HttpHandler {

	private static final Logger logger = LoggerFactory.getLogger(QueryAccountHttpHandler.class);

	@Override
	public void handle(HttpExchange he) throws IOException {
		logger.info("AddCardHttpHandler add card " + he.getRemoteAddress().toString());
		JSONObject result = new JSONObject();
		result.put("errorCode", -1);
		Connection connect = ServerManager.gameDBConnect.getDBConnect();
		if (connect == null) { // 数据库连接池已满
			return;
		}
		try {
			HttpHandlerMethod.parsePostParameters(he, HttpHandlerMethod.NORMAL_WAY);
			@SuppressWarnings("unchecked")
			Map<String, Object> attributes = (Map<String, Object>) he.getAttribute("parameters");
			String sign = (String)attributes.get("sign");
			String parametersStr = (String)attributes.get("parameters");

			String parametersStrBinary = new String(Base64.encode(parametersStr.getBytes("utf-8")));
			String mySign = Tools.md5(parametersStrBinary+GMConst.GM_SIGN_KEY).toLowerCase();
			if(!sign.trim().equals(mySign)){//验签
				return;
			}
			JSONObject jsonData = JSONObject.parseObject(parametersStr);
			String accountName = jsonData.getString("accountName");
			Account account = AccountDao.getAccount(connect, accountName, "NONE", "test");
			result.put("errorCode", 0);
			result.put("playerId", account.playerId);
		} catch (Exception e) {
			ErrorPrint.print(e);
		} finally {
			ServerManager.gameDBConnect.closeConnect(connect);
			he.setAttribute("parameters", null);
			he.setAttribute("get", null);
			he.sendResponseHeaders(200, result.toString().length());
			he.getResponseBody().write(result.toString().getBytes("UTF-8"));
			he.getRequestBody().close();
			he.getResponseBody().close();
			he.close();
		}
	}

}
