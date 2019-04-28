package redis.subscribe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.JedisPubSub;
import util.ErrorPrint;

public class GameRoomListener extends JedisPubSub {
	
	private static Logger logger = LoggerFactory.getLogger(GameRoomListener.class);
	
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
		case "room.*":
			roomMessage(pattern, channel, message);
			break;
		}
		
	}
	
	private void roomMessage(String pattern, String channel, String message) {
		String substring = pattern.substring(0, pattern.length() - 1);
		String roomIdStr = channel.replace(substring, "");
		try {
			@SuppressWarnings("unused")
			int roomId = Integer.parseInt(roomIdStr);
			switch (message) {
			case SubPubConst.GM_DESTORY_ROOM:
			}
		} catch (Exception e) {
			ErrorPrint.print(e);
		}
	}
}
