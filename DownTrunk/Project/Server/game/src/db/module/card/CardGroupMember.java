package db.module.card;

/**
 * mysql������ɳ�Ա����ģ��
 *
 */
public class CardGroupMember {

	/* ---------------- ���� --------------- */
	/** ����Id */
	private int groupId;
	/** ����Id */
	private String cardId;
	/** �������� */
	private int cardType;
	/** �������� */
	private int count;
	
	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	
	public String getCardId() {
		return cardId;
	}

	public void setCardId(String cardId) {
		this.cardId = cardId;
	}
	
	public int getCardType() {
		return cardType;
	}

	public void setCardType(int cardType) {
		this.cardType = cardType;
	}
	
	public int getCardCount() {
		return count;
	}
	
	public void setCardCount(int count) {
		this.count = count;
	}

}
