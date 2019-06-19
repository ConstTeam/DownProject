package message.game.other;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import message.hall.login.LoginService;
import module.scene.GameRoom;
import module.scene.RoomConst;
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
}