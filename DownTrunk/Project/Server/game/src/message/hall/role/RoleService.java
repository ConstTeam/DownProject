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
		case RoleMsgConst.CHANGE_ICON:
			String icon = data.readUTF();
			changeIcon(session, deviceId, playerId, icon);
			break;
		}
	}
	
	private void changeIcon(ISession session, String deviceId, int playerId, String icon) {
		if (deviceId != null && !LoginService.checkDeviceId(playerId, deviceId, session)) {
			return;
		}
		if (session.attachment() == null) {
			logger.error("session中player信息已失效。");
			return;
		}
		Player player = (Player)session.attachment();
		playerId = player.getPlayerId();
		HallServerOnlineManager.getInstance().getLock().lock(playerId);
		try {
			PlayerInfo playerInfo = RedisProxy.getInstance().getPlayerInfo(playerId);
			String oldIcon = playerInfo.getIcon();
			playerInfo.setIcon(icon);
			boolean result = RedisProxy.getInstance().updatePlayerInfo(playerInfo, "icon");
			if (result) {
				logger.info("玩家：{}。修改icon成功：{}。", playerId, icon);
			} else {
				playerInfo.setIcon(oldIcon);
				logger.info("玩家：{}。修改icon失败：{}。", playerId, icon);
			}
			RoleMsgSend.changeIconRes(session, result, playerInfo.getIcon());
		} finally {
			HallServerOnlineManager.getInstance().getLock().unlock(playerId);
		}	
	}
}
