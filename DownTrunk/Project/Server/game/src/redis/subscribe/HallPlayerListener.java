package redis.subscribe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import memory.SessionMemory;
import message.hall.S2CMessageSend;
import message.hall.login.LoginMessageSend;
import net.ISession;
import redis.clients.jedis.JedisPubSub;
import redis.data.ServerInfo;
import util.ErrorPrint;

public class HallPlayerListener extends JedisPubSub {
	
	private static Logger logger = LoggerFactory.getLogger(HallPlayerListener.class);
	
	// 取得订阅的消息后的处理
	public void onMessage(String channel, String message) {
		// 接收订阅频道消息后，业务处理逻辑
		logger.debug(channel + "=" + message);
	}

	// 初始化订阅时候的处理
	public void onSubscribe(String channel, int subscribedChannels) {
		logger.debug(channel + "=" + subscribedChannels);
	}

	// 取消订阅时候的处理
	public void onUnsubscribe(String channel, int subscribedChannels) {
		logger.debug(channel + "=" + subscribedChannels);
	}

	// 初始化按表达式的方式订阅时候的处理
	public void onPSubscribe(String pattern, int subscribedChannels) {
		logger.debug(pattern + "=" + subscribedChannels);
	}

	// 取消按表达式的方式订阅时候的处理
	public void onPUnsubscribe(String pattern, int subscribedChannels) {
		logger.debug(pattern + "=" + subscribedChannels);
	}

	// 取得按表达式的方式订阅的消息后的处理
	public void onPMessage(String pattern, String channel, String message) {
		logger.debug(pattern + "=" + channel + "=" + message);
		switch (pattern) {
		default:
			playerMessage(pattern, channel, message);
			break;
		}
		
	}

	private void playerMessage(String pattern, String channel, String message) {
		String substring = pattern.substring(0, pattern.length() - 1);
		String playerIdStr = channel.replace(substring, "");
		try {
			int playerId = Integer.parseInt(playerIdStr);
			
			switch (message) {
			case SubPubConst.PLAYER_LOGOUT:
				ISession session = SessionMemory.getInstance().getSession(playerId);
				if (session == null) {
					logger.error("玩家：{}。通知玩家踢号失败。玩家session不存在。", playerId);
					return;
				}
				S2CMessageSend.messageBox(session, "帐号重复登录，已被顶号下线");
				break;
			case SubPubConst.PLAYER_RECHARGE:
			case SubPubConst.GM_PLAYER_LOGOUT:
				break;
			default:
				// 向客户端发送所需连接至的ServerInfo
				ServerInfo info = ServerInfo.fromJson(message);
				session = SessionMemory.getInstance().getSession(playerId);
				if (session == null) {
					logger.error("玩家：{}。通知玩家进入游戏服失败，玩家session不存在。", playerId);
					return;
				}
				LoginMessageSend.assignSuccess(session);
				LoginMessageSend.connGameServer(session, info);
				break;
			}
			
		} catch (Exception e) {
			ErrorPrint.print(e);
		}
	}
}
