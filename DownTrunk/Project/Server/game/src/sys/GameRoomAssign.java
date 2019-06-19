package sys;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.ServerStaticInfo;
import memory.SessionMemory;
import message.hall.login.LoginMessageSend;
import module.templet.PVPTemplet;
import module.templet.TempletBase;
import net.ISession;
import redis.RedisProxy;
import redis.data.PlayerInfo;
import redis.data.RoomInfo;
import redis.data.ServerInfo;
import redis.data.ServerStatus;
import util.ErrorPrint;
import util.Tools;

/**
 * ��Ϸ���������
 * 
 */
public class GameRoomAssign implements Runnable, UncaughtExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GameRoomAssign.class);

	private static final int INTERVAL = 1000;

	public static final int LAST_ASSIGN_TIME = 180;
	
	private static GameRoomAssign instance;

	private static HashMap<Integer, PlayerInfo> players;

	private static LinkedBlockingQueue<Integer> queue;

	public static GameRoomAssign getInstance() {
		if (instance == null) {
			instance = new GameRoomAssign();
		}

		return instance;
	}
	
	private GameRoomAssign() {
	}

	public static void start() {
		GameRoomAssign inst = GameRoomAssign.getInstance();
		players = new HashMap<>();
		queue = new LinkedBlockingQueue<>();
		Thread thread = new Thread(inst, "GameRoomAssign");
		thread.setUncaughtExceptionHandler(inst);
		thread.start();
	}

	@Override
	public void run() {
		logger.info("������������������Ϸ�����߳̿�ʼ��");
		while (true) {
			/*
			 * ����������״̬�ж�
			 */
			if (!ServerStaticInfo.opened) {
				logger.info("�������رգ�������Ϸ�����߳̽�����");
				break;
			}
			TempletBase templet = new PVPTemplet();
			
			try {
				Integer playerIdA;
				PlayerInfo playerInfoA = null;
				while ((playerIdA = queue.take()) != null) {
					playerInfoA = players.get(playerIdA);
					if (playerInfoA == null) {
						continue;
					}
					Calendar now = Calendar.getInstance();
					Calendar assignTime = playerInfoA.getAssignTime();
					if (now.after(assignTime)) {
						if (assignRobot(playerInfoA)) {
							Thread.sleep(INTERVAL);
						} else {
							Thread.sleep(INTERVAL * 3);
						}
						continue;
					}

					Integer playerIdB = RedisProxy.getInstance().getPlayerMatchingList(playerIdA);
					if (playerIdB == null) {
						queue.add(playerIdA);
						Thread.sleep(INTERVAL);
						continue;
					}
					if (RedisProxy.getInstance().removePlayerMatching(playerIdA) == 0) {
						players.remove(playerIdA);
						Thread.sleep(INTERVAL);
						continue;
					}
					if (RedisProxy.getInstance().removePlayerMatching(playerIdB) == 0) {
						addPlayerInfo(playerInfoA);
						Thread.sleep(INTERVAL);
						continue;
					}
					
					/*
					 *  �����������Redis�������䣬���ɷ����
					 */
					ServerInfo serverInfo = distributeServer();
					if (serverInfo == null) {
						logger.error("��������ʧ�ܡ��޿ɷ������Ϸ����");
						// TODO �����ش������䣬��ֱ�ӽ�A B �����ط�����顣
						RedisProxy.getInstance().addPlayerMatching(playerIdB);
						addPlayerInfo(playerInfoA);
						Thread.sleep(INTERVAL * 3);
						continue;
					}
					String serverId = serverInfo.getServerId();
					RoomInfo roomInfo = RedisProxy.getInstance().addRoomInfo(templet, serverId);
					
					if (roomInfo == null) {
						logger.error("��������ʧ�ܡ�");
						// TODO �����ش������䣬��ֱ�ӽ�A B �����ط�����顣
						RedisProxy.getInstance().addPlayerMatching(playerIdB);
						addPlayerInfo(playerInfoA);
						Thread.sleep(INTERVAL * 3);
						continue;
					}
					playerInfoA.setRoomId(roomInfo.getRoomId());
					RedisProxy.getInstance().updatePlayerInfo(playerInfoA, "roomId");
					PlayerInfo playerInfoB = players.get(playerIdB);
					if (playerInfoB == null) {
						playerInfoB = RedisProxy.getInstance().getPlayerInfo(playerIdB);
					}
					playerInfoB.setRoomId(roomInfo.getRoomId());
					RedisProxy.getInstance().updatePlayerInfo(playerInfoB, "roomId");
					logger.info("ƥ��ɹ������A��{}�����B��{}����������Ϸ����{}", playerIdA, playerIdB, serverInfo.getServerId());
					// ��playerA������Ϣ��serverInfo
					ISession session = SessionMemory.getInstance().getSession(playerIdA);
					LoginMessageSend.assignSuccess(session);
					LoginMessageSend.connGameServer(session, serverInfo);
					
					ISession sessionB = SessionMemory.getInstance().getSession(playerIdB);
					if (sessionB == null) {
						// ��playerB���ڷ��㲥��serverInfo
						RedisProxy.getInstance().playerNotice(playerInfoB.getServerId(), playerIdB, serverInfo.toString());
					} else {
						LoginMessageSend.assignSuccess(sessionB);
						LoginMessageSend.connGameServer(sessionB, serverInfo);
					}
				}
			} catch (Exception e) {
				ErrorPrint.print(e);
			}
			
		}
	}
	
	public boolean assignRobot(PlayerInfo playerInfoA) {
		return true;
	}
	
	/**
	 * ����һ�����������з��䴴��
	 * 
	 * @param gameType
	 * @return
	 */
	public ServerInfo distributeServer() {
		List<ServerInfo> serverList = RedisProxy.getInstance().getServerList();
		if (serverList.size() <= 0) {
			return null;
		}
		
		// ��Ϸ����ƥ��
		List<Integer> weights = new LinkedList<Integer>();
		Iterator<ServerInfo> iterator = serverList.iterator();
		while (iterator.hasNext()) {
			ServerInfo serverInfo = iterator.next();
			// �����������û�֧���淨
			if (serverInfo.getSuspend() > 0) { 
				iterator.remove();
				continue;
			}

			int serverWeight = 10000;
			ServerStatus serverStatus = RedisProxy.getInstance().getServerStatus(serverInfo.getServerId());
			if (serverStatus != null) {
				// ��Ϸ������2����������¼
				if ((Calendar.getInstance().getTimeInMillis() / 1000) - serverStatus.getLastHeartbeat() > 2) {
					iterator.remove();
					continue;
				}
				serverWeight = serverWeight - serverStatus.getRoomCount();
			}
			weights.add(serverWeight);
		}
		
		// ��ǰ��Ծ������Ȩ�ؼ���
		if (serverList.size() > 0) {
			return Tools.randomWeightObject(serverList, weights);
		}
		return null;
	}
	
	public boolean addPlayerInfo(PlayerInfo playerInfo) {
		if (!RedisProxy.getInstance().addPlayerMatching(playerInfo.getPlayerId())) {
			return false;
		}
		players.put(playerInfo.getPlayerId(), playerInfo);
		if (!queue.add(playerInfo.getPlayerId())) {
			players.remove(playerInfo.getPlayerId(), playerInfo);
			return false;
		}
		logger.info("��ң�{}������ƥ����С�", playerInfo.getPlayerId());
		return true;
	}
	
	public boolean removePlayerInfo(int playerId) {
		if (RedisProxy.getInstance().removePlayerMatching(playerId) == 0) {
			return false;
		}
		players.remove(playerId);
		logger.info("��ң�{}���Ƴ�ƥ����С�", playerId);
		return true;
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		ErrorPrint.print(e);
		System.err.println("	at sys.GameRoomAssign(GameRoomAssign.class:0)");
		start();
	}
}
