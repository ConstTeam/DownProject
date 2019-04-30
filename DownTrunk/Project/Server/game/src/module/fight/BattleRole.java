package module.fight;

import quest.QuestManager;

public class BattleRole extends BattleRoleBase implements IBattleRoleStatus {

	/** 本局唯一id */
	private int uid;
	/** 玩家昵称 */
	private String nickname;
	/** 头像 */
	private String icon;
	/** 天梯排名 */
	private int rank;
	/** 当前状态 */
	private int state;
	/** 所在房间Id */
	private int roomId;
	/** 是否为机器人 */
	private boolean robot = false;
	
	private QuestManager questManager;

	public BattleRole(int playerId, String nickname, int hp, int uid) {
		this.setPlayerId(playerId);
		this.setUid(uid);
		this.setHp(hp);
		this.setNickname(nickname);
		setQuestManager(QuestManager.init(playerId));
	}
	
	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
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

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getRoomId() {
		return roomId;
	}

	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}

	public boolean isRobot() {
		return robot;
	}

	public void setRobot(boolean robot) {
		this.robot = robot;
	}

	public QuestManager getQuestManager() {
		return questManager;
	}

	public void setQuestManager(QuestManager questManager) {
		this.questManager = questManager;
	}
}
