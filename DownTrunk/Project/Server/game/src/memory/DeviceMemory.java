package memory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * �豸��Ϣ�洢��
 *
 */
public class DeviceMemory {

	private static DeviceMemory instance;
	
	private ConcurrentHashMap<Integer, String> devices;
	
	private DeviceMemory() {
		devices = new ConcurrentHashMap<>();
	}
	
	public static DeviceMemory getInstance() {
		if (instance == null) {
			instance = new DeviceMemory();
		}
		
		return instance;
	}

	public void create(int playerId, String device) {
		devices.putIfAbsent(playerId, device);
	}
	
	/**
	 * ��ȡ�豸��Ϣ
	 */
	public String getDevice(int playerId) {
		String putIfAbsent = devices.putIfAbsent(playerId, "");
		if (null == putIfAbsent) {
			return "";
		} else {
			return putIfAbsent;
		}
	}
}
