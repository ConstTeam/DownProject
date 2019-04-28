package skill;

import config.model.skill.SkillModel;
import module.card.CardBase;

public class TriggerEvent {
	
	private CardBase triggerCard;
	
	private CardBase card;

	private SkillModel model;
	
	public TriggerEvent(CardBase triggerCard, CardBase card, SkillModel model) {
		this.card = card;
		this.triggerCard = triggerCard;
		this.model = model;
	}

	public CardBase getCard() {
		return card;
	}

	public void setCard(CardBase card) {
		this.card = card;
	}

	public SkillModel getModel() {
		return model;
	}

	public void setModel(SkillModel model) {
		this.model = model;
	}

	public CardBase getTriggerCard() {
		return triggerCard;
	}

	public void setTriggerCard(CardBase triggerCard) {
		this.triggerCard = triggerCard;
	}

}
