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
 * ��Ϸ���������
 * 
 */
public class GameRoomManager {

	private static final Logger logger = LoggerFactory.getLogger(GameRoomManager.class);

	/** ȫ�ַ����<����Id,GameRoom> */
	private static ConcurrentHashMap<Integer, GameRoom> rooms = new ConcurrentHashMap<>();

	private static GameRoomManager instance;
	
	/** ������ */
	private static LockMemory lock = LockMemory.getInstance();

	public static GameRoomManager getInstance() {
		if (instance == null) {
			instance = new GameRoomManager();
		}

		return instance;
	}

	/**
	 * ��������
	 * 
	 * ͨ��Redis���䵱ǰΨһ�����
	 * 
	 * @param gameType ��Ϸ����
	 * @param roomType �������ͣ�����ѷ�����ҳ�
	 * @param templet ��Ϸ����ģ��
	 * @return
	 */
	public GameRoom addGameRoom(TempletBase templet) {
		RoomInfo roomInfo = RedisProxy.getInstance().addRoomInfo(templet, ServerStaticInfo.getServerId());
		if (roomInfo == null) {
			logger.error("��������ʧ�ܣ����ɷ����ʧ�ܣ�");
			return null;
		}
		return addGameRoom(roomInfo, templet.type);
	}
	
	/**
	 * ��������
	 * 
	 * ������ģ�崴����Ϸ��
	 * 
	 * @param roomInfo Redis���ɷ�����Ϣ
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
		logger.info("��������ɹ�������ţ�" + gameRoom.getRoomId());
		RedisProxy.getInstance().addServerRoomIndex(gameRoom.getRoomId());
		return gameRoom;
	}
	
	/**
	 * ��Ϸ����ģ�����
	 * 
	 * ������Ϸ���͡��������ͣ�������Ϸ����Ϊָ����ʽ��ģ��
	 * 
	 * @param gameType ��Ϸ����
	 * @param roomType ��������
	 * @param rule ��Ϸ����
	 * @return
	 * @throws Exception
	 */
	public TempletBase parseTempletStr(int gameType, int roomType, String rule) throws Exception {
		logger.error("����ģ��ʧ�ܣ����Ͳ����ڣ�" + gameType, new Throwable("create room templet error!"));
		return null;
	}
	
	/**
	 * ���ٷ���
	 * 
	 * ��ָ����������ٷ���
	 * 
	 * @param roomId
	 */
	public void destroyRoom(int roomId) {
		GameRoom room = rooms.remove(roomId);
		RedisProxy.getInstance().delRoomInfo(room);
		room.destroy();
		logger.info("���ٷ���ɹ�������ţ�" + roomId);
	}
	
	/**
	 * ����ȫ������
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
	 * ��ȡ��ǰ��Ϸ��ʵʱ��������
	 * @return
	 */
	public int getRoomCount() {
		// XXX ��Ҫ���ɸ���Ч�ļ���
		return rooms.size();
	}
}
