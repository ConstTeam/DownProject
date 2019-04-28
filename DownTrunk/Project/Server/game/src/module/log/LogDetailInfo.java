package module.log;

public class LogDetailInfo {
	
	public final static int ATTACK = 0;
	
	public final static int DRAW = 1;
	
	public final static int REDUCE = 2;
	
	public final static int DEATH = 0xFFFFFF;
	
	/** ¿‡–Õ 0π•ª˜ 1√˛≈∆ 2‘Ï≥……À∫¶ */
	private int type;
	/** ø®≈∆Id */
	private String cardId;

	private int value;
	
	public LogDetailInfo(int type, String cardId, int value) {
		this.type = type;
		this.cardId = cardId;
		this.value = value;
	}
	
	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getCardId() {
		return cardId;
	}

	public void setCardId(String cardId) {
		this.cardId = cardId;
	}

}
