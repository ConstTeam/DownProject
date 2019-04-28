package module.quest;

public class Quest {

	/** 玩家id */
	private int playerId;
	/** 索引 */
	private int index;
	/** 任务Id */
	private int questId;
	/** 任务计数 */
	private int value;
	/** 任务状态 */
	private int state;
	/** 任务奖励*/
	private int gold;
	/** 是否有变化 */
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