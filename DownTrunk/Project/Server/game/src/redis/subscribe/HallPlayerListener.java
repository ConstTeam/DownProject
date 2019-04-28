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
	
	// ȡ�ö��ĵ���Ϣ��Ĵ���
	public void onMessage(String channel, String message) {
		// ���ն���Ƶ����Ϣ��ҵ�����߼�
		logger.debug(channel + "=" + message);
	}

	// ��ʼ������ʱ��Ĵ���
	public void onSubscribe(String channel, int subscribedChannels) {
		logger.debug(channel + "=" + subscribedChannels);
	}

	// ȡ������ʱ��Ĵ���
	public void onUnsubscribe(String channel, int subscribedChannels) {
		logger.debug(channel + "=" + subscribedChannels);
	}

	// ��ʼ�������ʽ�ķ�ʽ����ʱ��Ĵ���
	public void onPSubscribe(String pattern, int subscribedChannels) {
		logger.debug(pattern + "=" + subscribedChannels);
	}

	// ȡ�������ʽ�ķ�ʽ����ʱ��Ĵ���
	public void onPUnsubscribe(String pattern, int subscribedChannels) {
		logger.debug(pattern + "=" + subscribedChannels);
	}

	// ȡ�ð����ʽ�ķ�ʽ���ĵ���Ϣ��Ĵ���
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
					logger.error("��ң�{}��֪ͨ����ߺ�ʧ�ܡ����session�����ڡ�", playerId);
					return;
				}
				S2CMessageSend.messageBox(session, "�ʺ��ظ���¼���ѱ���������");
				break;
			case SubPubConst.PLAYER_RECHARGE:
			case SubPubConst.GM_PLAYER_LOGOUT:
				break;
			default:
				// ��ͻ��˷���������������ServerInfo
				ServerInfo info = ServerInfo.fromJson(message);
				session = SessionMemory.getInstance().getSession(playerId);
				if (session == null) {
					logger.error("��ң�{}��֪ͨ��ҽ�����Ϸ��ʧ�ܣ����session�����ڡ�", playerId);
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
