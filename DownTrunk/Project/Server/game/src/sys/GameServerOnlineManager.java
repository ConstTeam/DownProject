package sys;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.PlayerDao;
import db.module.player.Player;
import memory.LockMemory;
import memory.SessionMemory;
import module.scene.GameRoom;
import module.scene.RoomConst;
import net.ISession;
import redis.RedisProxy;
import redis.data.PlayerInfo;

/**
 * ��Ϸ������ҹ�����
 * 
 */
public class GameServerOnlineManager {

	private static final Logger logger = LoggerFactory.getLogger(GameServerOnlineManager.class);

	/** ����������� */ 
	private static AtomicInteger count = new AtomicInteger(); // XXX �о�Ӧ�û����ط� SessionMemory֮���

	private static GameServerOnlineManager instance;
	
	private static ConcurrentHashMap<Integer, ScheduledFuture<?>> playerTrusteeshipTimer = new ConcurrentHashMap<>();

	/** ����� */
	private static LockMemory lock = LockMemory.getInstance();

	public static GameServerOnlineManager getInstance() {
		if (instance == null) {
			instance = new GameServerOnlineManager();
		}

		return instance;
	}

	public LockMemory getLock() {
		return lock;
	}

	/**
	 * ��ȡ��ǰ��Ϸ��ʵʱ�������
	 * 
	 * @return
	 */
	public int getOnlineCount() {
		return count.intValue();
	}

	public PlayerInfo playerLogin(ISession session, int playerId) {
		
		PlayerInfo playerInfo = RedisProxy.getInstance().getPlayerInfo(playerId);
		if (playerInfo == null) {
			return null;
		}
		Player player = PlayerDao.getPlayerInfo(playerId);
		if (player == null) {
			logger.error("���뷿��ʧ�ܣ���ȡ�����Ϣʧ�ܣ���ң�" + playerId);
			return null;
		}
		ScheduledFuture<?> future = playerTrusteeshipTimer.get(playerId);
		if (future != null && !future.isDone()) {
			future.cancel(false);
			logger.info("���{}���ر��й�Timer��", playerId);
		}

		ISession oldSession = SessionMemory.getInstance().add(playerInfo.getPlayerId(), session);
		if (oldSession != null) {
			GameServerOnlineManager.getInstance().playerLogout(oldSession);
			oldSession.close();
			logger.info("��ң�{}����Ϸ��Session����", playerId);
		}
		
		playerInfo.setGold(player.getGold());
		playerInfo.setDiamond(player.getDiamond());
		playerInfo.setOnline(true);
		
		session.attach(playerInfo);
		count.incrementAndGet();
		ServerLogTimer.setMaxOnlineNum(getOnlineCount());
		
		logger.info("��ң�{}����¼��Ϸ��", player.getPlayerId());
		return playerInfo;
	}

	public void playerLogout(ISession session) {
		if (session == null || session.attachment() == null) {
			return;
		}
		PlayerInfo player = (PlayerInfo) session.attachment();
		player.setOnline(false);
		exitRoom(player);
		if (SessionMemory.getInstance().removeByPlayerId(player.getPlayerId(), session)) {
			count.decrementAndGet();
		}
		logger.info("��ң�{}���˳���Ϸ��", player.getPlayerId());
	}
	
	private void exitRoom(PlayerInfo player) {
		if (player.getRoomId() == 0) {
			return;
		}
		GameRoom room = GameRoomManager.getInstance().getRoom(player.getRoomId());
		if (room == null || room.getState() == RoomConst.ROOM_DESTORY || room.getState() == RoomConst.ROOM_STATE_END) {
			return;
		}
		GameRoomManager.getInstance().getLock().lock(room.getRoomId());
		try {
			room.giveup(player.getPlayerId());
			if (room.getState() != RoomConst.ROOM_DESTORY) {
				GameRoomManager.getInstance().destroyRoom(room.getRoomId());
			}
		} finally {
			GameRoomManager.getInstance().getLock().unlock(room.getRoomId());
		}
	}

}
