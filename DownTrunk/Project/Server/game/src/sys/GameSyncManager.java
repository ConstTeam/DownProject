package sys;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.ByteBufferFactory;
import net.IByteBuffer;
import util.ErrorPrint;

public class GameSyncManager {

	private static final Logger logger = LoggerFactory.getLogger(GameSyncManager.class);

	public static final int INTERVAL = 30;
	
	private IByteBuffer data = ByteBufferFactory.getNewByteBuffer();
	
	private HashMap<Integer, DatagramPacket> players = new HashMap<>();
	
	private String roomId = "";
	
	private int count = 0;
	
	private int frame = 0;
	
	ScheduledFuture<?> future;
	
	public static GameSyncManager getInstance() {
		return new GameSyncManager();
	}

	public void start(String roomId) {
		this.roomId = roomId;
		logger.info("����Id��{}����Ϸλ��ͬ����ʼ��", roomId);
		future = GameTimer.getScheduled().scheduleAtFixedRate(() -> run(), INTERVAL, INTERVAL, TimeUnit.MILLISECONDS);
	}

	public void run() {
		try {
			IByteBuffer tempDate = null;
			int count = 0;
			if (this.count != 0) {
				synchronized (roomId) {
					tempDate = this.data;
					count = this.count;
					this.data = ByteBufferFactory.getNewByteBuffer();
					this.count = 0;
				}
			}
			IByteBuffer sendData = ByteBufferFactory.getNewByteBuffer();
			sendData.writeInt(frame);
			sendData.writeByte(count);
			if (count != 0) {
				sendData.writeBytes(tempDate.getBytes());
			}
			other(sendData);
			frame++;
		} catch (Exception e) {
			ErrorPrint.print(e);
		}
	}
	
	public void stop() {
		if (future != null) {
			future.cancel(false);
		}
	}

	public void addPlayer(int playerId) {
		players.put(playerId, new DatagramPacket(new byte[0], 0));
	}
	
	public void addData(IByteBuffer data, int roomId, int playerId) {
		synchronized (this.roomId) {
//			logger.debug("����Id��{}�����Id��{}", roomId, playerId);
			this.data.writeInt(playerId);
			this.data.writeInt(data.readInt());
			this.data.writeInt(data.readInt());
			count++;
		}
	}

	private void other(IByteBuffer data) {
		try {
			// ������һ�����ݰ�
			for (int playerId : players.keySet()) {
				InetAddress ip = UDPMsgManager.getIp(playerId);
				if (ip == null) {
					continue;
				}
				DatagramPacket packet = players.get(playerId);
				int port = UDPMsgManager.getPort(playerId);
				packet.setData(data.getBytes());
				packet.setAddress(ip);
				packet.setPort(port);
				// ����udp�ķ��������ݰ�
				UDPMsgManager.getDatagramSocket().send(packet);
//				logger.debug("����Id��{}��ip:{} �˿�:{} ͬ��λ�á�", roomId, packet.getAddress().getHostAddress(), packet.getPort());
			}
		} catch (Exception e) {
			ErrorPrint.print(e);
		}
	}
}