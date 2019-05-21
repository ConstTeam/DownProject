package skill;

import java.util.ArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import module.area.Area;
import module.card.CardBase;
import module.card.TroopCard;
import module.fight.BattleRole;
import module.scene.GameRoom;

public class SkillTargetSelect {
	
@SuppressWarnings("unused")
private static final Logger logger = LoggerFactory.getLogger(SkillManager.class);
	
	private static SkillTargetSelect instance;

	public static SkillTargetSelect getInstance() {
		if (instance == null) {
			instance = new SkillTargetSelect();
		}

		return instance;
	}
	
	public ArrayList<TroopCard> getOppAreaTroop(GameRoom room, CardBase selfCard) {
		BattleRole fighter = room.getBattleRole(room.getEnemyId(selfCard.getPlayerId()));
		int oppoAreaIndex = SkillManager.getInstance().getOppoAreaIndex(selfCard.getAreaIndex());
		Area area = fighter.getArea(oppoAreaIndex);
		return area.getTroops();
	}
	
	public ArrayList<CardBase> getOppAreaTroopAndArti(GameRoom room, CardBase selfCard) {
		ArrayList<CardBase> result = new ArrayList<>();
		BattleRole fighter = room.getBattleRole(room.getEnemyId(selfCard.getPlayerId()));
		int oppoAreaIndex = SkillManager.getInstance().getOppoAreaIndex(selfCard.getAreaIndex());
		Area area = fighter.getArea(oppoAreaIndex);
		result.addAll(area.getArtifact());
		result.addAll(area.getTroops());
		return result;
	}
	
	public ArrayList<CardBase> getOppAreaArtiAndTrap(GameRoom room, CardBase selfCard) {
		ArrayList<CardBase> result = new ArrayList<>();
		BattleRole fighter = room.getBattleRole(room.getEnemyId(selfCard.getPlayerId()));
		int oppoAreaIndex = SkillManager.getInstance().getOppoAreaIndex(selfCard.getAreaIndex());
		Area area = fighter.getArea(oppoAreaIndex);
		result.addAll(area.getArtiTraps());
		return result;
	}
	
	public ArrayList<CardBase> getOppAreaTrap(GameRoom room, CardBase selfCard) {
		ArrayList<CardBase> result = new ArrayList<>();
		BattleRole fighter = room.getBattleRole(room.getEnemyId(selfCard.getPlayerId()));
		int oppoAreaIndex = SkillManager.getInstance().getOppoAreaIndex(selfCard.getAreaIndex());
		Area area = fighter.getArea(oppoAreaIndex);
		result.addAll(area.getTrap());
		return result;
	}
}
