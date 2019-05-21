package skill;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.model.skill.SkillModel;
import module.area.Area;
import module.card.CardBase;
import module.card.ITrapStatus;
import module.card.TrapCard;
import module.fight.BattleRole;
import module.scene.GameRoom;

public class TrapTriggerManager implements ISkillConst, ITrapStatus {
	
@SuppressWarnings("unused")
private static final Logger logger = LoggerFactory.getLogger(TrapTriggerManager.class);
	
	private static TrapTriggerManager instance;

	public static TrapTriggerManager getInstance() {
		if (instance == null) {
			instance = new TrapTriggerManager();
		}

		return instance;
	}
	
	public void effect(GameRoom room, int fighterId, CardBase selfCard, Area area, BattleRole self, SkillModel model, int sendType) {
		String trigger = model.Trigger;
		String type = model.Type;

		switch (type) {
		case EXCESS_DAMAGE:
			selfCard.setStatus(TrapCard.EXCESS_DAMAGE, true);
			break;
			
		case AREA_SPELL_BLOCK:
			selfCard.setStatus(TrapCard.AREA_SPELL_BLOCK, true);
			break;
			
		case INTERRUPT:
			selfCard.setStatus(INTERRUPT, true);
			return;
		}
		
		switch (trigger) {
		case AFTER_ATTACK:
		case AFTER_ATTACK3:
			selfCard.setStatus(TrapCard.AFTER_ATTACK, true);
			return;
		}
	}
	
	public boolean spellBlock(GameRoom room, CardBase card, CardBase triggerCard) {
//		BattleRole self = room.getBattleRole(card.getPlayerId());
//		Area area = self.getArea(card.getAreaIndex());
//		for (TrapCard trap : area.getTrap()) {
//			if(spellBlock(room, trap, triggerCard)) {
//				return true;
//			}
//		}
		return false;
	}
	
	public boolean spellBlock(GameRoom room, Area area, CardBase triggerCard) {
		for (TrapCard trap : area.getTrap()) {
			if(spellBlock(room, trap, triggerCard)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean spellBlock(GameRoom room, TrapCard trap, CardBase triggerCard) {
		if (trap == null) {
			return false;
		}
		return true;
	}
	
	public boolean afterAttack(GameRoom room, TrapCard trap, CardBase triggerCard) {
		if (trap == null) {
			return false;
		}
		return true;
	}

	public boolean interrupt(GameRoom room, TrapCard trap, CardBase triggerCard) {
		if (trap == null) {
			return false;
		}
		return false;
	}
	
	public void triggerTrap(GameRoom room, TrapCard trap, CardBase triggerCard) {
	}
}
