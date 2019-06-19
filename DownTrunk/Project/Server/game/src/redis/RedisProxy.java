package redis;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import app.ServerStaticInfo;
import config.model.notice.NoticeRollModel;
import db.DBModuleConst;
import module.scene.GameRoom;
import module.templet.TempletBase;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;
import redis.data.PlayerInfo;
import redis.data.RoomInfo;
import redis.data.ServerInfo;
import redis.data.ServerOpen;
import redis.data.ServerStatus;
import redis.subscribe.GameRoomListener;
import redis.subscribe.HallPlayerListener;
import redis.subscribe.SubPubConst;
import util.ErrorPrint;
import util.TimeFormat;
import util.Tools;

/**
 * redis�����ӿ�
 * 
 */
public class RedisProxy {

	private static Logger logger = LoggerFactory.getLogger(RedisProxy.class);

	/** �����ɹ� */
	static final String STATUS_SUCC = "OK";

	/**
	 * redis�Ự����
	 */
	RedisSession redisSession;
	/**
	 * ȫ��ʵ������
	 */
	private static RedisProxy instance = null;

	/**
	 * ������������Ϣ
	 */
	private static final String SERVER_OPEN_KEY = "server_open";
	/**
	 * �������б���Ϣ
	 */
	private static final String SERVER_INFO_KEY = "server_info";
	/**
	 * ������״̬
	 */
	private static final String SERVER_STATUS_KEY = "server_status";
	/**
	 * �����Ϣ
	 */
	private static final String PLAYER_INFO_KEY = "player_info";
	/**
	 * ������Ϣ
	 */
	private static final String ROOM_INFO_KEY = "room_info";
	/**
	 * ��ҳ������б�
	 */
	private static final String ASSIGN_ROOM_KEY = "assign_room";
	/**
	 * �����������б�
	 */
	private static final String SERVER_ROOM_KEY = "server_room";
	/**
	 * �����������
	 */
	private static final String PLAYER_LIMIT = "player_limit";
	/**
	 * ��������
	 */
	private static final String NOTICE_ROLL = "notice_roll";
	/**
	 * ������а�
	 */
	private static final String GOLD_RANKING_KEY = "ranking_list_gold";
	/**
	 * ��ֵ֪ͨ
	 */
	private static final String RECHARGE_NOTICE = "recharge_notice";
	/**
	 * �����豸
	 */
	private static final String TEST_DEVICE_ID_KEY = "test_device_id";
	/**
	 * �Ѷ������
	 */
	private static final String PLAYER_FORBIDDEN_KEY = "player_forbidden";
	
	/**
	 * ���ƥ��
	 */
	private static final String PLAYER_MATCHING_KEY = "player_matching";
	
	/**
	 * ���ָ����Ϣ
	 */
	private static final String PLAYER_GUIDE_KEY = "player_guide_info";
	
	/**
	 * ���ָ����Ϣ
	 */
	private static final String PLAYER_GUIDE_ID_KEY = "player_guide_id_info";
	
	/**
	 * ���ܿ���
	 */
	private static final String FUNCTION_SWITCH = "function_switch";
	
	/**
	 * GM�豸��
	 */
	private static final String GM_DEVICE_KEY = "gm_device";

	/**
	 * ���Ͽ�������
	 */
	private static final String CHAIN_CARD_PACK = "chain_card_pack";
	
	/**
	 * ��ȡʵ������
	 * 
	 * @return
	 */
	public static RedisProxy getInstance() {
		if (instance == null) {
			instance = new RedisProxy();
		}
		return instance;
	}

	/**
	 * ����
	 * 
	 */
	private RedisProxy() {
		instance = this;
		RedisData.cachedServerInfo = new ConcurrentHashMap<String, ServerInfo>();
	}

	/**
	 * ��ʼ���Ự
	 * 
	 * @param argsfile
	 * @return
	 * @throws Exception
	 */
	public boolean init(String argsfile) throws Exception {
		Properties props = new Properties();
		String str;
		Reader in = new BufferedReader(new InputStreamReader(new FileInputStream(argsfile), "gbk"));
		props.load(in);
		String redisHost = (String) props.get("redisHost");
		if (redisHost != null && redisHost.length() > 0) {
			redisSession = new RedisSession();
			int maxTotal = 300;
			int maxIdle = 10;
			int maxWait = 10000;

			GenericObjectPoolConfig config = new GenericObjectPoolConfig();
			if ((str = (String) props.get("redisMaxActive")) != null)
				maxTotal = Integer.parseInt(str);
			if ((str = (String) props.get("redisMaxIdle")) != null)
				maxIdle = Integer.parseInt(str);
			if ((str = (String) props.get("redisMaxWait")) != null)
				maxWait = Integer.parseInt(str);

			config.setMaxTotal(maxTotal);
			config.setMaxIdle(maxIdle);
			config.setMaxWaitMillis(maxWait);
			config.setTestOnBorrow(true);

			int redisPort = 6379;
			if ((str = (String) props.get("redisPort")) != null)
				redisPort = Integer.parseInt(str);
			int redisTimeout = 3000;
			if ((str = (String) props.get("redisTimeout")) != null) {
				redisTimeout = Integer.parseInt(str);
			}
			String redisPass = (String) props.get("password");
			int dbIndex = Integer.parseInt(props.getProperty("redisDb"));
			if (!redisSession.init(redisHost, redisPort, redisTimeout, redisPass, dbIndex, config)) {
				logger.info("init redis proxy failed, host: {}, port: {}", redisHost, redisPort);
				return false;
			}
		}
		return true;
	}

	/**
	 * Redis����
	 * 
	 * @return
	 */
	public boolean save() {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				if (jedis.save().equalsIgnoreCase("ok")) {
					return true;
				}
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return false;
	}

	/**
	 * ��ȡ����Ự
	 * 
	 * @return
	 */
	public RedisSession getRedisSession() {
		return redisSession;
	}

	/**
	 * ��ȡָ��Id�ķ�������Ϣ
	 * 
	 * @param serverId
	 * @return
	 */
	public ServerInfo getServerInfo(String serverId) {
		ServerInfo serverInfo = RedisData.cachedServerInfo.get(serverId);
		if (serverInfo != null) {
			return serverInfo;
		}
		return hget(ServerInfo.class, SERVER_INFO_KEY, serverId);
	}

	/**
	 * ��ȡָ��Id�ķ�����������Ϣ
	 * 
	 * @return
	 */
	public ServerOpen getServerOpenMessage() {
		return get(ServerOpen.class, SERVER_OPEN_KEY);
	}
	
	public void clearDatabase() {
		// ��ȡjedis����
		Jedis jedis = redisSession.getJedis();
		if (jedis == null) {
			return;
		}
		jedis.flushDB();
		logger.info("Redis��������");
	}

	/**
	 * ���·�������Ϣ
	 * 
	 * @return
	 */
	public boolean serverInfoRegister() {
		ServerInfo info = new ServerInfo(1);
		return hset(SERVER_INFO_KEY, info.getServerId(), info.toString());
	}
	
	public boolean serverInfoSuspend() {
		ServerInfo info = new ServerInfo(1);
		info.setSuspend(1);
		return hset(SERVER_INFO_KEY, info.getServerId(), info.toString());
	}

	/**
	 * ��ȡ�������б�
	 * 
	 * @return
	 */
	public List<ServerInfo> getServerList() {
		if (RedisData.cachedServerInfo.size() == 0) {
			RedisProxy.getInstance().updateServerListCache();
		}
		List<ServerInfo> serverList = new ArrayList<ServerInfo>(RedisData.cachedServerInfo.size());
		serverList.addAll(RedisData.cachedServerInfo.values());
		return serverList;
	}

	/**
	 * ���·������б���
	 * 
	 * @return
	 */
	public boolean updateServerListCache() {
		// ��ȡjedis����
		Jedis jedis = redisSession.getJedis();
		if (jedis == null) {
			return false;
		}
		Map<String, String> serverMap = null;
		try {
			serverMap = jedis.hgetAll(SERVER_INFO_KEY);
		} finally {
			jedis.close();
		}
		if (serverMap == null) {
			return false;
		}

		// ��ת�洢��������Ϣ
		Map<String, ServerInfo> serverInfoMap = new ConcurrentHashMap<String, ServerInfo>();

		for (Entry<String, String> entry : serverMap.entrySet()) {
			try {
				ServerInfo serverInfo = ServerInfo.fromJson(entry.getValue());
				if (serverInfo == null || serverInfo.getSuspend() == 1) {
					continue;
				}

				serverInfoMap.put(serverInfo.getServerId(), serverInfo);
				if (!RedisData.cachedServerInfo.containsKey(serverInfo.getServerId())) {
					logger.info("detect newly server info: " + serverInfo.toString());
				}
			} catch (Exception e) {
				ErrorPrint.print(e);
			}
		}

		// ��ⱻɾ���ķ�����
		try {
			Set<String> serverIds = RedisData.cachedServerInfo.keySet();
			for (String serverId : serverIds) {
				if (!serverInfoMap.containsKey(serverId)) {
					logger.info("detect removed server info: " + RedisData.cachedServerInfo.get(serverId).toString());
				}
			}
		} catch (Exception e) {
			ErrorPrint.print(e);
		}

		// �滻�����еķ������б�
		RedisData.cachedServerInfo = serverInfoMap;
		logger.debug("update cached server list success, count: " + RedisData.cachedServerInfo.size());
		return true;
	}

	/**
	 * ��ȡ������״̬
	 * 
	 * @param serverId
	 * @return
	 */
	public ServerStatus getServerStatus(String serverId) {
		if (Tools.isEmptyString(serverId)) {
			return null;
		}
		ServerStatus serverState = hget(ServerStatus.class, SERVER_STATUS_KEY, serverId);
		if (serverState != null && serverState.idIsValid()) {
			return serverState;
		}
		return null;
	}

	/**
	 * ���·�����״̬
	 * 
	 * @return
	 */
	public boolean updateServerStatus() {
		ServerStatus status = new ServerStatus();
		return hset(SERVER_STATUS_KEY, status.getServerId(), status.toString());
	}

	/**
	 * 
	 * ����Room��Ϣ
	 * 
	 * ���RoomIdֱ�����ظ�Ϊֹ�� ����Room��Ϣ��ͬʱ�����Ӷ�Ӧ�Ľ�ҳ������ֲ���RoomId��¼
	 * 
	 * @param gameType
	 * @param roomType
	 * @param templet
	 * @return
	 */
	public RoomInfo addRoomInfo(TempletBase templet, String serverId) {
		RoomInfo roomInfo = new RoomInfo(serverId);
		roomInfo.randomRoomId();
		roomInfo.setArg1(templet.arg1);
		roomInfo.setType(templet.type);
		
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				while (jedis.hsetnx(String.format("%s:%s", ROOM_INFO_KEY, String.valueOf(roomInfo.getRoomId())),
						"roomId", String.valueOf(roomInfo.getRoomId())) == 0) {
					roomInfo.randomRoomId();
				}
				
				updateRoomInfo(roomInfo, null);
				return roomInfo;
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return null;
	}
	
	public void addServerRoomIndex(int roomId) {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				String key = String.format("%s:%s", SERVER_ROOM_KEY, ServerStaticInfo.getServerId());
				jedis.lpush(key, String.valueOf(roomId));
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
	}
	
	/**
	 * 
	 * ����Room��Ϣ
	 * 
	 * attrNameΪNullʱ����roomInfo������Valueֵ��ΪNull�����ԣ��������ָ������ֵ��
	 * 
	 * @param roomInfo
	 * @param attrName
	 * @return
	 */
	public boolean updateRoomInfo(RoomInfo roomInfo, String attrName) {
		if (roomInfo == null) {
			return false;
		}

		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				String key = String.format("%s:%s", ROOM_INFO_KEY, String.valueOf(roomInfo.getRoomId()));
				if (!Tools.isEmptyString(attrName)) {
					String attrValue = roomInfo.toJson().getString(attrName);
					if (attrValue == null) {
						throw new RuntimeException("ServerState AttrName Illegal");
					}
					jedis.hset(key, attrName, attrValue);
				} else {
					JSONObject jsonInfo = roomInfo.toJson();
					Set<Entry<String, Object>> entrySet = jsonInfo.entrySet();
					for (Entry<String, Object> entry : entrySet) {
						if (entry.getValue() != null) {
							jedis.hset(key, entry.getKey(), entry.getValue().toString());
						}
					}
				}
				return true;
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return false;
	}

	public boolean updateRoomServer(RoomInfo roomInfo) {
		if (roomInfo == null) {
			return false;
		}

		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				String key = String.format("%s:%s", ROOM_INFO_KEY, String.valueOf(roomInfo.getRoomId()));
				RoomInfo info = getRoomInfo(jedis, String.valueOf(roomInfo.getRoomId()));
				if ("NONE".equals(info.getServerId())) {
					jedis.hset(key, "serverId", roomInfo.getServerId());
					return true;
				}
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return false;
	}
	
	/**
	 * ��ȡRoom��Ϣ
	 * 
	 * @param roomId
	 * @return
	 */
	public RoomInfo getRoomInfo(int roomId) {
		if (roomId == 0) {
			return null;
		}

		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				return getRoomInfo(jedis, String.valueOf(roomId));
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return null;
	}

	/**
	 * ��ȡRoom��Ϣ
	 * 
	 * @param jedis
	 * @param roomId
	 * @return
	 */
	private RoomInfo getRoomInfo(Jedis jedis, String roomId) {
		String key = String.format("%s:%s", ROOM_INFO_KEY, roomId);
		Map<String, String> infoMap = jedis.hgetAll(key);
		if (infoMap == null || infoMap.size() <= 0) {
			return null;
		}

		JSONObject infoJson = (JSONObject) JSON.toJSON(infoMap);
		RoomInfo roomInfo = RoomInfo.fromJson(infoJson.toJSONString());
		if (roomInfo != null) {
			return roomInfo;
		}
		return null;
	}

	/**
	 * �Ƴ�Room��Ϣ
	 * 
	 * @param room
	 * @return
	 */
	public long delRoomInfo(GameRoom room) {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				if (room != null) {
					return delRoomInfo(jedis, room.getRoomId());
				}
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return 0;
	}

	/**
	 * �Ƴ�Room��Ϣ
	 * 
	 * �Ƴ�Room��Ϣ��ͬʱ���Ƴ���Ӧ�Ľ�ҳ������ֲ���RoomId��¼
	 * 
	 * @param jedis
	 * @param roomId
	 * @param roomType
	 * @param gameType
	 * @param clubId
	 * @param rule
	 * @return
	 */
	private long delRoomInfo(Jedis jedis, int roomId) {
		String key = String.format("%s:%s", SERVER_ROOM_KEY, ServerStaticInfo.getServerId());
		jedis.lrem(key, 1, String.valueOf(roomId));

		key = String.format("%s:%s", ROOM_INFO_KEY, String.valueOf(roomId));
		return jedis.del(key);
	}

	/**
	 * ��������������б��м�¼�����з���
	 */
	public void destroyAllRoom() {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				String key = String.format("%s:%s", SERVER_ROOM_KEY, ServerStaticInfo.getServerId());
				List<String> list = jedis.lrange(key, 0, -1);
				for (String id : list) {
					RoomInfo room = getRoomInfo(jedis, id);
					if (room == null || "NONE".equals(room.getServerId())) {
						continue;
					}
					delRoomInfo(jedis, room.getRoomId());
				}
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
	}

	/**
	 * ��gameType��ȡ��ҷ�Room�б�
	 * 
	 * @param gameType
	 * @return
	 */
	public ArrayList<RoomInfo> getRoomList(int gameType, boolean isAll) {
		if (gameType == 0) {
			return null;
		}

		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				ArrayList<RoomInfo> rooms = new ArrayList<>();
				String key = String.format("%s:%s", ASSIGN_ROOM_KEY, gameType);
				List<String> list = jedis.lrange(key, 0, -1);
				for (String id : list) {
					RoomInfo roomInfo = getRoomInfo(jedis, id);
					if (roomInfo != null) {
						rooms.add(roomInfo);
						if (rooms.size() == 50) {
							break;
						}
					} else {
						jedis.lrem(key, 1, id);
					}
				}
				return rooms;
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return null;
	}
	
	/**
	 * �����ҷ��б��м�¼��RoomId
	 * 
	 * @param gameType
	 * @param roomId
	 * @return
	 */
	public long delAssignRoomInfo(int gameType, int roomId) {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				String key = String.format("%s:%s", ASSIGN_ROOM_KEY, gameType);
				return jedis.lrem(key, 1, String.valueOf(roomId));
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return 0;
	}

	/**
	 * ��ȡ�����Ϣ
	 * 
	 * @param playerId
	 * @return
	 */
	public PlayerInfo getPlayerInfo(int playerId) {
		if (playerId == 0) {
			return null;
		}

		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				return getPlayerInfo(jedis, String.valueOf(playerId));
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return null;
	}

	/**
	 * ��ȡ�����Ϣ
	 */
	private PlayerInfo getPlayerInfo(Jedis jedis, String playerId) {
		String key = String.format("%s:%s", PLAYER_INFO_KEY, playerId);
		Map<String, String> infoMap = jedis.hgetAll(key);
		if (infoMap == null || infoMap.size() <= 0) {
			return null;
		}

		JSONObject infoJson = (JSONObject) JSON.toJSON(infoMap);
		return PlayerInfo.fromJson(infoJson.toJSONString());
	}
	
	private boolean isExistPlayerInfo(Jedis jedis, int playerId) {
		String key = String.format("%s:%s", PLAYER_INFO_KEY, String.valueOf(playerId));
		Map<String, String> infoMap = jedis.hgetAll(key);
		if (infoMap == null) {
			return false;
		}
		return true;
	}

	/**
	 * ���������Ϣ
	 * 
	 * ���PlayerIdֱ�����ظ�Ϊֹ��
	 * 
	 * @return
	 */
	public PlayerInfo addPlayerInfo() {
		PlayerInfo playerInfo = new PlayerInfo();
		playerInfo.randomPlayerId();
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				while (jedis.hsetnx(String.format("%s:%s", PLAYER_INFO_KEY, String.valueOf(playerInfo.getPlayerId())),
						"playerId", playerInfo.toString()) == 0) {
					playerInfo.randomPlayerId();
				}
				updatePlayerInfo(playerInfo);
				return playerInfo;
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return null;
	}
	
	public PlayerInfo addPlayerInfo(int playerId) {
		PlayerInfo playerInfo = new PlayerInfo();
		playerInfo.setPlayerId(playerId);
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				updatePlayerInfo(playerInfo);
				return playerInfo;
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return null;
	}
	
	/**
	 * ���������Ϣ
	 * 
	 * @param playerInfo
	 * @return
	 */
	public boolean updatePlayerInfo(PlayerInfo playerInfo) {
		return updatePlayerInfo(playerInfo, null);
	}

	/**
	 * ���������Ϣ
	 * 
	 * attrNameΪNullʱ����playerInfo������Valueֵ��ΪNull�����ԣ��������ָ������ֵ��
	 * 
	 * @param playerInfo
	 * @param attrName
	 * @return
	 */
	public boolean updatePlayerInfo(PlayerInfo playerInfo, String attrName) {
		if (playerInfo == null) {
			return false;
		}

		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				if (!isExistPlayerInfo(jedis, playerInfo.getPlayerId())) {
					return false;
				}
				String key = String.format("%s:%s", PLAYER_INFO_KEY, String.valueOf(playerInfo.getPlayerId()));
				if (!Tools.isEmptyString(attrName)) {
					String attrValue = playerInfo.toJson().getString(attrName);
					if (attrValue == null) {
						throw new RuntimeException("PlayerInfo AttrName Illegal");
					}
					jedis.hset(key, attrName, attrValue);
				} else {
					JSONObject jsonInfo = playerInfo.toJson();
					Set<Entry<String, Object>> entrySet = jsonInfo.entrySet();
					for (Entry<String, Object> entry : entrySet) {
						if (entry.getValue() != null) {
							jedis.hset(key, entry.getKey(), entry.getValue().toString());
						}
					}
				}
				return true;
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return false;
	}

	/**
	 * ��������Ϣ
	 * 
	 * @param playerId
	 * @return
	 */
	public long delPlayerInfo(int playerId) {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				String key = String.format("%s:%s", PLAYER_INFO_KEY, String.valueOf(playerId));
				return jedis.del(key);
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return 0;
	}

	/**
	 * �Ƿ��ܽ�����Ҵ���
	 * 
	 * @return ����ǰע�����������ע������������ޱȽϣ��統ǰע����������ϵͣ��򷵻�true
	 */
	public boolean isCanCreatePlayer() {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				int limit = DBModuleConst.PLAYER_LIMIT;
				String key = String.format("%s%s", PLAYER_INFO_KEY, "*");
				Set<String> set = jedis.keys(key);
				String str = jedis.get(PLAYER_LIMIT);
				if (!Tools.isEmptyString(str)) {
					limit = Integer.parseInt(str);
				}
				if (set.size() >= limit) {
					return false;
				}
				return true;
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return true;
	}

	/** 
	 * ��������Ϣ����
	 * 
	 * @return
	 */
	public boolean hallNoticeRegister() {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				final HallPlayerListener listener = new HallPlayerListener();
				jedis.psubscribe(listener, SubPubConst.PLAYER_NOTICE);
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return false;
	}
	
	/**
	 * ��Ϸ����Ϣ����
	 * @return
	 */
	public boolean roomNoticeRegister() {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				final GameRoomListener listener = new GameRoomListener();
				jedis.psubscribe(listener, "room.*");
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return false;
	}
	
	/**
	 * ���ֲ���Ϣ֪ͨ
	 * 
	 * @param roomId
	 * @return
	 */
	public boolean roomNotice(int roomId, String type) {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				jedis.publish("room." + roomId, type);
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return false;
	}
	
	/**
	 * ���ֲ���Ϣ֪ͨ
	 * 
	 * @param club
	 * @return
	 */
	public boolean clubNotice(int club, String type) {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				jedis.publish("club." + club, type);
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return false;
	}
	
	/**
	 * �����Ϣ֪ͨ
	 * 
	 * @param club
	 * @return
	 */
	public boolean playerNotice(String serverId, int playerId, String type) {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				jedis.publish(serverId + ".player." + playerId, type);
				logger.info(serverId + ".player." + playerId);
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return false;
	}

	/**
	 * ���½�Ҹ�����
	 * 
	 * @param playerId
	 * @param gold
	 */
	public void addPlayerGoldRanking(int playerId, int gold) {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				jedis.zadd(GOLD_RANKING_KEY, gold, String.valueOf(playerId));
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
	}
	
	/**
	 * ���½�Ҹ�����
	 * 
	 * @param playerId
	 * @param gold
	 */
	public void updatePlayerGoldRanking(int playerId, int gold) {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				jedis.zincrby(GOLD_RANKING_KEY, gold, String.valueOf(playerId));
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
	}

	/**
	 * ��ȡ��Ҹ�����
	 * 
	 * @return
	 */
	public Set<Tuple> getGoldRankingList() {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				return jedis.zrevrangeWithScores(GOLD_RANKING_KEY, 0, 9);
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return null;
	}

	/**
	 * ��ȡ��������
	 * 
	 * @return
	 */
	public List<NoticeRollModel> getNoticeRollInfo() {
		List<NoticeRollModel> noticeRollInfoList = new ArrayList<>();
		Calendar now = Calendar.getInstance();
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				List<String> list = jedis.lrange(NOTICE_ROLL, 0, -1);

				/*
				 *  GM���õĹ�������
				 */
				for (String string : list) {
					NoticeRollModel noticeRollInfo = NoticeRollModel.fromJson(string);
					if (TimeFormat.isBetweenTime(noticeRollInfo.getStartTime(), noticeRollInfo.getEndTime(), now.getTimeInMillis())) {
						noticeRollInfoList.add(noticeRollInfo);
					}
				}
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return noticeRollInfoList;
	}

	/**
	 * ��ӹ�������
	 * 
	 * @return
	 */
	public NoticeRollModel addNoticeRollInfo(NoticeRollModel noticeRollInfo) {
		Jedis jedis = redisSession.getJedis();

		if (jedis != null) {
			try {
				jedis.lpush(NOTICE_ROLL, noticeRollInfo.toJson().toString());
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return null;
	}

	public boolean isHaveRechargeNotice(int playerId) {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				String string = jedis.hget(RECHARGE_NOTICE, String.valueOf(playerId));
				if (Tools.isEmptyString(string)) {
					return false;
				}
				jedis.hdel(RECHARGE_NOTICE, String.valueOf(playerId));
				return true;
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return false;
	}
	
	public void removeRechargeNotice(int playerId) {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				jedis.hdel(RECHARGE_NOTICE, String.valueOf(playerId));
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
	}
	
	public void addRechargeNotice(int playerId) {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				jedis.hset(RECHARGE_NOTICE, String.valueOf(playerId), String.valueOf(Calendar.getInstance().getTimeInMillis()));
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
	}

	public boolean checkTestDeviceId(String deviceId) {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				return jedis.hexists(TEST_DEVICE_ID_KEY, deviceId);
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return false;
	}

	public String getPlayerForbidden(int playerId) {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				return jedis.hget(PLAYER_FORBIDDEN_KEY, String.valueOf(playerId));
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return null;
	}

	/*
	 * ��ȡ���Ƚ���ƥ���б�������
	 */
	public Integer getPlayerMatchingList(int playerId) {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				Set<String> hkeys = jedis.hkeys(PLAYER_MATCHING_KEY);
				for (String id : hkeys) {
					int pid = Integer.parseInt(id);
					if (pid != playerId) {
						return pid;
					}
				}
				return null;
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return null;
	}
	
	public boolean addPlayerMatching(int playerId) {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				Long res = jedis.hsetnx(PLAYER_MATCHING_KEY, String.valueOf(playerId), String.valueOf(Calendar.getInstance().getTimeInMillis()));
				return res == 1;
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return false;
	}
	
	public Long removePlayerMatching(int playerId) {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				Long res = jedis.hdel(PLAYER_MATCHING_KEY, String.valueOf(playerId));
				return res;
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return -1l;
	}
	
	public boolean savePlayerGuide(int playerId) {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				String key = String.format("%s:%s", PLAYER_GUIDE_KEY, String.valueOf(playerId));
				jedis.set(key, String.valueOf(playerId));
				return true;
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return false;
	}
	
	public boolean getPlayerGuide(int playerId) {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				String key = String.format("%s:%s", PLAYER_GUIDE_KEY, String.valueOf(playerId));
				String string = jedis.get(key);
				return Tools.isEmptyString(string);
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return false;
	}
	
	public boolean savePlayerGuideID(int playerId, int guideId) {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				String key = String.format("%s:%s", PLAYER_GUIDE_ID_KEY, String.valueOf(playerId));
				jedis.set(key, String.valueOf(guideId));
				return true;
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return false;
	}
	
	public int getPlayerGuideID(int playerId) {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				String key = String.format("%s:%s", PLAYER_GUIDE_ID_KEY, String.valueOf(playerId));
				String string = jedis.get(key);
				if (string != null) {
					return Integer.valueOf(string);
				}
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return 1;
	}
	
	public HashMap<String, Boolean> getFunctionSwitch() {
		HashMap<String, Boolean> result = new HashMap<>();
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				Map<String, String> infoMap = jedis.hgetAll(FUNCTION_SWITCH);
				if (infoMap == null || infoMap.size() <= 0) {
					return result;
				}
				Iterator<Entry<String, String>> iterator = infoMap.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<String, String> next = iterator.next();
					String key = next.getKey();
					String value = next.getValue();
					if (!Tools.isEmptyString(value)) {
						result.put(key, value.equalsIgnoreCase("true"));
					}
				}
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return result;
	}
	
	public boolean getFunctionSwitch(String funcName) {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				String info = jedis.hget(FUNCTION_SWITCH, funcName);
				if (Tools.isEmptyString(info)) {
					return false;
				}
				return info.equalsIgnoreCase("true");
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return false;
	}
	
	public void setFunctionSwitch(String funcName, boolean open) {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				jedis.hset(FUNCTION_SWITCH, funcName, String.valueOf(open));
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
	}
	
	public HashMap<String, Boolean> getGmDeviceList() {
		HashMap<String, Boolean> result = new HashMap<>();
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				Map<String, String> infoMap = jedis.hgetAll(GM_DEVICE_KEY);
				if (infoMap == null || infoMap.size() <= 0) {
					return result;
				}
				Iterator<Entry<String, String>> iterator = infoMap.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<String, String> next = iterator.next();
					String key = next.getKey();
					String value = next.getValue();
					if (!Tools.isEmptyString(value)) {
						result.put(key, value.equalsIgnoreCase("true"));
					}
				}
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return result;
	}
	
	public boolean isGmDevice(String deviceId) {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				String info = jedis.hget(GM_DEVICE_KEY, deviceId);
				if (Tools.isEmptyString(info)) {
					return false;
				}
				return info.equalsIgnoreCase("true");
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return false;
	}
	
	public void setGmDevice(String deviceId, boolean isGm) {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				jedis.hset(GM_DEVICE_KEY, deviceId, String.valueOf(isGm));
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
	}
	
	public HashMap<String, String> getChainCardPackCount(int packId) {
		HashMap<String, String> result = new HashMap<>();
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			Map<String, String> cardPackMap = null;
			try {
				cardPackMap = jedis.hgetAll(CHAIN_CARD_PACK);
				if (cardPackMap == null || cardPackMap.size() <= 0) {
					return result;
				}
				String packIdStr = "_" + packId + "_";
				Iterator<Entry<String, String>> iterator = cardPackMap.entrySet().iterator();
				while (iterator.hasNext()) {
					Entry<String, String> next = iterator.next();
					String key = next.getKey();
					String value = next.getValue();
					if (!Tools.isEmptyString(value) && Integer.parseInt(value) != 0 && key.indexOf(packIdStr) != -1) {
						result.put(key, value);
					}
				}
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return result;
	}
	
	public void delChainCardPackCount() {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				jedis.del(CHAIN_CARD_PACK);
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
	}
	
	public void setChainCardPackCount(String tableName, int count) {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				jedis.hset(CHAIN_CARD_PACK, tableName, String.valueOf(count));
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
	}
	
	public boolean hset(String key, String field, String value) {
		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				jedis.hset(key, field, value);
				return true;
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return false;
	}

	public <T> T hget(Class<T> t, String key, String field) {

		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				String info = jedis.hget(key, field);
				if (Tools.isEmptyString(info)) {
					return null;
				}
				T json = Tools.fromJson(info, t);
				if (json != null) {
					return json;
				}
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return null;
	}


	public <T> T get(Class<T> t, String key) {

		Jedis jedis = redisSession.getJedis();
		if (jedis != null) {
			try {
				String info = jedis.get(key);
				if (Tools.isEmptyString(info)) {
					return null;
				}
				T json = Tools.fromJson(info, t);
				if (json != null) {
					return json;
				}
			} catch (Exception e) {
				ErrorPrint.print(e);
			} finally {
				jedis.close();
			}
		}
		return null;
	}
}
