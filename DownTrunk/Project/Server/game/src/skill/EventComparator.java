package skill;

import java.util.Comparator;

import module.card.CardBase;

public class EventComparator implements Comparator<TriggerEvent> {

	@Override
	public int compare(TriggerEvent t1, TriggerEvent t2) {
		CardBase card1 = t1.getTriggerCard();
		CardBase card2 = t2.getTriggerCard();
		if (card1.getAreaIndex() != card2.getAreaIndex()) {
			return card1.getAreaIndex() - card2.getAreaIndex();
		}
		return card1.getMainRowIndex() - card2.getMainRowIndex();
	}
}
