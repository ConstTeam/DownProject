package redis.data;

public class ServerOpen {

	/**
	 * ����ʱ��
	 */
	private String openTime;
	/**
	 * ��ʾ��Ϣ
	 */
	private String message;
	/**
	 * �˳���Ϸ��ʾ��Ϣ
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
