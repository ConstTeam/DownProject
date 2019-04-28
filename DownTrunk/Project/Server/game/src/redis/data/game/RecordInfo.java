package redis.data.game;

public abstract class RecordInfo {

	/** 记录id */
	private String id;
	/** 房间Id*/
	private int roomId;
	/** 游戏类型 */
	private int gameType;
	/** 房间类型 */
	private int roomType;
	/** 记录时间 */
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
