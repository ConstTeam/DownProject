package sys;

import java.lang.Thread.UncaughtExceptionHandler;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.ServerStaticInfo;
import module.scene.GameRoom;
import net.ByteBufferFactory;
import net.IByteBuffer;
import redis.RedisProxy;
import redis.data.PlayerInfo;
import util.ErrorPrint;

public class GameSyncManager implements Runnable, UncaughtExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(GameSyncManager.class);

	
	private static HashMap<Integer, InetAddress> ips = new HashMap<>();
	private static HashMap<Integer, Integer> ports = new HashMap<>();

	private static GameSyncManager instance;

	public static GameSyncManager getInstance() {
		if (instance == null) {
			instance = new GameSyncManager();
		}

		return instance;
	}

	public static void start() {
		GameSyncManager manager = GameSyncManager.getInstance();
		Thread thread = new Thread(manager, "GameSyncManager");
		thread.setUncaughtExceptionHandler(manager);
		thread.start();
	}

	@Override
	public void run() {

		DatagramSocket datagramSocket = null;
		byte[] buf = new byte[1024];

		try {
			datagramSocket = new DatagramSocket(8801);
		} catch (Exception e) {
			ErrorPrint.print(e);
		}
		logger.info("服务器开启，游戏位置同步线程开始。");

		while (true) {
			if (!ServerStaticInfo.opened) {
				logger.info("服务器关闭，游戏位置同步线程结束。");
				break;
			}
			try {
				// 定义接收数据的数据包
				DatagramPacket datagramPacket = new DatagramPacket(buf, 0, buf.length);
				datagramSocket.receive(datagramPacket);
				byte[] byteData = datagramPacket.getData();
				IByteBuffer data = ByteBufferFactory.getNewByteBuffer(byteData);
				IByteBuffer sendData = ByteBufferFactory.getNewByteBuffer();
				int playerId = data.readInt();
				sendData.writeInt(playerId);
				sendData.writeInt(data.readInt());
				sendData.writeInt(data.readInt());
				sendData.writeInt(data.readInt());
				ports.put(playerId, datagramPacket.getPort());
				ips.put(playerId, datagramPacket.getAddress());
				other(playerId, sendData, datagramPacket);
			} catch (Exception e) {
				ErrorPrint.print(e);
			}
		}
		try {
			datagramSocket.close();
		} catch (Exception e) {
			ErrorPrint.print(e);
		}
		logger.info("游戏位置同步线程结束。");
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		ErrorPrint.print(e);
		System.err.println("	at sys.GameSyncManager(GameSyncManager.class:0)");
		start();
	}

	private void other(int playerId, IByteBuffer data, DatagramPacket datagramPacket) {
		PlayerInfo playerInfo = RedisProxy.getInstance().getPlayerInfo(playerId);
		if (playerInfo == null) {
			logger.error("玩家：{}，同步位置信息失败。获取玩家信息失败。", playerId);
			return;
		}
		if (playerInfo.getRoomId() == 0) {
			logger.error("玩家：{}，同步位置失败。未在游戏房间内。", playerId);
			return;
		}
		int roomId = playerInfo.getRoomId();

		GameRoomManager.getInstance().getLock().lock(roomId);
		try {
			GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
			if (room == null) {
				logger.error("玩家：{}，房间Id：{}，同步位置失败。游戏房间不存在。", playerId, roomId);
				return;
			}
			if (!room.isInRoom(playerId)) {
				logger.error("玩家：{}，房间Id：{}，同步位置失败。未在游戏房间内。", playerId, roomId);
				return;
			}

			int enemyId = room.getEnemyId(playerId);
			if (ports.get(enemyId) == null) {
				return;
			}
			int port = ports.get(enemyId);
			// 准备数据，把数据封装到数据包中。
			// 创建了一个数据包
			DatagramPacket packet = new DatagramPacket(data.getBytes(), data.length(), ips.get(enemyId), port);
			DatagramSocket datagramSocket = new DatagramSocket();
			// 调用udp的服务发送数据包
			datagramSocket.send(packet);
			// 关闭资源 ---实际上就是释放占用的端口号
			datagramSocket.close();
			logger.error("玩家：{}，房间Id：{}，ip:{} 端口:{} 同步位置。", playerId, roomId, packet.getAddress().getHostAddress(), packet.getPort());
		} catch (Exception e) {
			ErrorPrint.print(e);
		} finally {
			GameRoomManager.getInstance().getLock().unlock(roomId);
		}
	}
}