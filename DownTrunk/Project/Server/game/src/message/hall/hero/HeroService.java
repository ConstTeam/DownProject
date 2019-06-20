package message.hall.hero;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.ConfigData;
import config.model.HeroModel;
import config.model.SceneModel;
import db.PlayerDao;
import db.module.player.Player;
import message.hall.login.LoginService;
import message.hall.role.RoleMsgSend;
import net.DataAccessException;
import net.IByteBuffer;
import net.ISession;
import net.Servicelet;
import sys.HallServerOnlineManager;

public class HeroService extends Servicelet{
	private static final Logger logger = LoggerFactory.getLogger(HeroService.class);
	
	@Override
	public void access(ISession session, IByteBuffer data) throws DataAccessException{
		int type = data.readByte();
		String deviceId = null;
		int playerId = 0;

		switch (type) {
		case HeroMsgConst.BUY_HERO:
			int roleId = data.readByte();
			boolean isGold = data.readBoolean();
			buyHero(session, deviceId, playerId, roleId, isGold);
			break;
		case HeroMsgConst.BUY_SCENE:
			int sceneId = data.readByte();
			isGold = data.readBoolean();
			buyScene(session, deviceId, playerId, sceneId, isGold);
			break;
		}
	}
	
	private void buyHero(ISession session, String deviceId, int playerId, int roleId, boolean isGold) {
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
			HeroModel heroModel = ConfigData.heroModels.get(roleId);
			if (heroModel == null) {
				logger.info("玩家：{}。购买英雄失败，英雄Id在配置表中不存在：{}。", playerId, roleId);
				return;
			}
			int gold = isGold ? heroModel.Coin : 0;
			if (player.getGold() < gold) {
				logger.info("玩家：{}。购买英雄失败：{}。金币不足。需要金币 ：{}，玩家当前金币：{}", playerId, roleId, gold, player.getGold());
				return;
			}
			int roleList = PlayerDao.addRole(playerId, roleId, gold);
			if (roleList != -1) {
				logger.info("玩家：{}。购买英雄成功：{}。RoleList:{}", playerId, roleId, roleList);
				RoleMsgSend.roleListSync(session, roleList);
			} else {
				logger.info("玩家：{}。购买英雄失败：{}。", playerId, roleId);
			}
		} finally {
			HallServerOnlineManager.getInstance().getLock().unlock(playerId);
		}	
	}
	
	private void buyScene(ISession session, String deviceId, int playerId, int sceneId, boolean isGold) {
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
			SceneModel sceneModel = ConfigData.sceneModels.get(sceneId);
			if (sceneModel == null) {
				logger.info("玩家：{}。购买场景失败，场景Id在配置表中不存在：{}。", playerId, sceneId);
				return;
			}
			int gold = isGold ? sceneModel.Coin : 0;
			if (player.getGold() < gold) {
				logger.info("玩家：{}。购买场景失败：{}。金币不足。需要金币 ：{}，玩家当前金币：{}", playerId, sceneId, gold, player.getGold());
				return;
			}
			int sceneList = PlayerDao.addScene(playerId, sceneId, gold);
			if (sceneList != -1) {
				logger.info("玩家：{}。购买场景成功：{}。RoleList:{}", playerId, sceneId, sceneList);
				RoleMsgSend.sceneListSync(session, sceneList);
			} else {
				logger.info("玩家：{}。购买场景失败：{}。", playerId, sceneId);
			}
		} finally {
			HallServerOnlineManager.getInstance().getLock().unlock(playerId);
		}	
	}
}
