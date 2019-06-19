package module.fight;

public abstract class BattleRoleBase implements IBattleObject {

	
	public boolean change = false;
	
	/** 玩家id */
	private int playerId;
	/** 防御力上限 */
	private final int hpMaxLimit = 50;
	/** 血量 */
	private int hp;

	@Override
	public int getPlayerId() {
		return playerId;
	}
	
	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	public int getHpMaxLimit() {
		return hpMaxLimit;
	}

	@Override
	public int getHp() {
		return hp < 0 ? 0 : hp;
	}
	
	public int getRealHp() {
		return hp;
	}
	
	public void setHp(int hp) {
		hp = hp > this.getHpMaxLimit() ? this.getHpMaxLimit() : hp;
		this.hp = hp;
	}

	public void addHp(int hp) {
		hp += this.hp; 
		setHp(hp);
	}

	@Override
	public boolean isChange() {
		return change;
	}

	@Override
	public void setChange(boolean change) {
		this.change = change;
	}
}
