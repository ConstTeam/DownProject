package message.game.other;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import message.game.GameMsgModuleConst;
import net.ByteBufferFactory;
import net.IByteBuffer;
import net.ISession;

/**
 * ����ģ����Ϣ����
 *
 */
public class OtherMsgSend {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(OtherMsgSend.class);
	
	/**
	 * ��ͻ���ͬ�����뷿����Ϣ
	 * @param session
	 */
	public static void intoRoom(ISession session) {
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.ROOM_RESPONSE);
		data.writeByte(OtherMsgConst.INTO_ROOM);
		
		session.send(data);
	}
}
