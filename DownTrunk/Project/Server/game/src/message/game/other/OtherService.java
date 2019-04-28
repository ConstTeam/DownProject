package message.game.other;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.ServerStaticInfo;
import config.ConfigData;
import config.model.guide.GuideGateModel;
import message.hall.login.LoginService;
import module.scene.GameRoom;
import module.scene.RoomConst;
import module.templet.GuideTemplet;
import module.templet.TempletBase;
import net.DataAccessException;
import net.IByteBuffer;
import net.ISession;
import net.Servicelet;
import redis.RedisProxy;
import redis.data.PlayerInfo;
import redis.data.RoomInfo;
import sys.GameRoomManager;
import sys.GameServerOnlineManager;

/**
 * 房间模块
 * 
 */
public class OtherService extends Servicelet {

	private static final Logger logger = LoggerFactory.getLogger(OtherService.class);

	@Override
	public void access(ISession session, IByteBuffer data) throws DataAccessException {
		int type = data.readByte();
		String deviceId = null;
		int playerId = 0;

		switch (type) {
		case OtherMsgConst.INTO_ROOM:
			playerId = data.readInt();
			login(session, deviceId, playerId);
			break;
		case OtherMsgConst.INTO_GUIDE_ROOM:
			playerId = data.readInt();
			guideRoom(session, deviceId, playerId);
			break;
		}

	}

	private void login(ISession session, String deviceId, int playerId) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		PlayerInfo playerInfo = GameServerOnlineManager.getInstance().playerLogin(session, playerId);
		if (playerInfo == null) {
			logger.error("玩家：{}，战斗服登录失败。获取玩家信息失败。", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("玩家：{}，战斗服登录失败。玩家未分配游戏房间。", playerId);
// 			S2CMessageSend.messageBoxById(session, 1001);
			return;
		}
		int roomId = playerInfo.getRoomId();
		
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				RoomInfo roomInfo = RedisProxy.getInstance().getRoomInfo(roomId);
				if (roomInfo == null) {
					logger.info("玩家：{}，房间Id：{}，创建房间失败。房间信息不存在。", playerId, roomId);
					return;
				}
				room = GameRoomManager.getInstance().addGameRoom(roomInfo, roomInfo.getType());
				if (room == null) {
					logger.info("玩家：{}，房间Id：{}，创建房间失败。", roomId);
					return;
				}
				
				room.setOwner(playerInfo.getPlayerId());
				int code = room.joinGame(playerInfo, session);
				logger.info("玩家：{}，房间Id：{}，创建房间{}。", playerInfo.getPlayerId(), roomId, code == RoomConst.SUCCESS ? "成功" : "失败");
			} else {
				int code = room.joinGame(playerInfo, session);
				logger.info("玩家：{}，房间Id：{}，进入房间{}。", playerInfo.getPlayerId(), roomId, code == RoomConst.SUCCESS ? "成功" : "失败");
			}
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}		
	}

	private void guideRoom(ISession session, String deviceId, int playerId) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		PlayerInfo playerInfo = GameServerOnlineManager.getInstance().playerLogin(session, playerId);
		if (playerInfo == null) {
			logger.error("玩家：{}，战斗服登录失败。获取玩家信息失败。", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		GameRoomManager.getInstance().getLock().lock(playerId);
		int guideId = RedisProxy.getInstance().getPlayerGuideID(playerId);
		try {
			GuideGateModel gate = ConfigData.guideGateModels.get(guideId);
			if (gate == null) {
				logger.error("创建房间失败，指引Id：{}，在配置表中不存在。", guideId);
				return;
			}
			TempletBase templet = new GuideTemplet(guideId);
			RoomInfo roomInfo = RedisProxy.getInstance().addRoomInfo(templet, ServerStaticInfo.getServerId());
			if (roomInfo == null) {
				logger.error("玩家：{}，新手指引：{}，创建房间失败。", playerId, guideId);
				return;
			}
			int roomId = roomInfo.getRoomId();
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				room = GameRoomManager.getInstance().addGameRoom(roomInfo, roomInfo.getType());
				if (room == null) {
					logger.info("玩家：{}，新手指引：{}，房间Id：{}，创建房间失败。", playerId, guideId, roomId);
					return;
				}
				playerInfo.setRoomId(roomInfo.getRoomId());
				RedisProxy.getInstance().updatePlayerInfo(playerInfo, "roomId");
				room.setOwner(playerInfo.getPlayerId());
				int code = room.joinGame(playerInfo, session);
				logger.info("玩家：{}，房间Id：{}，创建房间{}。", playerInfo.getPlayerId(), roomId, code == RoomConst.SUCCESS ? "成功" : "失败");
			} else {
				int code = room.joinGame(playerInfo, session);
				logger.info("玩家：{}，房间Id：{}，进入房间{}。", playerInfo.getPlayerId(), roomId, code == RoomConst.SUCCESS ? "成功" : "失败");
			}
		} finally {
			GameRoomManager.getInstance().getLock().unlock(playerId);
		}		
	}
}