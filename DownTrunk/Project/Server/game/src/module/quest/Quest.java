package module.quest;

public class Quest {

	/** ���id */
	private int playerId;
	/** ���� */
	private int index;
	/** ����Id */
	private int questId;
	/** ������� */
	private int value;
	/** ����״̬ */
	private int state;
	/** ������*/
	private int gold;
	/** �Ƿ��б仯 */
	private boolean change;

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getQuestId() {
		return questId;
	}

	public void setQuestId(int questId) {
		this.questId = questId;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
	public void addValue(int value) {
		this.value += value;
		this.change = true;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getGold() {
		return gold;
	}

	public void setGold(int gold) {
		this.gold = gold;
	}

	public boolean isChange() {
		return change;
	}

	public void setChange(boolean change) {
		this.change = change;
	}

}