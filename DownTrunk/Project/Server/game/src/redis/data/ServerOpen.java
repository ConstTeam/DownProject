package redis.data;

public class ServerOpen {

	/**
	 * 开启时间
	 */
	private String openTime;
	/**
	 * 提示信息
	 */
	private String message;
	/**
	 * 退出游戏提示信息
	 */
	private String exitMessage;
	
	public String getOpenTime() {
		return openTime;
	}
	public void setOpenTime(String openTime) {
		this.openTime = openTime;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getExitMessage() {
		return exitMessage;
	}
	public void setExitMessage(String exitMessage) {
		this.exitMessage = exitMessage;
	}
}
