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
import sys.UDPMsgManager;
import util.ErrorPrint;
import util.Tools;

public class GameRoom extends RoomConst implements ISceneAction {

	private static final Logger logger = LoggerFactory.getLogger(GameRoom.class);

	/** ����Id */
	private int roomId;
	/** ���� */
	private int owner;
	/** ����״̬ */
	private int state = ROOM_STATE_JOIN;
	/** ����״̬ */
	private int playState = PLAY_STATE_READY;
	/** �غ��� */
	private int round = 0;
	
	/** ���Session�б�<���id, ISession> */
	private HashMap<Integer, ISession> sessions = new HashMap<>();
	/** ��ұ� <���id, Player> */
	private HashMap<Integer, PlayerInfo> players = new HashMap<>();
	/** ս����ұ� <���id, FightRole> */
	private HashMap<Integer, BattleRole> fighters = new HashMap<>();
	/** ս����ұ� <���uid, FightRole> */
	private HashMap<Integer, BattleRole> fightersByUid = new HashMap<>();

	/** ���׼���� <���id, Boolean> */
	private HashMap<Integer, Boolean> ready = new HashMap<>();
	/** ��Ϸ��ʼ����ģ�� */
	private TempletBase templet;
	
	private ScheduledFuture<?> future;

	/** ��ǰ��Ӣ��ΨһID���� */
	private int heroUId = 100;
	
	private int seed = Tools.random(1, 100000);
	
	private GameSyncManager syncManager = GameSyncManager.getInstance();
	
	@Override
	public int joinGame(PlayerInfo player, ISession session) {
		if (state != ROOM_STATE_JOIN) {
			return -1;
		}
		int playerId = player.getPlayerId();
		if (players.get(playerId) != null || players.size() >= this.templet.maxNum) {
			return -1;
		}
		/*
		 * ���������е�Ӣ��
		 */
		BattleRole battleRole = new BattleRole(playerId, player.getNickname(), this.templet.initHp, this.heroUId);
		battleRole.setRoomId(this.roomId);
		battleRole.setIcon(player.getIcon());
		battleRole.setSceneId(player.getSceneId());
		battleRole.setRoleId(player.getRoleId());
		syncManager.addPlayer(playerId);
		players.put(playerId, player);
		sessions.put(playerId, session);
		fighters.put(playerId, battleRole);
		fightersByUid.put(battleRole.getUid(), battleRole);
		this.heroUId++;
		/*
		 * ����Redis��Ϣ
		 */
		logger.info("��ң�{}������Id��{}�����뷿�䡣", playerId, this.roomId);
		RoomMsgSend.intoRoom(session);
		UDPMsgManager.getInstance().setPlayerRoom(playerId, roomId);
		if (players.size() == this.templet.maxNum) {
			gameStart();
		}
		return SUCCESS;
	}
	
	@Override
	public int exitGame(PlayerInfo player) {
		int playerId = player.getPlayerId();

		/*
		 * ����Redis��Ϣ
		 */
		RedisProxy.getInstance().updatePlayerInfo(player, "roomId");
		logger.info("��ң�{}������Id��{}���˳����䡣", playerId, this.roomId);
		return SUCCESS;
	}

	@Override
	public void gameStart() {
		logger.info("����Id��{}��׼����ʼ��Ϸ��", this.roomId);
		state = ROOM_STATE_READY;
		playState = PLAY_STATE_START;
		
		switch (this.getTemplet().type) {
		case RoomConst.ROOM_TYPE_PVP:
			sendRoomInfo();
			break;
		}
		
	}
	
	/**
	 * ƥ�䳡ģʽ
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
		if (state != ROOM_STATE_READY) {
			return;
		}
		this.ready.put(playerId, true);
		if (this.ready.size() == this.players.size()) {
			state = ROOM_STATE_PLAYING;
			switch (this.getTemplet().type) {
			case ROOM_TYPE_PVP:
				future = GameTimer.getScheduled().schedule(() -> pvpStart(), 3, TimeUnit.SECONDS);
				break;
			}
		}
	}
	
	public void roleHpSync(ISession session, int playerId, int hp) {
		Collection<ISession> sessions = this.sessions.values();
		FightMsgSend.hpSync(sessions, session, playerId, hp);
	}
	
	public void itemSync(ISession session, int playerId, int itemId) {
		Collection<ISession> sessions = this.sessions.values();
		FightMsgSend.itemSync(sessions, session, playerId, itemId);
	}
	
	public void useItemSync(int playerId, int targetId, int itemId, boolean mainSkill) {
		Collection<ISession> sessions = this.sessions.values();
		FightMsgSend.useItemSync(sessions, playerId, targetId, itemId, mainSkill);
	}
	
	public void heroDiedSync(int playerId) {
		Collection<ISession> sessions = this.sessions.values();
		FightMsgSend.heroDiedSync(sessions, playerId);
	}

	public boolean heroDied(int playerId) {
		BattleRole role = getBattleRole(playerId);
		if (role == null) {
			return false;
		}
		if (role.isDead()) {
			return false;
		}
		role.setDead(true);
		
		heroDiedSync(playerId);
		
		int winner = 0;
		for (BattleRole fighter : this.getBattleRoles().values()) {
			if (!fighter.isDead()) {
				if (winner != 0) {
					return true;
				}
				winner = fighter.getPlayerId();
			}
		}
		if (winner != 0) {
			settlement(winner);
		}
		return true;
	}
	
	private void pvpStart() {
		interruptTimer();
		Collection<ISession> sessions = this.sessions.values();
		FightMsgSend.startGame(sessions);
		syncManager.start(String.valueOf(roomId));
	}

	public boolean isInRoom(int playerId) {
		return getBattleRole(playerId) != null;
	}

	@Override
	public void gameEnd() {
		this.destroy();
	}

	@Override
	public void settlement(int playerId) {
		if (getState() == ROOM_DESTORY || getState() == ROOM_STATE_END) {
			return;
		}
		state = ROOM_STATE_END;
		try {
			for (BattleRole fighter : this.getBattleRoles().values()) {
				FightMsgSend.settlement(this.getSession(fighter.getPlayerId()), fighter.getPlayerId(), fighter.getPlayerId() == playerId);
			}
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
			// TODO �ͻ�����ʾ
		}
		/*
		 * ����ʱ15�롣��
		 */
		logger.info("���䣺{}������ʱ15����Զ���ʼ��", this.roomId);
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
		 * ����ʱ45�롣��
		 */
		int seconds = 45000;
		future = GameTimer.getScheduled().schedule(() -> notice(), seconds, TimeUnit.MILLISECONDS);
		logger.info("���䣺{}������ʱ60����Զ���ʼ����ʼ��ʱ��", this.roomId);
	}
	
	public void interruptTimer() {
		if (future != null && future.getDelay(TimeUnit.MILLISECONDS) > 0) {
			future.cancel(false);
			logger.info("���䣺{}����ֹ��ʱ", this.roomId);
		}
	}
	
	public int getPlayState() {
		return playState;
	}

	public void setPlayState(int playState) {
		this.playState = playState;
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