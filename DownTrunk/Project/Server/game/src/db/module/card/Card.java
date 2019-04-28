package db.module.card;

/**
 * mysql卡牌信息数据模型
 *
 */
public class Card{

	/* ---------------- 卡牌 --------------- */
	/** 卡牌Id */
	private String cardId;
	/** playerId */
	private int playerId;
	/** 卡牌数量 */
	private int count;
	/** 卡牌类型 */
	private byte cardType;
		
	public String getCardID() {
		return cardId;
	}
	
	public void setCardID(String cardId) {
		this.cardId = cardId;
	}
	
	public byte getCardType() {
		return cardType;
	}
	
	public void setCardType(byte cardType) {
		this.cardType = cardType;
	}
	
	public int getPlayerID() {
		return playerId;
	}

	public void setPlayerID(int playerId) {
		this.playerId = playerId;
	}
	
	public int getCount() {
		return count;
	}
	
	public void setCount(int count) {
		this.count = count;
	}
}
