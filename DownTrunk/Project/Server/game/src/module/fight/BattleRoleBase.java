package module.fight;

public abstract class BattleRoleBase implements IBattleObject {

	
	public boolean change = false;
	
	/** ���id */
	private int playerId;
	/** ���������� */
	private final int hpMaxLimit = 50;
	/** ��Դ */
	private int resource;
	/** �ɲ�����Դ */
	private int replResource;
	/** Ѫ�� */
	private int hp;
	/** �������� */
	private boolean areaLvUp;
	/** ���� */
	private boolean drawCard;
	/** ���ƴ��� */
	private int replaceDealCount;

	@Override
	public int getPlayerId() {
		return playerId;
	}
	
	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	public int getResource() {
		return resource;
	}

	public void setResource(int resource) {
		this.resource = resource;
	}

	public int getReplResource() {
		return replResource;
	}

	public void setReplResource(int replResource) {
		this.replResource = replResource;
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

	public boolean isAreaLvUp() {
		return areaLvUp;
	}

	public void setAreaLvUp(boolean areaLvUp) {
		this.areaLvUp = areaLvUp;
	}

	public int getReplaceDealCount() {
		return replaceDealCount;
	}

	public void addReplaceDealCount() {
		this.replaceDealCount ++;;
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

	@Override
	public boolean isForceShield() {
		return false;
	}

	public boolean isDrawCard() {
		return drawCard;
	}

	public void setDrawCard(boolean drawCard) {
		this.drawCard = drawCard;
	}
}
