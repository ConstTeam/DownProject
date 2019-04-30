package module.fight;

import quest.QuestManager;

public class BattleRole extends BattleRoleBase implements IBattleRoleStatus {

	/** ����Ψһid */
	private int uid;
	/** ����ǳ� */
	private String nickname;
	/** ͷ�� */
	private String icon;
	/** �������� */
	private int rank;
	/** ��ǰ״̬ */
	private int state;
	/** ���ڷ���Id */
	private int roomId;
	/** �Ƿ�Ϊ������ */
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
