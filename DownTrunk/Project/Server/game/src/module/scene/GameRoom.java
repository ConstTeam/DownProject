package module.scene;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import message.game.fight.FightMsgSend;
import message.game.room.RoomMsgSend;
import module.fight.BattleRole;
import module.templet.TempletBase;
import net.ISession;
import redis.RedisProxy;
import redis.data.PlayerInfo;
import sys.GameRoomManager;
import sys.GameSyncManager;
import sys.GameTimer;
import util.ErrorPrint;
import util.Tools;

public class GameRoom extends RoomConst implements ISceneAction {

	private static final Logger logger = LoggerFactory.getLogger(GameRoom.class);

	/** 房间Id */
	private int roomId;
	/** 房主 */
	private int owner;
	/** 房间状态 */
	private int state = ROOM_STATE_PLAYING;
	/** 房间状态 */
	private int playState = PLAY_STATE_READY;
	/** 回合数 */
	private int round = 0;

	/** 玩家Session列表<玩家id, ISession> */
	private HashMap<Integer, ISession> sessions = new HashMap<>();
	/** 玩家表 <玩家id, Player> */
	private HashMap<Integer, PlayerInfo> players = new HashMap<>();
	/** 战斗玩家表 <玩家id, FightRole> */
	private HashMap<Integer, BattleRole> fighters = new HashMap<>();
	/** 战斗玩家表 <玩家uid, FightRole> */
	private HashMap<Integer, BattleRole> fightersByUid = new HashMap<>();

	/** 玩家准备表 <玩家id, Boolean> */
	private HashMap<Integer, Boolean> ready = new HashMap<>();
	/** 游戏初始参数模板 */
	private TempletBase templet;
	
	private ScheduledFuture<?> future;

	/** 当前局英雄唯一ID自增 */
	private int heroUId = 100;
	/** 是否开启倒计时 */
	private boolean countDown = true; 
	/** 倒计时时长（毫秒） */
	private int turnCountDownTime = TURN_COUNT_DOWN_TIME;
	
	private int seed = Tools.random(1, 100000);
	
	private GameSyncManager syncManager = GameSyncManager.getInstance();
	
	@Override
	public int joinGame(PlayerInfo player, ISession session) {
		int playerId = player.getPlayerId();
		if (players.get(playerId) != null || players.size() >= 2) {
			return -1;
		}
		/*
		 * 创建本局中的英雄
		 */
		BattleRole battleRole = new BattleRole(playerId, player.getNickname(), this.templet.initHp, this.heroUId);
		battleRole.setRoomId(this.roomId);
		battleRole.setIcon(player.getIcon());
		if (battleRole.getQuestManager() == null) {
			return -1;
		}
		syncManager.addPlayer(playerId);
		players.put(playerId, player);
		sessions.put(playerId, session);
		fighters.put(playerId, battleRole);
		fightersByUid.put(battleRole.getUid(), battleRole);
		this.heroUId++;
		/*
		 * 更新Redis信息
		 */
		logger.info("玩家：{}，房间Id：{}，加入房间。", playerId, this.roomId);
		RoomMsgSend.intoRoom(session);
		if (players.size() == 2) {
			gameStart();
		}
		return SUCCESS;
	}
	
	@Override
	public int exitGame(PlayerInfo player) {
		int playerId = player.getPlayerId();

		/*
		 * 更新Redis信息
		 */
		RedisProxy.getInstance().updatePlayerInfo(player, "roomId");
		logger.info("玩家：{}，房间Id：{}，退出房间。", playerId, this.roomId);
		return SUCCESS;
	}

	@Override
	public void gameStart() {
		logger.info("房间Id：{}，准备开始游戏。", this.roomId);
		playState = PLAY_STATE_START;
		
		switch (this.getTemplet().type) {
		case RoomConst.ROOM_TYPE_PVP:
			sendRoomInfo();
			break;
		case RoomConst.ROOM_TYPE_GUIDE:
			break;
		case RoomConst.ROOM_TYPE_ROBOT:
			break;
		}
		
	}
	
	/**
	 * 匹配场模式
	 */
	public void sendRoomInfo() {
		for (int playerId : this.sessions.keySet()) {
			FightMsgSend.intoRoom(this.sessions.get(playerId), playerId, this);
		}
	}
			
	public void ready(int playerId) {
		if (this.ready.get(playerId) != null) {
			return;
		}
		this.ready.put(playerId, true);
		if (this.ready.size() == 2) {
			switch (this.getTemplet().type) {
			case ROOM_TYPE_PVP:
				future = GameTimer.getScheduled().schedule(() -> pvpStart(), 3, TimeUnit.SECONDS);
				break;

			case ROOM_TYPE_GUIDE:
				break;
				
			case ROOM_TYPE_ROBOT:
				pvpStart();
				break;
			}
		}
	}
	
	private void pvpStart() {
		interruptTimer();
		Collection<ISession> sessions = this.sessions.values();
		FightMsgSend.startGame(sessions);
		syncManager.start(String.valueOf(roomId));
	}

	public boolean isInRoom(int playerId) {
		return fighters.get(playerId) != null;
	}

	@Override
	public void gameEnd() {
		this.destroy();
	}

	@Override
	public void settlement(int playerId) {
		if (getState() == ROOM_DESTORY) {
			return;
		}
		try {
		} catch (Exception e) {
			ErrorPrint.print(e);
		} finally {
			GameRoomManager.getInstance().destroyRoom(this.getRoomId());
		}
	}
	
	@Override
	public void destroy() {
		this.syncManager.stop();
		interruptTimer();
		this.state = ROOM_DESTORY;
		this.players.clear();
	}

	public int getRoomId() {
		return roomId;
	}

	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getOwner() {
		return owner;
	}

	public void setOwner(int owner) {
		this.owner = owner;
	}

	public TempletBase getTemplet() {
		return templet;
	}

	public void setTemplet(TempletBase templet) {
		this.templet = templet;
	}

	public int getRound() {
		return round;
	}

	public void setRound(int round) {
		this.round = round;
	}

	public void addRound() {
		this.round++;
	}

	public boolean isTurboMode() {
		return getRound() >= this.templet.turboModeRound;
	}

	public boolean turboModeStart() {
		return getRound() == this.templet.turboModeRound;
	}

	@Override
	public void resetPlayer(PlayerInfo player) {

	}

	@Override
	public void resend(PlayerInfo player) {

	}

	@Override
	public void otherPlayerSyncMessage(PlayerInfo player) {

	}

	@Override
	public void exitRoomMessage(PlayerInfo player) {

	}

	@Override
	public void exitRoomSync(int playerId) {

	}

	@Override
	public boolean exitRoomPre(PlayerInfo player, ISession session, int state) {
		return false;
	}
	
	@Override
	public void giveup(int loser) {
		BattleRole role = this.getBattleRole(loser);
		if (role != null) {
			role.setHp(0);
			settlement(loser);
		}
	}

	public BattleRole getBattleRole(int playerId) {
		return this.fighters.get(playerId);
	}
	
	public HashMap<Integer, BattleRole> getBattleRoles() {
		return this.fighters;
	}

	private void notice() {
		
		for (Integer playerId : fighters.keySet()) {
			if (this.ready.get(playerId) != null) {
				continue;
			}
			// TODO 客户端提示
		}
		/*
		 * 倒计时15秒。。
		 */
		logger.info("房间：{}，倒计时15秒后自动开始。", this.roomId);
		int seconds = 17000;
		future = GameTimer.getScheduled().schedule(() -> autoReady(), seconds, TimeUnit.MILLISECONDS);
	}
	
	private void autoReady() {
		GameRoomManager.getInstance().getLock().lock(getRoomId());
		try {
			for (Integer playerId : fighters.keySet()) {
				ready(playerId);
			}
		} finally {
			GameRoomManager.getInstance().getLock().unlock(getRoomId());
		}
	}
	
	public void startTimer() {
		if (future != null && future.getDelay(TimeUnit.MILLISECONDS) > 0) {
			interruptTimer();
		}
		/*
		 * 倒计时45秒。。
		 */
		int seconds = 45000;
		future = GameTimer.getScheduled().schedule(() -> notice(), seconds, TimeUnit.MILLISECONDS);
		logger.info("房间：{}，倒计时60秒后自动开始。开始计时。", this.roomId);
	}
	
	public void interruptTimer() {
		if (future != null && future.getDelay(TimeUnit.MILLISECONDS) > 0) {
			future.cancel(false);
			logger.info("房间：{}，中止计时", this.roomId);
		}
	}
	
	public int getPlayState() {
		return playState;
	}

	public void setPlayState(int playState) {
		this.playState = playState;
	}

	public boolean isCountDown() {
		return countDown;
	}

	public void setCountDown(boolean countDown) {
		this.countDown = countDown;
	}

	public int getTurnCountDownTime() {
		return turnCountDownTime;
	}

	public void setTurnCountDownTime(int turnCountDownTime) {
		this.turnCountDownTime = turnCountDownTime;
	}
	
	public ISession getSession(int playerId) {
		return this.sessions.get(playerId);
	}
	
	public GameSyncManager getSyncManager() {
		return syncManager;
	}

	public void setSyncManager(GameSyncManager syncManager) {
		this.syncManager = syncManager;
	}

	public int getSeed() {
		return seed;
	}

	public void setSeed(int seed) {
		this.seed = seed;
	}
}
