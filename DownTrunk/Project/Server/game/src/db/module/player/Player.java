package db.module.player;

/**
 * mysql角色数据模型
 *
 */
public class Player {

	/* ---------------- 账号、角色 --------------- */
	/** 角色Id */
	private int playerId;
	/** 账号 */
	private String accountId;
	/** 昵称 */
	private String nickname;
	/** 最后登录设备Id */
	private String deviceId;
	/** 头像 */
	private String icon;
	/** 性别 */
	private int sex = 1;
	/** 房间Id */
	private int roomId;
	/** 是否纳入防沉迷 */
	private boolean isAntiAddi;
	/** 防沉迷状态 */
	private int antiAddiState;
	/** 防沉迷通知时间 */
	private String antiAddiNoticeTime;
    /** 钻石 */
	private int diamond;
	/** 金币 */
	private int gold;
	/** 英雄列表 */
	private int roleList;
	/** 场景列表 */
	private int sceneList;
	
	
	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	public String getAccountId() {
		return accountId;
	}

	public void setAccountId(String accountId) {
		this.accountId = accountId;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public boolean isAntiAddi() {
		return isAntiAddi;
	}

	public void setAntiAddi(boolean isAntiAddi) {
		this.isAntiAddi = isAntiAddi;
	}

	public int getAntiAddiState() {
		return antiAddiState;
	}

	public void setAntiAddiState(int antiAddiState) {
		this.antiAddiState = antiAddiState;
	}

	public String getAntiAddiNoticeTime() {
		return antiAddiNoticeTime;
	}

	public void setAntiAddiNoticeTime(String antiAddiNoticeTime) {
		this.antiAddiNoticeTime = antiAddiNoticeTime;
	}

	public int getRoomId() {
		return roomId;
	}

	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}

    public int getDiamond() {
        return diamond;
    }

    public void setDiamond(int diamond) {
        this.diamond = diamond;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public int getRoleList() {
		return roleList;
	}

	public void setRoleList(int roleList) {
		this.roleList = roleList;
	}

	public int getSceneList() {
		return sceneList;
	}

	public void setSceneList(int sceneList) {
		this.sceneList = sceneList;
	}
}
