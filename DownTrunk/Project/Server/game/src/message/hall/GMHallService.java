package message.hall;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.ConfigData;
import config.model.quest.QuestModel;
import db.QuestDao;
import db.module.player.Player;
import message.game.gm.GMMsgConst;
import message.hall.login.LoginService;
import module.quest.Quest;
import net.DataAccessException;
import net.IByteBuffer;
import net.ISession;
import net.Servicelet;
import redis.RedisProxy;
import redis.data.IFunctionSwitchConst;
import sys.HallServerOnlineManager;

/**
 * GM模块
 * 
 */
public class GMHallService extends Servicelet {

	private static final Logger logger = LoggerFactory.getLogger(GMHallService.class);

	@Override
	public void access(ISession session, IByteBuffer data) throws DataAccessException {
		int type = data.readByte();
		String deviceId = null;
		int playerId = 0;

		switch (type) {
		case GMMsgConst.GM_COMMOND:
			String arg1 = data.readUTF();
			String arg2 = data.readUTF();
			String arg3 = data.readUTF();
			gm(session, deviceId, playerId, arg1, arg2, arg3);
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
		Player player = (Player)session.attachment();
		if (player == null) {
			logger.error("玩家：{}，使用GM指令失败。获取玩家信息失败。", playerId);
			return;
		}
		playerId = player.getPlayerId();
		if (!gmPanel(player.getDeviceId())) {
			logger.error("玩家：{}，使用GM指令失败。GM功能未开启。Arg1:{}, Arg2:{}", playerId, arg0, arg1);
			return;
		}
		int[] args = new int[3];
		try {
			args[0] = Integer.parseInt(arg0);
			args[1] = Integer.parseInt(arg1);
		} catch (Exception e) {
			logger.error("玩家：{}，使用GM指令失败。参数解析失败。Arg1:{}, Arg2:{}", playerId, arg0, arg1);
			return;
		}
			
		HallServerOnlineManager.getInstance().getLock().lock(playerId);
		try {
			switch (args[0]) {
			case 3:
				QuestModel questModel = ConfigData.questModels.get(args[1]);
				if (questModel == null) {
					logger.error("玩家：{}，使用GM指令失败。任务Id：{}，在配置表中不存在。", playerId, arg1);
					return;
				}
				Quest quest = new Quest();
				quest.setPlayerId(playerId);
				quest.setIndex(1);
				quest.setQuestId(questModel.ID);
				QuestDao.gmFlushSingleQuest(playerId, quest);
				break;
			case 11:
				RedisProxy.getInstance().savePlayerGuideID(playerId, 9);
				break;
			}
			logger.info("玩家：{}，使用GM指令成功。Arg1:{}, Arg2:{}", playerId, arg0, arg1);
		} finally {
			HallServerOnlineManager.getInstance().getLock().unlock(playerId);
		}		
	}
	
	public static boolean gmPanel(String deviceId) {
		boolean isOpen = RedisProxy.getInstance().getFunctionSwitch(IFunctionSwitchConst.GM); // GM面板是否开启
		return isOpen || RedisProxy.getInstance().isGmDevice(deviceId);
	}
	
	public static boolean gmPanel(String platform, String deviceId) {
		return !"wx".equalsIgnoreCase(platform) && gmPanel(deviceId);
	}
}