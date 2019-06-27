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
import util.ErrorPrint;

public class UDPMsgManager implements Runnable, UncaughtExceptionHandler {

	private static final Logger logger = LoggerFactory.getLogger(UDPMsgManager.class);

	private static HashMap<Integer, InetAddress> ips = new HashMap<>();
	
	private static HashMap<Integer, Integer> ports = new HashMap<>();
	
	private HashMap<Integer, Integer> roomIdMap = new HashMap<>();

	private static UDPMsgManager instance;
	
	private static DatagramSocket datagramSocket;
	
	public static UDPMsgManager getInstance() {
		if (instance == null) {
			instance = new UDPMsgManager();
		}
		return instance;
	}

	public static void start() {
		UDPMsgManager manager = UDPMsgManager.getInstance();
		Thread thread = new Thread(manager, "GameSyncManager");
		thread.setUncaughtExceptionHandler(manager);
		thread.start();
	}

	@Override
	public void run() {

		byte[] buf = new byte[1024];

		try {
			datagramSocket = new DatagramSocket(8801);
		} catch (Exception e) {
			ErrorPrint.print(e);
			return;
		}
		logger.info("服务器开启，UDP数据接收线程开始。");

		while (true) {
			if (!ServerStaticInfo.opened) {
				logger.info("服务器关闭，UDP数据接收线程结束。");
				break;
			}
			try {
				// 定义接收数据的数据包
				DatagramPacket datagramPacket = new DatagramPacket(buf, 0, buf.length);
				datagramSocket.receive(datagramPacket);
				byte[] byteData = datagramPacket.getData();
				IByteBuffer data = ByteBufferFactory.getNewByteBuffer(byteData);
				int playerId = data.readInt();
				
				Integer roomId = this.roomIdMap.get(playerId);
				if (roomId == null || roomId == 0) {
					continue;
				}
				ports.put(playerId, datagramPacket.getPort());
				ips.put(playerId, datagramPacket.getAddress());
				GameRoom room = GameRoomManager.getInstance().getRoom(roomId);
				if (room == null) {
					continue;
				}
				room.getSyncManager().addData(data, playerId);
			} catch (Exception e) {
				ErrorPrint.print(e);
			}
		}
		try {
			datagramSocket.close();
		} catch (Exception e) {
			ErrorPrint.print(e);
		}
		logger.info("UDP数据接收线程结束。");
	}

	@Override
	public void uncaughtException(Thread t, Throwable e) {
		ErrorPrint.print(e);
		System.err.println("	at sys.GameSyncManager(GameSyncManager.class:0)");
	}
	
	public static InetAddress getIp(int playerId) {
		return ips.get(playerId);
	}
	
	public static int getPort(int playerId) {
		return ports.get(playerId);
	}

	public static DatagramSocket getDatagramSocket() {
		return datagramSocket;
	}

	public static void setDatagramSocket(DatagramSocket datagramSocket) {
		UDPMsgManager.datagramSocket = datagramSocket;
	}

	public void setPlayerRoom(int playerId, int roomId) {
		roomIdMap.put(playerId, roomId);
	}
}