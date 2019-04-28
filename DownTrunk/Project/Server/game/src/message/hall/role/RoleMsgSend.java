package message.hall.role;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.ConfigData;
import db.module.player.Player;
import memory.SessionMemory;
import memory.UserMemory;
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
	
	/**
	 * 同步角色金币
	 * @param session
	 * @param gold
	 */
	public static void syncRoleGold(int playerId) {
		Player player = UserMemory.getInstance().getPlayer(playerId);
		if (player == null) {
			return;
		}
		ISession session = SessionMemory.getInstance().getSession(playerId);
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		data.writeByte(HallMsgModuleConst.ROLE_RESPONSE);
		data.writeByte(RoleMsgConst.ROLE_GOLD_RES);
		
		data.writeInt(player.getGold());
		
		session.send(data);
	}
	
	public static void syncRoleGold(ISession session, int gold) {
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		data.writeByte(HallMsgModuleConst.ROLE_RESPONSE);
		data.writeByte(RoleMsgConst.ROLE_GOLD_RES);
		
		data.writeInt(gold);
		
		session.send(data);
	}
	
	/**
	 * 同步角色剩余次数
	 * 
	 * @param session
	 * @param resCount
	 */
	public static void syncRoleResCount(ISession session, HashMap<Integer, Integer> resCount) {
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		data.writeByte(HallMsgModuleConst.ROLE_RESPONSE);
		data.writeByte(RoleMsgConst.ROLE_RES_COUNT);
		
		HashMap<Integer, Integer> useCountModels = ConfigData.useCountModels;
		data.writeInt(useCountModels.size());
		Iterator<Entry<Integer, Integer>> iterator = useCountModels.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Integer, Integer> next = iterator.next();
			Integer type = next.getKey();
			Integer value = next.getValue();
			Integer useCount = resCount.get(type);
			if (useCount != null) {
				value = value - useCount;
				value = value < 0 ? 0 : value;
			}
			data.writeInt(type);
			data.writeInt(value);
		}
		session.send(data);
	}

	/**
	 * 同步角色剩余次数
	 * 
	 * @param session
	 * @param type
	 * @param resCount
	 */
	public static void syncRoleResCount(ISession session, int type, int resCount) {
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		data.writeByte(HallMsgModuleConst.ROLE_RESPONSE);
		data.writeByte(RoleMsgConst.ROLE_RES_COUNT);
		
		data.writeInt(1);
		data.writeInt(type);
		data.writeInt(resCount);
		
		session.send(data);
	}

	public static void changeIconRes(ISession session, boolean result, String icon) {
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		data.writeByte(HallMsgModuleConst.ROLE_RESPONSE);
		data.writeByte(RoleMsgConst.CHANGE_ICON_RES);
		
		data.writeBoolean(result);
		data.writeUTF(icon);
		
		session.send(data);
	}
	
	public static void syncRoleGuideId(ISession session, int guideId) {
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		data.writeByte(HallMsgModuleConst.ROLE_RESPONSE);
		data.writeByte(RoleMsgConst.GUIDE_ID_SYNC);
		
		data.writeByte(guideId);
		
		session.send(data);
	}
	
}
