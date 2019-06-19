package message.hall.role;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.module.player.Player;
import message.hall.login.LoginService;
import net.DataAccessException;
import net.IByteBuffer;
import net.ISession;
import net.Servicelet;
import redis.RedisProxy;
import redis.data.PlayerInfo;
import sys.HallServerOnlineManager;

public class RoleService extends Servicelet{
	private static final Logger logger = LoggerFactory.getLogger(RoleService.class);
	
	@Override
	public void access(ISession session, IByteBuffer data) throws DataAccessException{
		int type = data.readByte();
		String deviceId = null;
		int playerId = 0;

		switch (type) {
		case RoleMsgConst.CHANGE_SCENE:
			int sceneId = data.readByte();
			changeScene(session, deviceId, playerId, sceneId);
			break;
		case RoleMsgConst.CHANGE_ROLE:
			int role = data.readByte();
			changeRole(session, deviceId, playerId, role);
			break;
		}
	}
	
	private void changeScene(ISession session, String deviceId, int playerId, int sceneId) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session��player��Ϣ��ʧЧ��");
			return;
		}
		Player player = (Player)session.attachment();
		playerId = player.getPlayerId();
		HallServerOnlineManager.getInstance().getLock().lock(playerId);
		try {
			PlayerInfo playerInfo = RedisProxy.getInstance().getPlayerInfo(playerId);
			int oldSceneId = playerInfo.getSceneId();
			playerInfo.setSceneId(sceneId);
			boolean result = RedisProxy.getInstance().updatePlayerInfo(playerInfo, "sceneId");
			if (result) {
				logger.info("��ң�{}���޸�sceneId�ɹ���{}��", playerId, sceneId);
				RoleMsgSend.changeSceneRes(session, sceneId);
			} else {
				playerInfo.setSceneId(oldSceneId);
				logger.info("��ң�{}���޸�sceneIdʧ�ܣ�{}��", playerId, sceneId);
			}
		} finally {
			HallServerOnlineManager.getInstance().getLock().unlock(playerId);
		}	
	}
	
	private void changeRole(ISession session, String deviceId, int playerId, int roleId) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session��player��Ϣ��ʧЧ��");
			return;
		}
		Player player = (Player)session.attachment();
		playerId = player.getPlayerId();
		HallServerOnlineManager.getInstance().getLock().lock(playerId);
		try {
			PlayerInfo playerInfo = RedisProxy.getInstance().getPlayerInfo(playerId);
			int oldRoleId = playerInfo.getRoleId();
			playerInfo.setRoleId(roleId);
			boolean result = RedisProxy.getInstance().updatePlayerInfo(playerInfo, "roleId");
			if (result) {
				logger.info("��ң�{}���޸�roleId�ɹ���{}��", playerId, roleId);
				RoleMsgSend.changeRoleRes(session, roleId);
			} else {
				playerInfo.setRoleId(oldRoleId);
				logger.info("��ң�{}���޸�roleIdʧ�ܣ�{}��", playerId, roleId);
			}
		} finally {
			HallServerOnlineManager.getInstance().getLock().unlock(playerId);
		}	
	}
}
