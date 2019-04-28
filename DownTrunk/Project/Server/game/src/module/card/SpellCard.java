package module.card;

import java.util.HashMap;

import config.model.card.CardModel;

public class SpellCard extends CardBase {

	private final int type = CardModel.SPELL;

	public SpellCard(CardModel cardModel) {
		this.init(cardModel);
		this.setGenius(cardModel.Genius);
	}
	
	public int getType() {
		return type;
	}

	@Override
	public int getMainRowIndex() {
		return -1;
	}

	@Override
	public int getAreaIndex() {
		return -1;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void copy(CardBase card) {
		Object clone = card.getStatus().clone();
		this.setStatus((HashMap<String, Integer>) clone);
	}
}
