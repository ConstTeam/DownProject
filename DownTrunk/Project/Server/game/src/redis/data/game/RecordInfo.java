package redis.data.game;

public abstract class RecordInfo {

	/** ��¼id */
	private String id;
	/** ����Id*/
	private int roomId;
	/** ��Ϸ���� */
	private int gameType;
	/** �������� */
	private int roomType;
	/** ��¼ʱ�� */
	private String createTime;
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getGameType() {
		return gameType;
	}

	public void setGameType(int gameType) {
		this.gameType = gameType;
	}

	public String getCreateTime() {
		return createTime;
	}

	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}
	
	public abstract String toSimpleString();

	public int getRoomType() {
		return roomType;
	}

	public void setRoomType(int roomType) {
		this.roomType = roomType;
	}

	public int getRoomId() {
		return roomId;
	}

	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}
}
