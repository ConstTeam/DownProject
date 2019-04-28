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
 * 战斗模块
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
			logger.error("session中player信息已失效。");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("玩家：{}，开局结束换牌失败。获取玩家信息失败。", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("玩家：{}，开局结束换牌失败。未在游戏房间内。", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("玩家：{}，房间Id：{}，开局结束换牌失败。游戏房间不存在。", playerId, roomId);
				return;
			}
			if (!room.isInRoom(playerId)) {
				logger.error("玩家：{}，房间Id：{}，开局结束换牌失败。未在游戏房间内。", playerId, roomId);
				return;
			}
			room.ready(playerId);
			logger.info("玩家：{}，房间Id：{}，开局结束换牌成功。", playerId, roomId);
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}

	private void endTurn(ISession session, String deviceId, int playerId) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session中player信息已失效。");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("玩家：{}，结束回合失败。获取玩家信息失败。", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("玩家：{}，结束回合失败。未在游戏房间内。", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("玩家：{}，房间Id：{}，结束回合失败。游戏房间不存在。", playerId, roomId);
				return;
			}
			if (!room.checkPlayer(playerId)) {
				logger.info("玩家：{}，房间Id：{}，结束回合失败。未在当前玩家回合内。", playerInfo.getPlayerId(), roomId);
				return;
			}
			
			room.turnFinish(playerId);
			logger.info("玩家：{}，房间Id：{}，结束回合成功。", playerId, roomId);
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}

	private void playCardSpell(ISession session, String deviceId, int playerId, int cardUid, SelectObject sobj) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session中player信息已失效。");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("玩家：{}，打出手牌失败。获取玩家信息失败。", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("玩家：{}，打出手牌失败。未在游戏房间内。", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("玩家：{}，房间Id：{}，打出手牌失败。游戏房间不存在。", playerId, roomId);
				return;
			}
			if (!room.checkPlayer(playerId)) {
				logger.info("玩家：{}，房间Id：{}，打出手牌失败。未在当前玩家回合内。", playerInfo.getPlayerId(), roomId);
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
			logger.error("session中player信息已失效。");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("玩家：{}，打出手牌失败。获取玩家信息失败。", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("玩家：{}，打出手牌失败。未在游戏房间内。", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("玩家：{}，房间Id：{}，打出手牌失败。游戏房间不存在。", playerId, roomId);
				return;
			}
			if (!room.checkPlayer(playerId)) {
				logger.info("玩家：{}，房间Id：{}，打出手牌失败。未在当前玩家回合内。", playerInfo.getPlayerId(), roomId);
				return;
			}
			if (areaIndex < 0 || areaIndex > BattleRole.AREA_MAX_INDEX) {
				logger.info("玩家：{}，房间Id：{}，打出手牌失败。区域Index：{}，有误。", playerInfo.getPlayerId(), roomId, areaIndex);
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
			logger.error("session中player信息已失效。");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("玩家：{}，攻击失败。获取玩家信息失败。", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("玩家：{}，攻击失败。未在游戏房间内。", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("玩家：{}，房间Id：{}，攻击失败。游戏房间不存在。", playerId, roomId);
				return;
			}
			if (!room.checkPlayer(playerId)) {
				logger.info("玩家：{}，房间Id：{}，攻击失败。未在当前玩家回合内。", playerInfo.getPlayerId(), roomId);
				return;
			}
			
			boolean result = room.attack(playerId, attCardId, defCardId);
			logger.info("玩家：{}，房间Id：{}，攻击{}。", playerId, roomId, result ? "成功" : "失败");
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}

	private void areaLvUp(ISession session, String deviceId, int playerId, int areaIndex) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session中player信息已失效。");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("玩家：{}，升级区域失败。获取玩家信息失败。", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("玩家：{}，升级区域失败。未在游戏房间内。", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("玩家：{}，房间Id：{}，区域Index：{}，升级区域失败。游戏房间不存在。", playerId, roomId, areaIndex);
				return;
			}
			if (!room.checkPlayer(playerId)) {
				logger.info("玩家：{}，房间Id：{}，区域Index：{}，升级区域失败。未在当前玩家回合内。", playerInfo.getPlayerId(), roomId, areaIndex);
				return;
			}
			if (areaIndex < 0 || areaIndex >= Area.ROW_MAX_INDEX) {
				logger.info("玩家：{}，房间Id：{}，区域Index：{}，升级区域失败，区域Index有误：。", playerInfo.getPlayerId(), roomId, areaIndex);
				return;
			}
			
			boolean result = room.areaLvUp(playerId, areaIndex);
			logger.info("玩家：{}，房间Id：{}，区域Index：{}，升级区域{}。", playerId, roomId, areaIndex, result ? "成功" : "失败");
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}
	
	private void replaceFirstDeal(ISession session, String deviceId, int playerId, ArrayList<Boolean> isReplace) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session中player信息已失效。");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("玩家：{}，开局换牌失败。获取玩家信息失败。", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("玩家：{}，开局换牌失败。未在游戏房间内。", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("玩家：{}，房间Id：{}，开局换牌失败。游戏房间不存在。", playerId, roomId);
				return;
			}
			if (!room.isInRoom(playerId)) {
				logger.error("玩家：{}，房间Id：{}，开局换牌失败。未在游戏房间内。", playerId, roomId);
				return;
			}
			boolean result = room.replaceDealCard(playerId, isReplace);
			logger.info("玩家：{}，房间Id：{}，换牌Index：{}，开局换牌{}。", playerId, roomId, isReplace, result ? "成功" : "失败");
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}
	
	private void findCardSelect(ISession session, String deviceId, int playerId, int index) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session中player信息已失效。");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("玩家：{}，发现选择卡牌失败。获取玩家信息失败。", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("玩家：{}，发现选择卡牌失败。未在游戏房间内。", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("玩家：{}，房间Id：{}，发现选择卡牌失败。游戏房间不存在。", playerId, roomId);
				return;
			}
			if (!room.isInRoom(playerId)) {
				logger.error("玩家：{}，房间Id：{}，发现选择卡牌失败。未在游戏房间内。", playerId, roomId);
				return;
			}
			if (index < 0) {
				logger.error("玩家：{}，房间Id：{}，选牌Index：{}，发现选择卡牌失败。index有误。", playerId, roomId, index);
				return;
			}
			boolean result = room.findCardSelect(playerId, index);
			logger.info("玩家：{}，房间Id：{}，选牌Index：{}，发现选择卡牌{}。", playerId, roomId, index, result ? "成功" : "失败");
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}
	
	private void checkCardSelect(ISession session, String deviceId, int playerId, boolean select) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session中player信息已失效。");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("玩家：{}，查看选择失败。获取玩家信息失败。", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("玩家：{}，查看选择失败。未在游戏房间内。", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("玩家：{}，房间Id：{}，查看选择失败。游戏房间不存在。", playerId, roomId);
				return;
			}
			if (!room.isInRoom(playerId)) {
				logger.error("玩家：{}，房间Id：{}，查看选择失败。未在游戏房间内。", playerId, roomId);
				return;
			}
			
			boolean result = room.checkCardSelect(playerId, select);
			logger.info("玩家：{}，房间Id：{}，查看选择放入牌库底：{}，查看选择{}。", playerId, roomId, select, result ? "成功" : "失败");
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}
	
	private void giveUp(ISession session, String deviceId, int playerId) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session中player信息已失效。");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("玩家：{}，认输失败。获取玩家信息失败。", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("玩家：{}，认输失败。未在游戏房间内。", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("玩家：{}，房间Id：{}，认输失败。游戏房间不存在。", playerId, roomId);
				return;
			}
			if (!room.isInRoom(playerId)) {
				logger.error("玩家：{}，房间Id：{}，认输失败。未在游戏房间内。", playerId, roomId);
				return;
			}
			
			room.giveup(playerId);
			logger.info("玩家：{}，房间Id：{}，认输。", playerId, roomId);
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}

	private void constructTrapSelect(ISession session, String deviceId, int playerId, boolean isOutput, int cardUid) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session中player信息已失效。");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("玩家：{}，组装陷阱失败。获取玩家信息失败。", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("玩家：{}，组装陷阱失败。未在游戏房间内。", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("玩家：{}，房间Id：{}，组装陷阱失败。游戏房间不存在。", playerId, roomId);
				return;
			}
			if (!room.isInRoom(playerId)) {
				logger.error("玩家：{}，房间Id：{}，组装陷阱失败。未在游戏房间内。", playerId, roomId);
				return;
			}
			
			room.constructTrapCardSelect(playerId, cardUid);
			logger.info("玩家：{}，房间Id：{}，组装陷阱。", playerId, roomId);
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}

	private void summonTroop(ISession session, String deviceId, int playerId, int cardUid) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session中player信息已失效。");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("玩家：{}，召唤替换卡牌失败。获取玩家信息失败。", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("玩家：{}，召唤替换卡牌失败。未在游戏房间内。", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("玩家：{}，房间Id：{}，召唤替换卡牌失败。游戏房间不存在。", playerId, roomId);
				return;
			}
			if (!room.isInRoom(playerId)) {
				logger.error("玩家：{}，房间Id：{}，召唤替换卡牌失败。未在游戏房间内。", playerId, roomId);
				return;
			}
			
			BattleRole battleRole = room.getBattleRole(playerId);
			room.summonReplaceCard(battleRole, cardUid);
			
			logger.info("玩家：{}，房间Id：{}，卡牌Uid：{}，召唤替换卡牌。", playerId, roomId, cardUid);
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}

	private void tapCardSync(ISession session, String deviceId, int playerId, IByteBuffer data) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session中player信息已失效。");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("玩家：{}，同步触牌信息失败。获取玩家信息失败。", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("玩家：{}，同步触牌失败。未在游戏房间内。", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("玩家：{}，房间Id：{}，同步触牌失败。游戏房间不存在。", playerId, roomId);
				return;
			}
			if (!room.isInRoom(playerId)) {
				logger.error("玩家：{}，房间Id：{}，同步触牌失败。未在游戏房间内。", playerId, roomId);
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
			logger.error("session中player信息已失效。");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("玩家：{}，选择目标失败。获取玩家信息失败。", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("玩家：{}，选择目标失败。未在游戏房间内。", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("玩家：{}，房间Id：{}，选择目标失败。游戏房间不存在。", playerId, roomId);
				return;
			}
			if (!room.isInRoom(playerId)) {
				logger.error("玩家：{}，房间Id：{}，选择目标失败。未在游戏房间内。", playerId, roomId);
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
			logger.error("session中player信息已失效。");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("玩家：{}，摸牌失败。获取玩家信息失败。", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("玩家：{}，摸牌失败。未在游戏房间内。", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("玩家：{}，房间Id：{}，摸牌失败。游戏房间不存在。", playerId, roomId);
				return;
			}
			if (!room.isInRoom(playerId)) {
				logger.error("玩家：{}，房间Id：{}，摸牌失败。未在游戏房间内。", playerId, roomId);
				return;
			}
			boolean result = room.drawCard(playerId);
			if (result) {
				logger.info("玩家：{}，房间Id：{}，摸牌成功。", playerId, roomId);
			} else {
				logger.info("玩家：{}，房间Id：{}，摸牌失败。", playerId, roomId);
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
			logger.error("session中player信息已失效。");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("玩家：{}，同步触牌信息失败。获取玩家信息失败。", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("玩家：{}，同步触牌失败。未在游戏房间内。", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("玩家：{}，房间Id：{}，同步触牌失败。游戏房间不存在。", playerId, roomId);
				return;
			}
			if (!room.isInRoom(playerId)) {
				logger.error("玩家：{}，房间Id：{}，同步触牌失败。未在游戏房间内。", playerId, roomId);
				return;
			}
			
			int enemyId = room.getEnemyId(playerId);
			FightMsgSend.other(room.getSession(enemyId), data);
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}
}