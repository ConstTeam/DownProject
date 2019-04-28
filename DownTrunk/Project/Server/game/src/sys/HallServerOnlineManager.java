package sys;

import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.module.player.Player;
import memory.LockMemory;
import memory.SessionMemory;
import memory.UserMemory;
import net.ISession;
import redis.RedisProxy;
import redis.data.PlayerInfo;
import util.TimeFormat;

/**
 * 大厅在线玩家管理器
 * 
 */
public class HallServerOnlineManager {

	private static final Logger logger = LoggerFactory.getLogger(HallServerOnlineManager.class);

	/** 在线玩家人线 */
	private static AtomicInteger count = new AtomicInteger();

	private static HallServerOnlineManager instance;
	
	/** 玩家锁 */
	private static LockMemory lock = LockMemory.getInstance();

	public static HallServerOnlineManager getInstance() {
		if (instance == null) {
			instance = new HallServerOnlineManager();
		}

		return instance;
	}

	public LockMemory getLock() {
		return lock;
	}

	/**
	 * 获取当前大厅服实时玩家数量
	 * 
	 * @return
	 */
	public int getOnlineCount() {
		return count.intValue();
	}

	public void playerLogin(ISession session, Player player) {
		session.attach(player);
		UserMemory.getInstance().add(player);
		ISession oldSession = SessionMemory.getInstance().add(player.getPlayerId(), session);
		if (oldSession == null) {
			count.incrementAndGet();
			ServerLogTimer.setMaxOnlineNum(getOnlineCount());
		} else {
			playerLogout(oldSession);
		}
		PlayerInfo playerInfo = new PlayerInfo();
		playerInfo.setPlayerId(player.getPlayerId());
		playerInfo.setOnline(true);
		playerInfo.setLastLoginTime(TimeFormat.getTime());
		RedisProxy.getInstance().updatePlayerInfo(playerInfo, "online");
		RedisProxy.getInstance().updatePlayerInfo(playerInfo, "lastLoginTime");
		logger.info("玩家：{}，登录游戏。", player.getPlayerId());
	}

	public void playerLogout(ISession session) {
		if (session == null) {
			return;
		}
		if (session.attachment() == null) {
			return;
		}
		Player player = (Player) session.attachment();
		GameRoomAssign.getInstance().removePlayerInfo(player.getPlayerId());
		if (UserMemory.getInstance().remove(player.getPlayerId(), player)) {
			PlayerInfo playerInfo = new PlayerInfo();
			playerInfo.setPlayerId(player.getPlayerId());
			playerInfo.setOnline(false);
			playerInfo.setLastLogoutTime(TimeFormat.getTime());
			RedisProxy.getInstance().updatePlayerInfo(playerInfo, "online");
			RedisProxy.getInstance().updatePlayerInfo(playerInfo, "lastLogoutTime");
			logger.info("玩家：{}，退出游戏。", player.getPlayerId());
		}
		if (SessionMemory.getInstance().removeByPlayerId(player.getPlayerId(), session)) {
			count.decrementAndGet();
		}
		if (!session.isClosed()) {
			session.close();
		}
	}

}
