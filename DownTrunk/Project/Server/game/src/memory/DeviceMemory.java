package memory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 设备信息存储器
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
	 * 获取设备信息
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
