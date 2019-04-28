package message.hall.guide;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.module.player.Player;
import message.hall.login.LoginService;
import net.DataAccessException;
import net.IByteBuffer;
import net.ISession;
import net.Servicelet;
import redis.RedisProxy;
import sys.HallServerOnlineManager;

/**
 * 指引模块
 * 
 */
public class GuideService extends Servicelet {

	private static final Logger logger = LoggerFactory.getLogger(GuideService.class);

	@Override
	public void access(ISession session, IByteBuffer data) throws DataAccessException {
		int type = data.readByte();
		String deviceId = null;
		int playerId = 0;

		switch (type) {
		case GuideMsgConst.GUIDE_INFO:
			guideInfo(session, deviceId, playerId);
			break;
			
		case GuideMsgConst.UPDATE_GUIDE:
			int guideId = data.readInt();
			updateGuide(session, deviceId, playerId, guideId);
			break;
			
		}

	}
	
	private void guideInfo(ISession session, String deviceId, int playerId) {
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
			int guideId = RedisProxy.getInstance().getPlayerGuideID(playerId);
			GuideMsgSend.guideInfoSync(session, guideId);
		} finally {
			HallServerOnlineManager.getInstance().getLock().unlock(playerId);
		}	
	}
	
	private void updateGuide(ISession session, String deviceId, int playerId, int guideId) {
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
			int curGuideId = RedisProxy.getInstance().getPlayerGuideID(playerId);
			if (curGuideId >= guideId) {
				logger.error("玩家：{}。更新指引失败。 {}-{}有误。", playerId, curGuideId, guideId);
				return;
			}
			RedisProxy.getInstance().savePlayerGuideID(playerId, guideId);
			GuideMsgSend.updateGuideRet(session, guideId);
		} finally {
			HallServerOnlineManager.getInstance().getLock().unlock(playerId);
		}	
	}
	
}