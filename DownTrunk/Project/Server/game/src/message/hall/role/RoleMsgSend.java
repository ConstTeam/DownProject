package message.hall.role;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.module.player.Player;
import message.game.fight.FightMsgSend;
import message.hall.GMHallService;
import message.hall.HallMsgModuleConst;
import net.ByteBufferFactory;
import net.IByteBuffer;
import net.ISession;

public class RoleMsgSend {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(FightMsgSend.class);
	
	/**
	 * 同步角色信息
	 * @param session
	 * @param player
	 */
	public static void syncRoleInfo(ISession session, Player player, String platform) {
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		data.writeByte(HallMsgModuleConst.ROLE_RESPONSE);
		data.writeByte(RoleMsgConst.ROLE_INFO_RES);
		
		data.writeBoolean(GMHallService.gmPanel(platform, player.getDeviceId()));
		data.writeInt(player.getPlayerId());
		data.writeUTF(player.getNickname());
		data.writeInt(player.getGold());
		data.writeUTF(player.getIcon());
		
		session.send(data);
	}

	public static void changeSceneRes(ISession session, int sceneId) {
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		data.writeByte(HallMsgModuleConst.ROLE_RESPONSE);
		data.writeByte(RoleMsgConst.CHANGE_SCENE_RES);
		
		data.writeByte(sceneId);
		
		session.send(data);
	}
}
