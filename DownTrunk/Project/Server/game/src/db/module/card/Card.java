package db.module.card;

/**
 * mysql������Ϣ����ģ��
 *
 */
public class Card{

	/* ---------------- ���� --------------- */
	/** ����Id */
	private String cardId;
	/** playerId */
	private int playerId;
	/** �������� */
	private int count;
	/** �������� */
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
