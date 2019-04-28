package message.hall.login;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import message.hall.HallMsgModuleConst;
import net.ByteBufferFactory;
import net.IByteBuffer;
import net.ISession;
import redis.data.ServerInfo;

/**
 * 登录模块消息发送
 *
 */
public class LoginMessageSend {
	
	private static final Logger logger = LoggerFactory.getLogger(LoginMessageSend.class);
	
	public static void messsageBox(ISession session, int boxType, int textId1, int textId2, int hour, int minute) {
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(1);
		data.writeByte(LoginMessageConst.ANTI_MESSAGE_BOX);
		
		data.writeByte(boxType);
		
		data.writeInt(textId1);
		data.writeInt(hour);
		data.writeInt(textId2);
		data.writeInt(minute);
		
		session.send(data);
	}
	
	public static void connGameServer(ISession session, ServerInfo info) {
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(HallMsgModuleConst.LOGIN_RESPONSE);
		data.writeByte(LoginMessageConst.ASSIGN_INSTANCE_SERVER_RES);
		
		data.writeUTF(info.getIp());
		data.writeShort(info.getPort());
		
		session.send(data);
	}
	
	public static void cancelAssign(ISession session) {
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(HallMsgModuleConst.LOGIN_RESPONSE);
		data.writeByte(LoginMessageConst.CANCEL_ASSIGN_SYNC);
		
		session.send(data);
		logger.debug("{} - {}", HallMsgModuleConst.LOGIN_RESPONSE, LoginMessageConst.CANCEL_ASSIGN_SYNC);
	}
	
	public static void assignSuccess(ISession session) {
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(HallMsgModuleConst.LOGIN_RESPONSE);
		data.writeByte(LoginMessageConst.ASSIGN_SUCCESS_SYNC);
		
		session.send(data);
		logger.debug("{} - {}", HallMsgModuleConst.LOGIN_RESPONSE, LoginMessageConst.ASSIGN_SUCCESS_SYNC);
	}
}
