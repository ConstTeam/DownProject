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
		data.writeUTF(String.valueOf(player.getPlayerId()));
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

	public static void changeRoleRes(ISession session, int roleId) {
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		data.writeByte(HallMsgModuleConst.ROLE_RESPONSE);
		data.writeByte(RoleMsgConst.CHANGE_ROLE_RES);
		
		data.writeByte(roleId);
		
		session.send(data);
	}

	public static void roleGoldSync(ISession session, int gold) {
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		data.writeByte(HallMsgModuleConst.ROLE_RESPONSE);
		data.writeByte(RoleMsgConst.ROLE_GOLD_SYNC);
		
		data.writeInt(gold);
		
		session.send(data);
	}

	public static void roleListSync(ISession session, int roleList) {
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		data.writeByte(HallMsgModuleConst.ROLE_RESPONSE);
		data.writeByte(RoleMsgConst.ROLE_LIST_SYNC);

		data.writeInt(roleList);
		
		session.send(data);
	}

	public static void sceneListSync(ISession session, int sceneList) {
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		data.writeByte(HallMsgModuleConst.ROLE_RESPONSE);
		data.writeByte(RoleMsgConst.SCENE_LIST_SYNC);

		data.writeInt(sceneList);
		
		session.send(data);
	}

	public static void assignRes(ISession session, boolean res) {
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		data.writeByte(HallMsgModuleConst.ROLE_RESPONSE);
		data.writeByte(RoleMsgConst.ASSIGN_RES);

		data.writeBoolean(res);
		
		session.send(data);
	}
}
