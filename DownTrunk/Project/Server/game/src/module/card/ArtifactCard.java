package module.card;

import java.util.HashMap;

import config.model.card.CardModel;

public class ArtifactCard extends CardBase implements IArtifactStatus {

	private final int type = CardModel.ARTIFACT;
	/** Ë¯Ãß×´Ì¬ */
	private boolean sleep;

	public ArtifactCard(CardModel cardModel) {
		this.init(cardModel);
		this.setGenius(cardModel.Genius);
	}

	public int getType() {
		return type;
	}

	public boolean isSleep() {
		return sleep;
	}

	public void setSleep(boolean sleep) {
		this.sleep = sleep;
	}

	public boolean isAttackLimitOnce() {
		return getStatus(ATTACK_LIMIT_ONCE);
	}
	
	public void endTrun() {
		Integer trunCount = getStatus().get(TRUN_COUNT);
		if (trunCount != null) {
			setStatus(TRUN_COUNT, true);
		}
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
