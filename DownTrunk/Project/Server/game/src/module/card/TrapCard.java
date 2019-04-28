package module.card;

import java.util.HashMap;

import config.model.card.CardModel;

public class TrapCard extends CardBase implements ITrapStatus {

	private final int type = CardModel.TRAP;
	
	public TrapCard(CardModel cardModel) {
		this.init(cardModel);
	}
	
	public int getType() {
		return type;
	}

	public boolean isExcessDamage() {
		return getStatus(EXCESS_DAMAGE);
	}

	public boolean isAfterAttack() {
		return getStatus(AFTER_ATTACK);
	}

	@Override
	public int getMainRowIndex() {
		if (this.getArea() == null) {
			return -1;
		}
		return this.getArea().getMainRowIndex(this);
	}

	@Override
	public int getAreaIndex() {
		if (this.getArea() == null) {
			return -1;
		}
		return this.getArea().getIndex();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void copy(CardBase card) {
		Object clone = card.getStatus().clone();
		this.setStatus((HashMap<String, Integer>) clone);
	}
}
