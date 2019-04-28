package message.game.room;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import message.game.GameMsgModuleConst;
import net.ByteBufferFactory;
import net.IByteBuffer;
import net.ISession;

/**
 * 房间模块消息发送
 *
 */
public class RoomMsgSend {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(RoomMsgSend.class);
	
	/**
	 * 向客户端同步进入房间消息
	 * @param session
	 */
	public static void intoRoom(ISession session) {
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.ROOM_RESPONSE);
		data.writeByte(RoomMsgConst.INTO_ROOM);
		
		session.send(data);
	}
}
