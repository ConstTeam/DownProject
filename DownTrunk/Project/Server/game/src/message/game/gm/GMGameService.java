package message.game.gm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import message.hall.GMHallService;
import message.hall.login.LoginService;
import module.area.Area;
import module.fight.BattleRole;
import module.scene.GameRoom;
import module.scene.RoomConst;
import net.DataAccessException;
import net.IByteBuffer;
import net.ISession;
import net.Servicelet;
import redis.data.PlayerInfo;
import sys.GameRoomManager;

/**
 * GM模块
 * 
 */
public class GMGameService extends Servicelet {

	private static final Logger logger = LoggerFactory.getLogger(GMGameService.class);

	@Override
	public void access(ISession session, IByteBuffer data) throws DataAccessException {
		int type = data.readByte();
		String deviceId = null;
		int playerId = 0;

		switch (type) {
		case GMMsgConst.GM_COMMOND:
			String arg0 = data.readUTF();
			String arg1 = data.readUTF();
			String arg2 = data.readUTF();
			gm(session, deviceId, playerId, arg0, arg1, arg2);
		}

	}

	private void gm(ISession session, String deviceId, int playerId, String arg0, String arg1, String arg2) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session中player信息已失效。");
			return;
		}
		PlayerInfo playerInfo = (PlayerInfo)session.attachment();
		if (playerInfo == null) {
			logger.error("玩家：{}，使用GM指令失败。获取玩家信息失败。", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("玩家：{}，使用GM指令失败。玩家未在游戏房间内。", playerId);
			return;
		}
		if (!GMHallService.gmPanel(playerInfo.getDeviceId())) {
			logger.error("玩家：{}，使用GM指令失败。GM功能未开启。Arg1:{}, Arg2:{}", playerId, arg0, arg1);
			return;
		}
		int roomId = playerInfo.getRoomId();
		int[] args = new int[3];
		try {
			args[0] = Integer.parseInt(arg0);
			args[1] = Integer.parseInt(arg1);
		} catch (Exception e) {
			logger.error("玩家：{}，使用GM指令失败。参数解析失败。Arg1:{}, Arg2:{}", playerId, arg0, arg1);
			return;
		}
			
		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("玩家：{}，使用GM指令失败。游戏房间不存在。", playerId);
				return;
			}
			switch (args[0]) {
			case 2:
				BattleRole fighter = room.getBattleRole(playerId);
				fighter.setDealCardId(arg1);
				break;
			case 4:
				room.syncStart(playerId);
				fighter = room.getBattleRole(playerId);
				fighter.addReplResource(args[1]);
				fighter.replenishedResource();
				room.resourceSync(fighter);
				room.syncEnd(playerId);
				break;
			case 6:
				room.syncStart(playerId);
				fighter = room.getBattleRole(playerId);
				if (args[1] == 0) {
					for (Area tempArea : fighter.getAreas()) {
						if (tempArea.getLevel() < Area.MAX_LEVEL) {
							room.areaLvUp(tempArea, fighter);
						}
					}
				} else if (args[1] >= 1 && args[1] <= 3) {
					Area area = fighter.getArea(args[1] - 1);
					if (area.getLevel() < Area.MAX_LEVEL) {
						room.areaLvUp(area, fighter);
					}
				}
				room.syncEnd(playerId);
				break;
			case 8:
				if (args[1] == 0) {
					room.setCountDown(false);
					room.interruptTimer();
				} else if (args[1] == 1) {
					room.setCountDown(true);
					room.setTurnCountDownTime(RoomConst.TURN_COUNT_DOWN_TIME);
					room.startTurnTimer();
				} else if (args[1] > 15) {
					room.setCountDown(true);
					int second = args[1] - 15;
					room.setTurnCountDownTime(second * 1000);
					room.startTurnTimer();
				} else {
					room.setCountDown(true);
					room.setTurnCountDownTime(1000);
					room.startTurnTimer();
				}
				break;
			}
			logger.info("玩家：{}，使用GM指令成功。Arg1:{}, Arg2:{}", playerId, arg0, arg1);
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}		
	}
}