package message.game.fight;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import message.hall.login.LoginService;
import module.scene.GameRoom;
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
			heroDied(session, deviceId, playerId);
			break;
		}
	}

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
			logger.error("玩家：{}，准备失败。获取玩家信息失败。", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("玩家：{}，准备失败。未在游戏房间内。", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("玩家：{}，房间Id：{}，准备失败。游戏房间不存在。", playerId, roomId);
				return;
			}
			if (!room.isInRoom(playerId)) {
				logger.error("玩家：{}，房间Id：{}，准备失败。未在游戏房间内。", playerId, roomId);
				return;
			}
			room.ready(playerId);
			logger.info("玩家：{}，房间Id：{}，准备成功。", playerId, roomId);
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}

	private void roleHp(ISession session, String deviceId, int playerId, int hp) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session中player信息已失效。");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("玩家：{}，同步血量失败。获取玩家信息失败。", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("玩家：{}，同步血量失败。未在游戏房间内。", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("玩家：{}，房间Id：{}，同步血量失败。游戏房间不存在。", playerId, roomId);
				return;
			}
			if (!room.isInRoom(playerId)) {
				logger.error("玩家：{}，房间Id：{}，同步血量失败。未在游戏房间内。", playerId, roomId);
				return;
			}
			room.roleHpSync(session, playerId, hp);
			logger.info("玩家：{}，房间Id：{}，血量：{}，同步血量成功。", playerId, roomId, hp);
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}

	private void getItem(ISession session, String deviceId, int playerId, int itemId) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session中player信息已失效。");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("玩家：{}，获取道具失败。获取玩家信息失败。", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("玩家：{}，获取道具失败。未在游戏房间内。", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("玩家：{}，房间Id：{}，获取道具失败。游戏房间不存在。", playerId, roomId);
				return;
			}
			if (!room.isInRoom(playerId)) {
				logger.error("玩家：{}，房间Id：{}，获取道具失败。未在游戏房间内。", playerId, roomId);
				return;
			}
			room.itemSync(session, playerId, itemId);
			logger.info("玩家：{}，房间Id：{}，道具：{}，获取道具成功。", playerId, roomId, itemId);
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}
	
	private void useItem(ISession session, String deviceId, int playerId, int targetId, int itemId, boolean mainSkill) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session中player信息已失效。");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("玩家：{}，使用道具失败。获取玩家信息失败。", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("玩家：{}，使用道具失败。未在游戏房间内。", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("玩家：{}，房间Id：{}，使用道具失败。游戏房间不存在。", playerId, roomId);
				return;
			}
			if (!room.isInRoom(playerId)) {
				logger.error("玩家：{}，房间Id：{}，使用道具失败。未在游戏房间内。", playerId, roomId);
				return;
			}
			room.useItemSync(playerId, targetId, itemId, mainSkill);
			logger.info("玩家：{}，房间Id：{}，目标玩家：{}，道具：{}，使用道具成功。", playerId, roomId, targetId, itemId);
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}
	
	private void heroDied(ISession session, String deviceId, int playerId) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session中player信息已失效。");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("玩家：{}，英雄死亡失败。获取玩家信息失败。", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("玩家：{}，英雄死亡失败。未在游戏房间内。", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("玩家：{}，房间Id：{}，英雄死亡失败。游戏房间不存在。", playerId, roomId);
				return;
			}
			if (!room.isInRoom(playerId)) {
				logger.error("玩家：{}，房间Id：{}，英雄死亡失败。未在游戏房间内。", playerId, roomId);
				return;
			}
			room.heroDied(playerId);
			logger.info("玩家：{}，房间Id：{}，英雄死亡。", playerId, roomId);
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}
}