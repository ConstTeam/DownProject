package db.module.card;

/**
 * mysql卡组组成成员数据模型
 *
 */
public class CardGroupMember {

	/* ---------------- 卡组 --------------- */
	/** 卡组Id */
	private int groupId;
	/** 卡牌Id */
	private String cardId;
	/** 卡牌类型 */
	private int cardType;
	/** 卡牌数量 */
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
