package sys;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.ArrayList;
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
 * 游戏房间管理器
 * 
 */
public class GameRoomAssign implements Runnable, UncaughtExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GameRoomAssign.class);

	private static final int INTERVAL = 1000;

	/** 分配超时时间（秒） */
	public static final int LAST_ASSIGN_TIME = 30;
	
	private static GameRoomAssign instance;

	private static HashMap<Integer, PlayerInfo> players;

	private static LinkedBlockingQueue<Integer> queue;

	private HashMap<Integer, Integer> numbers = new HashMap<>();
	
	public static GameRoomAssign getInstance() {
		if (instance == null) {
			instance = new GameRoomAssign();
		}
		return instance;
	}
	
	private GameRoomAssign() {
		numbers.put(1, 2);
		numbers.put(2, 5);
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
		logger.info("服务器开启，分配游戏房间线程开始。");
		while (true) {
			/*
			 * 服务器开启状态判定
			 */
			if (!ServerStaticInfo.opened) {
				logger.info("服务器关闭，分配游戏房间线程结束。");
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
					
					/*
					 * 是否已超时
					 */
					boolean timeOver = false;
					if (now.after(assignTime)) {
						timeOver = true;
					}
					
					/*
					 * 按类型获取匹配队列中的玩家列表
					 */
					int assignType = playerInfoA.getAssignType();
					ArrayList<Integer> list = RedisProxy.getInstance().getPlayerMatchingList(playerInfoA.getAssignType());
					
					/*
					 * 需在玩家数量达到房间人数上限或分配超时，进入下一步
					 * 未达到条件则继续等待
					 */
					if (list.size() < numbers.get(assignType) && !timeOver) {
						queue.add(playerIdA);
						Thread.sleep(INTERVAL);
						continue;
					}
					
					ArrayList<Integer> roomPlayerList = new ArrayList<>();
					for (int playerId : list) {
						RedisProxy.getInstance().removePlayerInMatchingQueue(playerId, assignType);
						if (RedisProxy.getInstance().removePlayerMatching(playerId) > 0) {
							roomPlayerList.add(playerId);
						} else {
							players.remove(playerId);
							Thread.sleep(INTERVAL);
						}
					}
					
					if (roomPlayerList.size() < 2) {
						// 将已分配玩家重塞回分配队伍。
						addPlayerInMatchingList(roomPlayerList, assignType, playerInfoA);
						Thread.sleep(INTERVAL);
						continue;
					}
					
					/*
					 *  分配服务器，Redis创建房间，生成房间号
					 */
					ServerInfo serverInfo = distributeServer();
					if (serverInfo == null) {
						logger.error("创建房间失败。无可分配的游戏服。");
						// 尝试重创建房间，或直接将已分配玩家重塞回分配队伍。
						addPlayerInMatchingList(roomPlayerList, assignType, playerInfoA);
						Thread.sleep(INTERVAL * 3);
						continue;
					}
					String serverId = serverInfo.getServerId();
					templet.assignType = assignType;
					RoomInfo roomInfo = RedisProxy.getInstance().addRoomInfo(templet, serverId);
					
					if (roomInfo == null) {
						logger.error("创建房间失败。");
						// 尝试重创建房间，或直接将已分配玩家重塞回分配队伍。
						addPlayerInMatchingList(roomPlayerList, assignType, playerInfoA);
						Thread.sleep(INTERVAL * 3);
						continue;
					}
					
					/*
					 * 房间创建成功
					 * 通知各个玩家连接游戏服，进入房间 
					 */
					playerInfoA.setRoomId(roomInfo.getRoomId());
					RedisProxy.getInstance().updatePlayerInfo(playerInfoA, "roomId");
					// 向playerA发送消息，serverInfo
					ISession session = SessionMemory.getInstance().getSession(playerIdA);
					LoginMessageSend.assignSuccess(session);
					LoginMessageSend.connGameServer(session, serverInfo);
					logger.info("匹配成功。玩家Id：{}，RoomId：{}，分配至游戏服：{}", playerInfoA, roomInfo.getRoomId(), serverInfo.getServerId());
					
					for (int playerId : list) {
						if (playerId == playerIdA) {
							continue;
						}
						PlayerInfo playerInfo = players.get(playerId);
						if (playerInfo == null) {
							playerInfo = RedisProxy.getInstance().getPlayerInfo(playerId);
						}
						playerInfo.setRoomId(roomInfo.getRoomId());
						RedisProxy.getInstance().updatePlayerInfo(playerInfo, "roomId");
						logger.info("匹配成功。玩家Id：{}，RoomId：{}，分配至游戏服：{}", playerId, roomInfo.getRoomId(), serverInfo.getServerId());
						
						ISession sessionB = SessionMemory.getInstance().getSession(playerId);
						if (sessionB == null) {
							// 向playerB所在服广播，serverInfo
							RedisProxy.getInstance().playerNotice(playerInfo.getServerId(), playerId, serverInfo.toString());
						} else {
							LoginMessageSend.assignSuccess(sessionB);
							LoginMessageSend.connGameServer(sessionB, serverInfo);
						}
					}
				}
			} catch (Exception e) {
				ErrorPrint.print(e);
			}
			
		}
	}
	
	public void addPlayerInMatchingList(ArrayList<Integer> list, int assignType, PlayerInfo playerInfo) {
		for (int playerId : list) {
			if (playerId == playerInfo.getPlayerId()) {
				addPlayerInfo(playerInfo);
				continue;
			}
			RedisProxy.getInstance().addPlayerMatching(playerId);
			RedisProxy.getInstance().addPlayerInMatchingQueue(playerId, assignType);
		}
	}
	
	public boolean assignRobot(PlayerInfo playerInfoA) {
		return true;
	}
	
	/**
	 * 分配一个服务器进行房间创建
	 * 
	 * @param gameType
	 * @return
	 */
	public ServerInfo distributeServer() {
		List<ServerInfo> serverList = RedisProxy.getInstance().getServerList();
		if (serverList.size() <= 0) {
			return null;
		}
		
		// 游戏类型匹配
		List<Integer> weights = new LinkedList<Integer>();
		Iterator<ServerInfo> iterator = serverList.iterator();
		while (iterator.hasNext()) {
			ServerInfo serverInfo = iterator.next();
			// 服务器不可用或不支持玩法
			if (serverInfo.getSuspend() > 0) { 
				iterator.remove();
				continue;
			}

			int serverWeight = 10000;
			ServerStatus serverStatus = RedisProxy.getInstance().getServerStatus(serverInfo.getServerId());
			if (serverStatus != null) {
				// 游戏服超过2秒无心跳记录
				if ((Calendar.getInstance().getTimeInMillis() / 1000) - serverStatus.getLastHeartbeat() > 2) {
					iterator.remove();
					continue;
				}
				serverWeight = serverWeight - serverStatus.getRoomCount();
			}
			weights.add(serverWeight);
		}
		
		// 当前活跃房间数权重计算
		if (serverList.size() > 0) {
			return Tools.randomWeightObject(serverList, weights);
		}
		return null;
	}
	
	public boolean addPlayerInfo(PlayerInfo playerInfo) {
		if (SessionMemory.getInstance().getSession(playerInfo.getPlayerId()) == null) {
			return false;
		}
		if (numbers.get(playerInfo.getAssignType()) == null) {
			return false;
		}
		if (!RedisProxy.getInstance().addPlayerMatching(playerInfo.getPlayerId())) {
			return false;
		}
		players.put(playerInfo.getPlayerId(), playerInfo);
		if (!queue.add(playerInfo.getPlayerId())) {
			players.remove(playerInfo.getPlayerId(), playerInfo);
			return false;
		}
		RedisProxy.getInstance().addPlayerInMatchingQueue(playerInfo.getPlayerId(), playerInfo.getAssignType());
		logger.info("玩家：{}，进入匹配队列。", playerInfo.getPlayerId());
		return true;
	}
	
	public boolean removePlayerInfo(int playerId) {
		if (RedisProxy.getInstance().removePlayerMatching(playerId) == 0) {
			return false;
		}
		players.remove(playerId);
		logger.info("玩家：{}，移出匹配队列。", playerId);
		return true;
	}

	public Integer getNumber(int type) {
		return numbers.get(type);
	}
	
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		ErrorPrint.print(e);
		System.err.println("	at sys.GameRoomAssign(GameRoomAssign.class:0)");
		start();
	}
}
