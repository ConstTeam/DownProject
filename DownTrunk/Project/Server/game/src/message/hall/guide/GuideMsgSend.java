package message.hall.guide;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import message.hall.HallMsgModuleConst;
import net.ByteBufferFactory;
import net.IByteBuffer;
import net.ISession;

/**
 * 指引模块消息发送
 *
 */
public class GuideMsgSend {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(GuideMsgSend.class);
	
	public static void guideInfoSync(ISession session, int guideId) {
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(HallMsgModuleConst.GUIDE_RESPONSE);
		data.writeByte(GuideMsgConst.GUIDE_INFO_SYNC);
		
		data.writeInt(guideId);
		session.send(data);
	}
	
	public static void updateGuideRet(ISession session, int guideId) {
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(HallMsgModuleConst.GUIDE_RESPONSE);
		data.writeByte(GuideMsgConst.UPDATE_GUIDE_RET);
		
		data.writeInt(guideId);
		session.send(data);
	}
}
