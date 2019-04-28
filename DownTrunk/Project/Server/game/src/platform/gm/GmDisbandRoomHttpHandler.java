package platform.gm;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import platform.util.HttpHandlerMethod;
import redis.RedisProxy;
import redis.data.RoomInfo;
import redis.subscribe.SubPubConst;
import util.ErrorPrint;


public class GmDisbandRoomHttpHandler extends HttpServlet implements HttpHandler {
		
	private static final Logger logger = LoggerFactory.getLogger(GmDisbandRoomHttpHandler.class);
	
	private static final long serialVersionUID = 1L;
	
	@Override
	public void handle(HttpExchange he) throws IOException {
		logger.info("GmDisbandRoomHttpHandler from " + he.getRemoteAddress().toString());
		
		String result;
		JSONObject info = new JSONObject();
		try {
			HttpHandlerMethod.parsePostParameters(he, HttpHandlerMethod.NORMAL_WAY);
			
			@SuppressWarnings("unchecked")
			Map<String, Object> attributes = (Map<String, Object>) he.getAttribute("parameters");
			if (attributes == null) {
				info.put("state", -1);
				return;
			}
			Iterator<Entry<String, Object>> iter =  attributes.entrySet().iterator();
			while(iter.hasNext()){
				Entry<String, Object> entry = iter.next();
				String key = entry.getKey();
				Object value = entry.getValue();
				logger.info("key:"+key);
				logger.info("value:"+value);
			}

			int roomId = Integer.parseInt((String) attributes.get("roomId"));
			
			RoomInfo roomInfo = RedisProxy.getInstance().getRoomInfo(roomId);
			if (roomInfo == null) {
				info.put("state", -3);
				return;
			}
			RedisProxy.getInstance().roomNotice(roomId, SubPubConst.GM_DESTORY_ROOM);
			info.put("state", 1);
		} catch (Exception e) {
			ErrorPrint.print(e);
			info.put("state", -2);
		} finally {
			result = info.toJSONString();
			logger.info("强制解散房间返回信息："+result);
			he.setAttribute("parameters", null);
			he.setAttribute("get", null);
			he.sendResponseHeaders(200, result.getBytes("UTF-8").length);
			he.getResponseBody().write(result.getBytes("UTF-8"));
			he.getRequestBody().close();
			he.getResponseBody().close();
			he.close();
			logger.info("GmDisbandRoomHttpHandler Over from " + he.getRemoteAddress().toString());
		}
	}
	
	
}
