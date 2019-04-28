package module.card;

import java.util.ArrayList;

public class FindCard {
	
	public static final int FIND = 1;
	
	public static final int CONSTRUCT_TRAP = 2;
	
	public static final int SUMMON = 3;
	
	public static final int REVEAL = 4;
	
	public static final int SUMMON_COPY = 5;
	
	
	private ArrayList<CardBase> cards = new ArrayList<>();
	
	private String cardId;
	
	private int type;
	
	private int cardUid;
	
	private int areaIndex = -1;
	
	private boolean send = false;
	
	private boolean first = false;
	
	private boolean isOutput = false;

	public ArrayList<CardBase> getCards() {
		return cards;
	}

	public void addCards(CardBase card) {
		this.cards.add(card);
	}

	public boolean isFirst() {
		return first;
	}

	public void setFirst(boolean first) {
		this.first = first;
	}

	public boolean isSend() {
		return send;
	}

	public void setSend(boolean send) {
		this.send = send;
	}

	public String getCardId() {
		return cardId;
	}

	public void setCardId(String cardId) {
		this.cardId = cardId;
	}

	public int getCardUid() {
		return cardUid;
	}

	public void setCardUid(int cardUid) {
		this.cardUid = cardUid;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public boolean isOutput() {
		return isOutput;
	}

	public void setOutput(boolean isOutput) {
		this.isOutput = isOutput;
	}

	public int getAreaIndex() {
		return areaIndex;
	}

	public void setAreaIndex(int areaIndex) {
		this.areaIndex = areaIndex;
	}
}
