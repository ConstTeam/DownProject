package config.model.card;

import java.util.ArrayList;

import com.alibaba.fastjson.JSONObject;

public class CardDeckModel {

	/** ������id */
	public Integer DeckId;
	/** ��������ʾ�� */
	public String DeckName;
	/** ������ */
	public ArrayList<Integer> Runes = new ArrayList<>();
	/** ����id */
	public JSONObject cardIds = new JSONObject();
	/** �Ƿ�Ϊ��ʼ���� */
	public boolean open = false;
}
