package message.game.fight;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.model.card.CardModel;
import message.hall.login.LoginService;
import module.area.Area;
import module.fight.BattleRole;
import module.scene.GameRoom;
import module.scene.SelectObject;
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
			
		case FightMsgConst.END_TURN:
			endTurn(session, deviceId, playerId);
			break;
			
		case FightMsgConst.TROOP_CARD_PLAY:
			int cardUid = data.readInt();
			int areaIndex = data.readByte();
			int mainRowIndex = data.readByte();
			SelectObject sobj = SelectObject.spellCardPlaySelectObj(data);
			playCard(session, deviceId, playerId, cardUid, areaIndex, mainRowIndex, sobj, CardModel.TROOP);
			break;

		case FightMsgConst.ATTACT:
			int attCardId = data.readInt();
			int defCardId = data.readInt();
			attack(session, deviceId, playerId, attCardId, defCardId);
			break;

		case FightMsgConst.AREA_LV_UP:
			areaIndex = data.readByte();
			areaLvUp(session, deviceId, playerId, areaIndex);
			break;
			
		case FightMsgConst.SPELL_CARD_PLAY:
			cardUid = data.readInt();
			sobj = SelectObject.spellCardPlaySelectObj(data);
			playCardSpell(session, deviceId, playerId, cardUid, sobj);
			break;
			
		case FightMsgConst.ARTI_CARD_PLAY:
			cardUid = data.readInt();
			areaIndex = data.readByte();
			mainRowIndex = data.readByte();
			playCard(session, deviceId, playerId, cardUid, areaIndex, mainRowIndex, null, CardModel.ARTIFACT);
			break;

		case FightMsgConst.REPLACE_FIRST_DEAL:
			ArrayList<Boolean> isReplace = new ArrayList<>();
			for (int i = 0; i < 3; i++) {
				isReplace.add(data.readBoolean());
			}
			replaceFirstDeal(session, deviceId, playerId, isReplace);
			break;
			
		case FightMsgConst.FIND_SELECT:
			int index = data.readByte();
			findCardSelect(session, deviceId, playerId, index);
			break;
			
		case FightMsgConst.CHECK_CARD_SELECT:
			boolean select = data.readBoolean();
			checkCardSelect(session, deviceId, playerId, select);
			break;
		case FightMsgConst.GIVE_UP:
			giveUp(session, deviceId, playerId);
			break;
		case FightMsgConst.CONSTRUCT_TRAP:
			boolean isOutput = data.readBoolean();
			cardUid = data.readInt();
			constructTrapSelect(session, deviceId, playerId, isOutput, cardUid);
			break;
		case FightMsgConst.SUMMON_TROOP:
			cardUid = data.readInt();
			summonTroop(session, deviceId, playerId, cardUid);
			break;
		case FightMsgConst.TAP_CARD:
			tapCardSync(session, deviceId, playerId, data);
			break;
		case FightMsgConst.TARGET_SELECT:
			targetSelect(session, deviceId, playerId, data);
			break;
		case FightMsgConst.DRAW_CARD:
			drawCard(session, deviceId, playerId);
			break;
		case FightMsgConst.OTHER:
			other(session, deviceId, playerId, data);
			break;
		}

	}

	@SuppressWarnings("unused")
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
			logger.error("��ң�{}�����ֽ�������ʧ�ܡ���ȡ�����Ϣʧ�ܡ�", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("��ң�{}�����ֽ�������ʧ�ܡ�δ����Ϸ�����ڡ�", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("��ң�{}������Id��{}�����ֽ�������ʧ�ܡ���Ϸ���䲻���ڡ�", playerId, roomId);
				return;
			}
			if (!room.isInRoom(playerId)) {
				logger.error("��ң�{}������Id��{}�����ֽ�������ʧ�ܡ�δ����Ϸ�����ڡ�", playerId, roomId);
				return;
			}
			room.ready(playerId);
			logger.info("��ң�{}������Id��{}�����ֽ������Ƴɹ���", playerId, roomId);
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}

	private void endTurn(ISession session, String deviceId, int playerId) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session��player��Ϣ��ʧЧ��");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("��ң�{}�������غ�ʧ�ܡ���ȡ�����Ϣʧ�ܡ�", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("��ң�{}�������غ�ʧ�ܡ�δ����Ϸ�����ڡ�", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("��ң�{}������Id��{}�������غ�ʧ�ܡ���Ϸ���䲻���ڡ�", playerId, roomId);
				return;
			}
			if (!room.checkPlayer(playerId)) {
				logger.info("��ң�{}������Id��{}�������غ�ʧ�ܡ�δ�ڵ�ǰ��һغ��ڡ�", playerInfo.getPlayerId(), roomId);
				return;
			}
			
			room.turnFinish(playerId);
			logger.info("��ң�{}������Id��{}�������غϳɹ���", playerId, roomId);
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}

	private void playCardSpell(ISession session, String deviceId, int playerId, int cardUid, SelectObject sobj) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session��player��Ϣ��ʧЧ��");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("��ң�{}���������ʧ�ܡ���ȡ�����Ϣʧ�ܡ�", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("��ң�{}���������ʧ�ܡ�δ����Ϸ�����ڡ�", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("��ң�{}������Id��{}���������ʧ�ܡ���Ϸ���䲻���ڡ�", playerId, roomId);
				return;
			}
			if (!room.checkPlayer(playerId)) {
				logger.info("��ң�{}������Id��{}���������ʧ�ܡ�δ�ڵ�ǰ��һغ��ڡ�", playerInfo.getPlayerId(), roomId);
				return;
			}
			room.playCardSpell(playerId, cardUid, sobj);
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}

	private void playCard(ISession session, String deviceId, int playerId, int cardUid, int areaIndex, int mainRowIndex, SelectObject sobj, int type) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session��player��Ϣ��ʧЧ��");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("��ң�{}���������ʧ�ܡ���ȡ�����Ϣʧ�ܡ�", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("��ң�{}���������ʧ�ܡ�δ����Ϸ�����ڡ�", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("��ң�{}������Id��{}���������ʧ�ܡ���Ϸ���䲻���ڡ�", playerId, roomId);
				return;
			}
			if (!room.checkPlayer(playerId)) {
				logger.info("��ң�{}������Id��{}���������ʧ�ܡ�δ�ڵ�ǰ��һغ��ڡ�", playerInfo.getPlayerId(), roomId);
				return;
			}
			if (areaIndex < 0 || areaIndex > BattleRole.AREA_MAX_INDEX) {
				logger.info("��ң�{}������Id��{}���������ʧ�ܡ�����Index��{}������", playerInfo.getPlayerId(), roomId, areaIndex);
				return;
			}

			room.playCard(playerId, type, cardUid, areaIndex, mainRowIndex, sobj);
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}

	private void attack(ISession session, String deviceId, int playerId, int attCardId, int defCardId) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session��player��Ϣ��ʧЧ��");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("��ң�{}������ʧ�ܡ���ȡ�����Ϣʧ�ܡ�", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("��ң�{}������ʧ�ܡ�δ����Ϸ�����ڡ�", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("��ң�{}������Id��{}������ʧ�ܡ���Ϸ���䲻���ڡ�", playerId, roomId);
				return;
			}
			if (!room.checkPlayer(playerId)) {
				logger.info("��ң�{}������Id��{}������ʧ�ܡ�δ�ڵ�ǰ��һغ��ڡ�", playerInfo.getPlayerId(), roomId);
				return;
			}
			
			boolean result = room.attack(playerId, attCardId, defCardId);
			logger.info("��ң�{}������Id��{}������{}��", playerId, roomId, result ? "�ɹ�" : "ʧ��");
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}

	private void areaLvUp(ISession session, String deviceId, int playerId, int areaIndex) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session��player��Ϣ��ʧЧ��");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("��ң�{}����������ʧ�ܡ���ȡ�����Ϣʧ�ܡ�", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("��ң�{}����������ʧ�ܡ�δ����Ϸ�����ڡ�", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("��ң�{}������Id��{}������Index��{}����������ʧ�ܡ���Ϸ���䲻���ڡ�", playerId, roomId, areaIndex);
				return;
			}
			if (!room.checkPlayer(playerId)) {
				logger.info("��ң�{}������Id��{}������Index��{}����������ʧ�ܡ�δ�ڵ�ǰ��һغ��ڡ�", playerInfo.getPlayerId(), roomId, areaIndex);
				return;
			}
			if (areaIndex < 0 || areaIndex >= Area.ROW_MAX_INDEX) {
				logger.info("��ң�{}������Id��{}������Index��{}����������ʧ�ܣ�����Index���󣺡�", playerInfo.getPlayerId(), roomId, areaIndex);
				return;
			}
			
			boolean result = room.areaLvUp(playerId, areaIndex);
			logger.info("��ң�{}������Id��{}������Index��{}����������{}��", playerId, roomId, areaIndex, result ? "�ɹ�" : "ʧ��");
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}
	
	private void replaceFirstDeal(ISession session, String deviceId, int playerId, ArrayList<Boolean> isReplace) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session��player��Ϣ��ʧЧ��");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("��ң�{}�����ֻ���ʧ�ܡ���ȡ�����Ϣʧ�ܡ�", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("��ң�{}�����ֻ���ʧ�ܡ�δ����Ϸ�����ڡ�", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("��ң�{}������Id��{}�����ֻ���ʧ�ܡ���Ϸ���䲻���ڡ�", playerId, roomId);
				return;
			}
			if (!room.isInRoom(playerId)) {
				logger.error("��ң�{}������Id��{}�����ֻ���ʧ�ܡ�δ����Ϸ�����ڡ�", playerId, roomId);
				return;
			}
			boolean result = room.replaceDealCard(playerId, isReplace);
			logger.info("��ң�{}������Id��{}������Index��{}�����ֻ���{}��", playerId, roomId, isReplace, result ? "�ɹ�" : "ʧ��");
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}
	
	private void findCardSelect(ISession session, String deviceId, int playerId, int index) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session��player��Ϣ��ʧЧ��");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("��ң�{}������ѡ����ʧ�ܡ���ȡ�����Ϣʧ�ܡ�", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("��ң�{}������ѡ����ʧ�ܡ�δ����Ϸ�����ڡ�", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("��ң�{}������Id��{}������ѡ����ʧ�ܡ���Ϸ���䲻���ڡ�", playerId, roomId);
				return;
			}
			if (!room.isInRoom(playerId)) {
				logger.error("��ң�{}������Id��{}������ѡ����ʧ�ܡ�δ����Ϸ�����ڡ�", playerId, roomId);
				return;
			}
			if (index < 0) {
				logger.error("��ң�{}������Id��{}��ѡ��Index��{}������ѡ����ʧ�ܡ�index����", playerId, roomId, index);
				return;
			}
			boolean result = room.findCardSelect(playerId, index);
			logger.info("��ң�{}������Id��{}��ѡ��Index��{}������ѡ����{}��", playerId, roomId, index, result ? "�ɹ�" : "ʧ��");
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}
	
	private void checkCardSelect(ISession session, String deviceId, int playerId, boolean select) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session��player��Ϣ��ʧЧ��");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("��ң�{}���鿴ѡ��ʧ�ܡ���ȡ�����Ϣʧ�ܡ�", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("��ң�{}���鿴ѡ��ʧ�ܡ�δ����Ϸ�����ڡ�", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("��ң�{}������Id��{}���鿴ѡ��ʧ�ܡ���Ϸ���䲻���ڡ�", playerId, roomId);
				return;
			}
			if (!room.isInRoom(playerId)) {
				logger.error("��ң�{}������Id��{}���鿴ѡ��ʧ�ܡ�δ����Ϸ�����ڡ�", playerId, roomId);
				return;
			}
			
			boolean result = room.checkCardSelect(playerId, select);
			logger.info("��ң�{}������Id��{}���鿴ѡ������ƿ�ף�{}���鿴ѡ��{}��", playerId, roomId, select, result ? "�ɹ�" : "ʧ��");
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}
	
	private void giveUp(ISession session, String deviceId, int playerId) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session��player��Ϣ��ʧЧ��");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("��ң�{}������ʧ�ܡ���ȡ�����Ϣʧ�ܡ�", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("��ң�{}������ʧ�ܡ�δ����Ϸ�����ڡ�", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("��ң�{}������Id��{}������ʧ�ܡ���Ϸ���䲻���ڡ�", playerId, roomId);
				return;
			}
			if (!room.isInRoom(playerId)) {
				logger.error("��ң�{}������Id��{}������ʧ�ܡ�δ����Ϸ�����ڡ�", playerId, roomId);
				return;
			}
			
			room.giveup(playerId);
			logger.info("��ң�{}������Id��{}�����䡣", playerId, roomId);
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}

	private void constructTrapSelect(ISession session, String deviceId, int playerId, boolean isOutput, int cardUid) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session��player��Ϣ��ʧЧ��");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("��ң�{}����װ����ʧ�ܡ���ȡ�����Ϣʧ�ܡ�", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("��ң�{}����װ����ʧ�ܡ�δ����Ϸ�����ڡ�", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("��ң�{}������Id��{}����װ����ʧ�ܡ���Ϸ���䲻���ڡ�", playerId, roomId);
				return;
			}
			if (!room.isInRoom(playerId)) {
				logger.error("��ң�{}������Id��{}����װ����ʧ�ܡ�δ����Ϸ�����ڡ�", playerId, roomId);
				return;
			}
			
			room.constructTrapCardSelect(playerId, cardUid);
			logger.info("��ң�{}������Id��{}����װ���塣", playerId, roomId);
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}

	private void summonTroop(ISession session, String deviceId, int playerId, int cardUid) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session��player��Ϣ��ʧЧ��");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("��ң�{}���ٻ��滻����ʧ�ܡ���ȡ�����Ϣʧ�ܡ�", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("��ң�{}���ٻ��滻����ʧ�ܡ�δ����Ϸ�����ڡ�", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("��ң�{}������Id��{}���ٻ��滻����ʧ�ܡ���Ϸ���䲻���ڡ�", playerId, roomId);
				return;
			}
			if (!room.isInRoom(playerId)) {
				logger.error("��ң�{}������Id��{}���ٻ��滻����ʧ�ܡ�δ����Ϸ�����ڡ�", playerId, roomId);
				return;
			}
			
			BattleRole battleRole = room.getBattleRole(playerId);
			room.summonReplaceCard(battleRole, cardUid);
			
			logger.info("��ң�{}������Id��{}������Uid��{}���ٻ��滻���ơ�", playerId, roomId, cardUid);
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}

	private void tapCardSync(ISession session, String deviceId, int playerId, IByteBuffer data) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session��player��Ϣ��ʧЧ��");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("��ң�{}��ͬ��������Ϣʧ�ܡ���ȡ�����Ϣʧ�ܡ�", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("��ң�{}��ͬ������ʧ�ܡ�δ����Ϸ�����ڡ�", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("��ң�{}������Id��{}��ͬ������ʧ�ܡ���Ϸ���䲻���ڡ�", playerId, roomId);
				return;
			}
			if (!room.isInRoom(playerId)) {
				logger.error("��ң�{}������Id��{}��ͬ������ʧ�ܡ�δ����Ϸ�����ڡ�", playerId, roomId);
				return;
			}
			
			int enemyId = room.getEnemyId(playerId);
			FightMsgSend.tapCardSync(room.getSession(enemyId), data);
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}

	private void targetSelect(ISession session, String deviceId, int playerId, IByteBuffer data) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session��player��Ϣ��ʧЧ��");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("��ң�{}��ѡ��Ŀ��ʧ�ܡ���ȡ�����Ϣʧ�ܡ�", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("��ң�{}��ѡ��Ŀ��ʧ�ܡ�δ����Ϸ�����ڡ�", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("��ң�{}������Id��{}��ѡ��Ŀ��ʧ�ܡ���Ϸ���䲻���ڡ�", playerId, roomId);
				return;
			}
			if (!room.isInRoom(playerId)) {
				logger.error("��ң�{}������Id��{}��ѡ��Ŀ��ʧ�ܡ�δ����Ϸ�����ڡ�", playerId, roomId);
				return;
			}
			SelectObject sobj = SelectObject.spellCardPlaySelectObj(data);
			room.targetSelect(playerId, sobj);
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}

	private void drawCard(ISession session, String deviceId, int playerId) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session��player��Ϣ��ʧЧ��");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("��ң�{}������ʧ�ܡ���ȡ�����Ϣʧ�ܡ�", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("��ң�{}������ʧ�ܡ�δ����Ϸ�����ڡ�", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("��ң�{}������Id��{}������ʧ�ܡ���Ϸ���䲻���ڡ�", playerId, roomId);
				return;
			}
			if (!room.isInRoom(playerId)) {
				logger.error("��ң�{}������Id��{}������ʧ�ܡ�δ����Ϸ�����ڡ�", playerId, roomId);
				return;
			}
			boolean result = room.drawCard(playerId);
			if (result) {
				logger.info("��ң�{}������Id��{}�����Ƴɹ���", playerId, roomId);
			} else {
				logger.info("��ң�{}������Id��{}������ʧ�ܡ�", playerId, roomId);
			}
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}

	private void other(ISession session, String deviceId, int playerId, IByteBuffer data) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session��player��Ϣ��ʧЧ��");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("��ң�{}��ͬ��������Ϣʧ�ܡ���ȡ�����Ϣʧ�ܡ�", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("��ң�{}��ͬ������ʧ�ܡ�δ����Ϸ�����ڡ�", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("��ң�{}������Id��{}��ͬ������ʧ�ܡ���Ϸ���䲻���ڡ�", playerId, roomId);
				return;
			}
			if (!room.isInRoom(playerId)) {
				logger.error("��ң�{}������Id��{}��ͬ������ʧ�ܡ�δ����Ϸ�����ڡ�", playerId, roomId);
				return;
			}
			
			int enemyId = room.getEnemyId(playerId);
			FightMsgSend.other(room.getSession(enemyId), data);
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}
}