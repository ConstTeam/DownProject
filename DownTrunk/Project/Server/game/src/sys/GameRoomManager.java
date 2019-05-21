package sys;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.ServerStaticInfo;
import config.ConfigData;
import config.model.guide.GuideGateModel;
import memory.LockMemory;
import module.scene.GameRoom;
import module.scene.RoomConst;
import module.templet.GuideTemplet;
import module.templet.PVPTemplet;
import module.templet.RobotTemplet;
import module.templet.TempletBase;
import redis.RedisProxy;
import redis.data.RoomInfo;

/**
 * 游戏房间管理器
 * 
 */
public class GameRoomManager {

	private static final Logger logger = LoggerFactory.getLogger(GameRoomManager.class);

	/** 全局房间表<房间Id,GameRoom> */
	private static ConcurrentHashMap<Integer, GameRoom> rooms = new ConcurrentHashMap<>();

	private static GameRoomManager instance;
	
	/** 房间锁 */
	private static LockMemory lock = LockMemory.getInstance();

	public static GameRoomManager getInstance() {
		if (instance == null) {
			instance = new GameRoomManager();
		}

		return instance;
	}

	/**
	 * 创建房间
	 * 
	 * 通过Redis分配当前唯一房间号
	 * 
	 * @param gameType 游戏类型
	 * @param roomType 房间类型，如好友房，金币场
	 * @param templet 游戏规则模板
	 * @return
	 */
	public GameRoom addGameRoom(TempletBase templet) {
		RoomInfo roomInfo = RedisProxy.getInstance().addRoomInfo(templet, ServerStaticInfo.getServerId());
		if (roomInfo == null) {
			logger.error("创建房间失败！生成房间号失败！");
			return null;
		}
		return addGameRoom(roomInfo, templet.type);
	}
	
	/**
	 * 创建房间
	 * 
	 * 按房间模板创建游戏房
	 * 
	 * @param roomInfo Redis生成房间信息
	 * @return
	 */
	public GameRoom addGameRoom(RoomInfo roomInfo, int roomType) {
		roomInfo.setServerId(ServerStaticInfo.getServerId());
		GameRoom gameRoom = new GameRoom();
		TempletBase templet = null;
		switch (roomType) {
		case RoomConst.ROOM_TYPE_PVP:
			templet = new PVPTemplet();
			gameRoom.setTemplet(templet);
			break;
		case RoomConst.ROOM_TYPE_GUIDE:
			templet = new GuideTemplet(roomInfo.getArg1());
			GuideGateModel gate = ConfigData.guideGateModels.get(roomInfo.getArg1());
			gameRoom.setTemplet(templet);
			templet.firstCardNum = gate.HandCardCount;
			break;
		case RoomConst.ROOM_TYPE_ROBOT:
			templet = new RobotTemplet(roomInfo.getArg1());
			gameRoom.setTemplet(templet);
			break;
		}
		gameRoom.setRoomId(roomInfo.getRoomId());
		rooms.put(gameRoom.getRoomId(), gameRoom);
		logger.info("创建房间成功，房间号：" + gameRoom.getRoomId());
		RedisProxy.getInstance().addServerRoomIndex(gameRoom.getRoomId());
		return gameRoom;
	}
	
	/**
	 * 游戏规则模板解析
	 * 
	 * 按照游戏类型、房间类型，解析游戏规则为指定样式的模板
	 * 
	 * @param gameType 游戏类型
	 * @param roomType 房间类型
	 * @param rule 游戏规则
	 * @return
	 * @throws Exception
	 */
	public TempletBase parseTempletStr(int gameType, int roomType, String rule) throws Exception {
		logger.error("创建模板失败！类型不存在：" + gameType, new Throwable("create room templet error!"));
		return null;
	}
	
	/**
	 * 销毁房间
	 * 
	 * 按指定房间号销毁房间
	 * 
	 * @param roomId
	 */
	public void destroyRoom(int roomId) {
		GameRoom room = rooms.remove(roomId);
		RedisProxy.getInstance().delRoomInfo(room);
		room.destroy();
		logger.info("销毁房间成功，房间号：" + roomId);
	}
	
	/**
	 * 销毁全部房间
	 */
	public void destroyAllRoom() {
		for (GameRoom room : rooms.values()) {
			RedisProxy.getInstance().delRoomInfo(room);
			room.destroy();
		}
		rooms.clear();
	}
	
	public GameRoom getRoom(int roomId) {
		return rooms.get(roomId);
	}
	
	public Collection<GameRoom> getRooms() {
		return rooms.values();
	}

	public LockMemory getLock() {
		return lock;
	}
	
	/**
	 * 获取当前游戏服实时房间数量
	 * @return
	 */
	public int getRoomCount() {
		// XXX 需要换成更高效的计数
		return rooms.size();
	}
}
