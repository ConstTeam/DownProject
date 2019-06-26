package module.fight;

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
	/** ����Id */
	private int sceneId;
	/** ��ɫId */
	private int roleId;
	/** �Ƿ�Ϊ������ */
	private boolean robot = false;
	/** ���� */
	private boolean dead = false;
	
	public BattleRole(int playerId, String nickname, int hp, int uid) {
		this.setPlayerId(playerId);
		this.setUid(uid);
		this.setHp(hp);
		this.setNickname(nickname);
	}
	
	public void addHp(int defence) {
		defence += this.getRealHp();
		this.setHp(defence);
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

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getState() {
		return state;
	}

	public boolean isState() {
		return state != 0;
	}
	
	public void setState(int state) {
		this.state = state;
	}

	public void clearState() {
		this.state = 0;
	}
	
	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
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

	public int getSceneId() {
		return sceneId;
	}

	public void setSceneId(int sceneId) {
		this.sceneId = sceneId;
	}

	public int getRoleId() {
		return roleId;
	}

	public void setRoleId(int roleId) {
		this.roleId = roleId;
	}

	public boolean isDead() {
		return dead;
	}

	public void setDead(boolean dead) {
		this.dead = dead;
	}
}
