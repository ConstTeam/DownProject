package skill;

import java.util.Comparator;

import module.card.CardBase;

public class CardComparator implements Comparator<CardBase> {

	@Override
	public int compare(CardBase card1, CardBase card2) {
		if (card1.getAreaIndex() != card2.getAreaIndex()) {
			return card1.getAreaIndex() - card2.getAreaIndex();
		}
		return card1.getMainRowIndex() - card2.getMainRowIndex();
	}
}
