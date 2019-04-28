package config.model.card;

import java.util.ArrayList;

import com.alibaba.fastjson.JSONObject;

public class CardDeckModel {

	/** 卡牌组id */
	public Integer DeckId;
	/** 卡牌组显示名 */
	public String DeckName;
	/** 符文组 */
	public ArrayList<Integer> Runes = new ArrayList<>();
	/** 卡牌id */
	public JSONObject cardIds = new JSONObject();
	/** 是否为初始卡组 */
	public boolean open = false;
}
