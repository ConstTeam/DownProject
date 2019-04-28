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
 * ����ģ��
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
			logger.error("��ң�{}��ս������¼ʧ�ܡ���ȡ�����Ϣʧ�ܡ�", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		if (playerInfo.getRoomId() == 0) {
			logger.error("��ң�{}��ս������¼ʧ�ܡ����δ������Ϸ���䡣", playerId);
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
					logger.info("��ң�{}������Id��{}����������ʧ�ܡ�������Ϣ�����ڡ�", playerId, roomId);
					return;
				}
				room = GameRoomManager.getInstance().addGameRoom(roomInfo, roomInfo.getType());
				if (room == null) {
					logger.info("��ң�{}������Id��{}����������ʧ�ܡ�", roomId);
					return;
				}
				
				room.setOwner(playerInfo.getPlayerId());
				int code = room.joinGame(playerInfo, session);
				logger.info("��ң�{}������Id��{}����������{}��", playerInfo.getPlayerId(), roomId, code == RoomConst.SUCCESS ? "�ɹ�" : "ʧ��");
			} else {
				int code = room.joinGame(playerInfo, session);
				logger.info("��ң�{}������Id��{}�����뷿��{}��", playerInfo.getPlayerId(), roomId, code == RoomConst.SUCCESS ? "�ɹ�" : "ʧ��");
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
			logger.error("��ң�{}��ս������¼ʧ�ܡ���ȡ�����Ϣʧ�ܡ�", playerId);
			return;
		}
		playerId = playerInfo.getPlayerId();
		GameRoomManager.getInstance().getLock().lock(playerId);
		int guideId = RedisProxy.getInstance().getPlayerGuideID(playerId);
		try {
			GuideGateModel gate = ConfigData.guideGateModels.get(guideId);
			if (gate == null) {
				logger.error("��������ʧ�ܣ�ָ��Id��{}�������ñ��в����ڡ�", guideId);
				return;
			}
			TempletBase templet = new GuideTemplet(guideId);
			RoomInfo roomInfo = RedisProxy.getInstance().addRoomInfo(templet, ServerStaticInfo.getServerId());
			if (roomInfo == null) {
				logger.error("��ң�{}������ָ����{}����������ʧ�ܡ�", playerId, guideId);
				return;
			}
			int roomId = roomInfo.getRoomId();
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				room = GameRoomManager.getInstance().addGameRoom(roomInfo, roomInfo.getType());
				if (room == null) {
					logger.info("��ң�{}������ָ����{}������Id��{}����������ʧ�ܡ�", playerId, guideId, roomId);
					return;
				}
				playerInfo.setRoomId(roomInfo.getRoomId());
				RedisProxy.getInstance().updatePlayerInfo(playerInfo, "roomId");
				room.setOwner(playerInfo.getPlayerId());
				int code = room.joinGame(playerInfo, session);
				logger.info("��ң�{}������Id��{}����������{}��", playerInfo.getPlayerId(), roomId, code == RoomConst.SUCCESS ? "�ɹ�" : "ʧ��");
			} else {
				int code = room.joinGame(playerInfo, session);
				logger.info("��ң�{}������Id��{}�����뷿��{}��", playerInfo.getPlayerId(), roomId, code == RoomConst.SUCCESS ? "�ɹ�" : "ʧ��");
			}
		} finally {
			GameRoomManager.getInstance().getLock().unlock(playerId);
		}		
	}
}