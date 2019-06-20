package db.module.player;

/**
 * mysql��ɫ����ģ��
 *
 */
public class Player {

	/* ---------------- �˺š���ɫ --------------- */
	/** ��ɫId */
	private int playerId;
	/** �˺� */
	private String accountId;
	/** �ǳ� */
	private String nickname;
	/** ����¼�豸Id */
	private String deviceId;
	/** ͷ�� */
	private String icon;
	/** �Ա� */
	private int sex = 1;
	/** ����Id */
	private int roomId;
	/** �Ƿ���������� */
	private boolean isAntiAddi;
	/** ������״̬ */
	private int antiAddiState;
	/** ������֪ͨʱ�� */
	private String antiAddiNoticeTime;
    /** ��ʯ */
	private int diamond;
	/** ��� */
	private int gold;
	/** Ӣ���б� */
	private int roleList;
	/** �����б� */
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
