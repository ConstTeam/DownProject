package message.hall;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.module.player.Player;
import message.hall.login.LoginMessageConst;
import message.hall.role.RoleMsgSend;
import net.ByteBufferFactory;
import net.IByteBuffer;
import net.ISession;

/**
 * 客户端消息发送
 *
 */
public class S2CMessageSend {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(S2CMessageSend.class);
	
	public static final int NORMAL_MESSAGE = 0;
	
	public static final int EXIT_MESSAGE = 1;
	
	/**
	 * 发送角色信息
	 * 
	 * @param player
	 */
	public static void loginResult(ISession session, Player player, String platform) {
		RoleMsgSend.syncRoleInfo(session, player, platform);
	}

	public static void messageBox(ISession session, String message) {
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		data.writeByte(HallMsgModuleConst.LOGIN_SERVICE);
		data.writeByte(LoginMessageConst.MESSAGE_BOX);

		data.writeUTF(message);

		session.send(data);
	}

	public static void assignServer(ISession session, boolean result, String ip, int port) {
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		data.writeByte(HallMsgModuleConst.LOGIN_SERVICE);
		data.writeByte(LoginMessageConst.ASSIGN_INSTANCE_SERVER_RES);

		data.writeBoolean(result);
		if (result) {
			data.writeUTF(ip);
			data.writeShort(port);
		}
		
		session.send(data);
	}

	public static void reLoginServer(ISession session, String ip, int port) {
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		data.writeByte(HallMsgModuleConst.LOGIN_SERVICE);
		data.writeByte(LoginMessageConst.RE_LOGIN_SERVER);

		data.writeUTF(ip);
		data.writeShort(port);
		
		session.send(data);
	}
	
	public static void sendMultiMessage(Collection<ISession> sessions, IByteBuffer data) {
		if (sessions == null || sessions.size() == 0) {
			return;
		}
		sendMultiMessage(sessions, data, null);
	}
	
	public static void sendMultiMessage(Collection<ISession> sessions, IByteBuffer data, ISession exSession) {
		for (ISession session : sessions) {
			if (session == null) {
				continue;
			}
			if (session.equals(exSession)) {
				continue;
			}
			if (!session.isClosed()) {
				session.send(data);
			}
		}
	}
}
