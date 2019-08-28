package message.game.fight;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import message.hall.login.LoginService;
import module.fight.BattleRole;
import module.scene.GameRoom;
import net.DataAccessException;
import net.IByteBuffer;
import net.ISession;
import net.Servicelet;
import redis.data.PlayerInfo;
import sys.GameRoomManager;

/**
 * ս��ģ��
 * 
 */
public class FightService extends Servicelet {

	private static final Logger logger = LoggerFactory.getLogger(FightService.class);

	@Override
	public void access(ISession session, IByteBuffer data) throws DataAccessException {
		int type = data.readByte();
		String deviceId = null;
		int playerId = 0;

		switch (type) {
		case FightMsgConst.READY:
			ready(session, deviceId, playerId);
			break;
		case FightMsgConst.ROLE_HP:
			int hp = data.readByte();
			roleHp(session, deviceId, playerId, hp);
			break;
		case FightMsgConst.GET_ITEM:
			int itemId = data.readByte();
			getItem(session, deviceId, playerId, itemId);
			break;
		case FightMsgConst.USE_ITEM:
			int targetId = data.readInt();
			itemId = data.readByte();
			boolean mainSkill = data.readBoolean();
			useItem(session, deviceId, playerId, targetId, itemId, mainSkill);
			break;
		case FightMsgConst.HERO_DIED:
			int deadPlayerId = data.readInt();
			heroDied(session, deviceId, playerId, deadPlayerId);
			break;
		}
	}

	private void ready(ISession session, String deviceId, int playerId) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session��player��Ϣ��ʧЧ��");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("��ң�{}��׼��ʧ�ܡ���ȡ�����Ϣʧ�ܡ�", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("��ң�{}��׼��ʧ�ܡ�δ����Ϸ�����ڡ�", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("��ң�{}������Id��{}��׼��ʧ�ܡ���Ϸ���䲻���ڡ�", playerId, roomId);
				return;
			}
			if (!room.isInRoom(playerId)) {
				logger.error("��ң�{}������Id��{}��׼��ʧ�ܡ�δ����Ϸ�����ڡ�", playerId, roomId);
				return;
			}
			room.ready(playerId);
			logger.info("��ң�{}������Id��{}��׼���ɹ���", playerId, roomId);
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}

	private void roleHp(ISession session, String deviceId, int playerId, int hp) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session��player��Ϣ��ʧЧ��");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("��ң�{}��ͬ��Ѫ��ʧ�ܡ���ȡ�����Ϣʧ�ܡ�", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("��ң�{}��ͬ��Ѫ��ʧ�ܡ�δ����Ϸ�����ڡ�", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("��ң�{}������Id��{}��ͬ��Ѫ��ʧ�ܡ���Ϸ���䲻���ڡ�", playerId, roomId);
				return;
			}
			if (!room.isInRoom(playerId)) {
				logger.error("��ң�{}������Id��{}��ͬ��Ѫ��ʧ�ܡ�δ����Ϸ�����ڡ�", playerId, roomId);
				return;
			}
			room.roleHpSync(session, playerId, hp);
			logger.info("��ң�{}������Id��{}��Ѫ����{}��ͬ��Ѫ���ɹ���", playerId, roomId, hp);
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}

	private void getItem(ISession session, String deviceId, int playerId, int itemId) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session��player��Ϣ��ʧЧ��");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("��ң�{}����ȡ����ʧ�ܡ���ȡ�����Ϣʧ�ܡ�", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("��ң�{}����ȡ����ʧ�ܡ�δ����Ϸ�����ڡ�", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("��ң�{}������Id��{}����ȡ����ʧ�ܡ���Ϸ���䲻���ڡ�", playerId, roomId);
				return;
			}
			if (!room.isInRoom(playerId)) {
				logger.error("��ң�{}������Id��{}����ȡ����ʧ�ܡ�δ����Ϸ�����ڡ�", playerId, roomId);
				return;
			}
			room.itemSync(session, playerId, itemId);
			logger.info("��ң�{}������Id��{}�����ߣ�{}����ȡ���߳ɹ���", playerId, roomId, itemId);
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}
	
	private void useItem(ISession session, String deviceId, int playerId, int targetId, int itemId, boolean mainSkill) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session��player��Ϣ��ʧЧ��");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("��ң�{}��ʹ�õ���ʧ�ܡ���ȡ�����Ϣʧ�ܡ�", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("��ң�{}��ʹ�õ���ʧ�ܡ�δ����Ϸ�����ڡ�", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("��ң�{}������Id��{}��ʹ�õ���ʧ�ܡ���Ϸ���䲻���ڡ�", playerId, roomId);
				return;
			}
			if (!room.isInRoom(playerId)) {
				logger.error("��ң�{}������Id��{}��ʹ�õ���ʧ�ܡ�δ����Ϸ�����ڡ�", playerId, roomId);
				return;
			}
			room.useItemSync(playerId, targetId, itemId, mainSkill);
			logger.info("��ң�{}������Id��{}��Ŀ����ң�{}�����ߣ�{}��ʹ�õ��߳ɹ���", playerId, roomId, targetId, itemId);
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}
	
	private void heroDied(ISession session, String deviceId, int playerId, int deadPlayerId) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session��player��Ϣ��ʧЧ��");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("��ң�{}��Ӣ������ʧ�ܡ���ȡ�����Ϣʧ�ܡ�", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("��ң�{}��Ӣ������ʧ�ܡ�δ����Ϸ�����ڡ�", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("��ң�{}������Id��{}��Ӣ������ʧ�ܡ���Ϸ���䲻���ڡ�", playerId, roomId);
				return;
			}
			BattleRole role = room.getBattleRole(deadPlayerId);
			if (role == null) {
				logger.error("��ң�{}������Id��{}��Ӣ������ʧ�ܡ�δ����Ϸ�����ڡ�", playerId, roomId);
				return;
			}
			if (!role.isRobot() && playerId != deadPlayerId) {
				logger.error("��ң�{}������Id��{}��Ӣ������ʧ�ܡ����ѷ�Ӣ���������������Id��{}��", playerId, roomId, deadPlayerId);
				return;
			}
			room.heroDied(role);
			logger.info("��ң�{}������Id��{}��Ӣ���������������Id��{}��", playerId, roomId, deadPlayerId);
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}
}