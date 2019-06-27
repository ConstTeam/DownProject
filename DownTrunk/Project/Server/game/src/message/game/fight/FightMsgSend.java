package message.game.fight;

import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.ConfigData;
import message.game.GameMsgModuleConst;
import message.hall.S2CMessageSend;
import module.fight.BattleRole;
import module.scene.GameRoom;
import net.ByteBufferFactory;
import net.IByteBuffer;
import net.ISession;
import sys.GameSyncManager;
import util.Tools;

/**
 * 战斗模块消息发送
 *
 */
public class FightMsgSend {
	
	private static final Logger logger = LoggerFactory.getLogger(FightMsgSend.class);
	
	public static void messageBox(ISession session, String message) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.MSG_BOX);

		data.writeUTF(message);

		session.send(data);
	}

	public static void messageBox(ISession session, int id) {
		if (session == null || session.isClosed()) {
			return;
		}
		String message = ConfigData.messageBox.get(id);
		if (Tools.isEmptyString(message)) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.MSG_BOX);

		data.writeUTF(message);

		session.send(data);
	}
	
	public static void intoRoom(ISession session, int playerId, GameRoom room) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.INTO_ROOM);

		data.writeInt(room.getRoomId());
		data.writeInt(room.getSeed());
		data.writeByte(GameSyncManager.INTERVAL);
		
		data.writeInt(100);
		
		Collection<BattleRole> values = room.getBattleRoles().values();
		data.writeByte(values.size() - 1);
		for (BattleRole role : values) {
			if (role.getPlayerId() == playerId) {
				continue;
			}
			data.writeInt(role.getPlayerId());
			data.writeUTF(role.getNickname());
			data.writeByte(role.getSceneId());
			data.writeByte(role.getRoleId());
		}
		
		session.send(data);
	}
	
	public static void startGame(Collection<ISession> sessions) {
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.START_GAME);
		
		S2CMessageSend.sendMultiMessage(sessions, data);
		logger.debug("{} - {}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.START_GAME);
	}
	
	public static void hpSync(Collection<ISession> sessions, ISession exSession, int playerId, int hp) {
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.HP_SYNC);
		
		data.writeInt(playerId);
		data.writeByte(hp);
		
		S2CMessageSend.sendMultiMessage(sessions, data, exSession);
		logger.debug("{} - {}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.HP_SYNC);
	}

	public static void itemSync(Collection<ISession> sessions, ISession exSession, int playerId, int itemId) {
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.GET_ITEM_SYNC);
		
		data.writeInt(playerId);
		data.writeByte(itemId);
		
		S2CMessageSend.sendMultiMessage(sessions, data, exSession);
		logger.debug("{} - {}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.GET_ITEM_SYNC);
	}

	public static void useItemSync(Collection<ISession> sessions, int playerId, int targetId, int itemId, boolean mainSkill) {
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.USE_ITEM_SYNC);

		data.writeInt(playerId);
		data.writeInt(targetId);
		data.writeByte(itemId);
		data.writeBoolean(mainSkill);
		
		S2CMessageSend.sendMultiMessage(sessions, data);
		logger.debug("{} - {}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.USE_ITEM_SYNC);
	}

	public static void settlement(ISession session, int playerId, boolean isWin) {
		if (session == null || session.isClosed()) {
			return;
		}
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.SETTLEMENT);

		data.writeBoolean(isWin);
		
		session.send(data);
		logger.debug("{} - {} playerId:{} isWin:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.SETTLEMENT, playerId, isWin);
	}

	public static void heroDiedSync(Collection<ISession> sessions, int playerId) {
		IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
		
		data.writeByte(GameMsgModuleConst.FIGHT_RESPONSE);
		data.writeByte(FightMsgConst.HERO_DIED_SYNC);

		data.writeInt(playerId);
		
		S2CMessageSend.sendMultiMessage(sessions, data);
		logger.debug("{} - {} playerId:{}", GameMsgModuleConst.FIGHT_RESPONSE, FightMsgConst.HERO_DIED_SYNC, playerId);
	}
}