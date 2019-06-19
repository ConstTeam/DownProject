package module.fight;

public interface IBattleObject {
	
	public int getHp();
	
	public void setChange(boolean change);
	
	public boolean isChange();
	
	public int getPlayerId();
	
	public int getUid();
}
