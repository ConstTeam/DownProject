package module.log;

import java.util.ArrayList;

public class LogInfo {

	public final static int AREA_LV_UP = 0;
	
	public final static int PLAY_CARD = 1;
	
	public final static int ATTACK = 2;
	
	public final static int TRIGGER = 3;
	
	
	/** ���� */
	private int index;
	/** ���id */
	private int playerId;
	/** ���� 0�������� 1���� 2�������*/
	private int type;
	/** �������� */
	private int cardType;
	/** ����Id */
	private String cardId;
	/** Ŀ�� */
	private ArrayList<LogDetailInfo> target;
	
	public LogInfo(int playerId, int type) {
		this.playerId = playerId;
		this.type = type;
		target = new ArrayList<>();
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
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

	public ArrayList<LogDetailInfo> getTarget() {
		return target;
	}

	public void setTarget(ArrayList<LogDetailInfo> target) {
		this.target = target;
	}
	
	public void addTarget(int type, String cardId, int value) {
		this.target.add(new LogDetailInfo(type, cardId, value));
	}
	
	public void addTarget(int type, String cardId) {
		this.target.add(new LogDetailInfo(type, cardId, 0));
	}

	public int getCardType() {
		return cardType;
	}

	public void setCardType(int cardType) {
		this.cardType = cardType;
	}
}
