package skill;

import java.util.ArrayList;

import module.card.CardBase;
import module.card.TroopCard;
import module.scene.GameRoom;

public class SkillTargetSelect {
	
	private static SkillTargetSelect instance;

	public static SkillTargetSelect getInstance() {
		if (instance == null) {
			instance = new SkillTargetSelect();
		}

		return instance;
	}
	
	public ArrayList<TroopCard> getOppAreaTroop(GameRoom room, CardBase selfCard) {
		ArrayList<TroopCard> result = new ArrayList<>();
		return result;
	}
	
	public ArrayList<CardBase> getOppAreaTroopAndArti(GameRoom room, CardBase selfCard) {
		ArrayList<CardBase> result = new ArrayList<>();
		return result;
	}
	
	public ArrayList<CardBase> getOppAreaArtiAndTrap(GameRoom room, CardBase selfCard) {
		ArrayList<CardBase> result = new ArrayList<>();
		return result;
	}
	
	public ArrayList<CardBase> getOppAreaTrap(GameRoom room, CardBase selfCard) {
		ArrayList<CardBase> result = new ArrayList<>();
		return result;
	}
}
