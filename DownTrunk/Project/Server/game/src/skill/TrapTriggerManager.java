package skill;

import java.util.ArrayList;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.ConfigData;
import config.model.skill.SkillModel;
import message.game.fight.FightMsgSend;
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
		default: 
			SkillArg arg = new SkillArg(room, fighterId, selfCard, area, self, model, sendType);
			if (!SkillManager.getInstance().triggerRegister(arg)) {
				return;
			}
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
		if (room.checkPlayer(trap.getPlayerId())) {
			return false;
		}
		if (!trap.getStatus(AREA_SPELL_BLOCK)) {
			return false;
		}
		BattleRole self = room.getBattleRole(trap.getPlayerId());
		Area area = self.getArea(trap.getAreaIndex());
		HashMap<Integer, SkillModel> skill = ConfigData.skillModels.get(trap.getRealId());
		for (SkillModel model : skill.values()) {
			if (COPY.equals(model.Type)) {
				CardBase card = room.createCard(self.getPlayerId(), triggerCard.getRealId());
				if (card != null) {
					ArrayList<CardBase> cards = new ArrayList<>();
					cards.add(card);
					room.drawCardAndSync(self, cards);
				}
			} else {
				SkillArg arg = new SkillArg(room, self.getPlayerId(), trap, area, self, model, 1);
				SkillManager.getInstance().triggerRegister(arg);
			}
		}
		trap.setStatus(AREA_SPELL_BLOCK, false);
		triggerTrap(room, trap, triggerCard);
		return true;
	}
	
	public boolean afterAttack(GameRoom room, TrapCard trap, CardBase triggerCard) {
		if (trap == null) {
			return false;
		}
		if (room.checkPlayer(trap.getPlayerId())) {
			return false;
		}
		boolean result = false;
		BattleRole self = room.getBattleRole(trap.getPlayerId());
		Area area = self.getArea(trap.getAreaIndex());
		HashMap<Integer, SkillModel> skill = ConfigData.skillModels.get(trap.getRealId());
		for (SkillModel model : skill.values()) {
			SkillArg arg = new SkillArg(room, trap.getPlayerId(), trap, area, self, model, 1);
			if (AFTER_ATTACK.equals(model.Trigger)) {
				if (SkillManager.TRIGGER.equals(model.Target)) {
					arg.setSelfCard(triggerCard);
				}
				result = SkillManager.getInstance().triggerEffect(arg);
			} else if (AFTER_ATTACK3.equals(model.Trigger)) {
				if (self.getStatus(TriggerManager.HERO_BE_ATTACK_COUNT) && self.getStatus().get(TriggerManager.HERO_BE_ATTACK_COUNT) == 3) {
					result = SkillManager.getInstance().trigger(arg);
				}
			}
		}
		if (result) {
			triggerTrap(room, trap, triggerCard);
		}
		return true;
	}

	public boolean interrupt(GameRoom room, TrapCard trap, CardBase triggerCard) {
		if (trap == null) {
			return false;
		}
		if (room.checkPlayer(trap.getPlayerId())) {
			return false;
		}
		HashMap<Integer, SkillModel> skill = ConfigData.skillModels.get(trap.getRealId());
		if (skill == null || skill.size() == 0) {
			return false;
		}
		int playerId = trap.getPlayerId();
		int enemyId = room.getEnemyId(playerId);
		for (SkillModel model : skill.values()) {
			if (!SkillManager.INTERRUPT.equals(model.Type)) {
				continue;
			}
			SkillArg arg = new SkillArg(room, triggerCard.getPlayerId(), triggerCard, triggerCard.getArea(), room.getBattleRole(triggerCard.getPlayerId()), model, 0);
			if (!SkillManager.getInstance().trigger(arg)) {
				return false;
			}
			room.syncStart(playerId);
			trap.setStatus(SkillManager.INTERRUPT, false);
			FightMsgSend.revealCardSync(room.getSession(playerId), triggerCard, room.getBattleRole(enemyId));
			room.interruptCardSync(playerId, triggerCard);
			triggerTrap(room, trap, triggerCard);
			room.syncEnd(playerId);
			return true;
		}
		return false;
	}
	
	public void triggerTrap(GameRoom room, TrapCard trap, CardBase triggerCard) {
		room.trapTriggerSync(trap);
		room.cardKill(trap);
		room.getTriggerManager().triggerEffect(room, TriggerManager.TRIGGER_TRAP, trap.getPlayerId(), trap, 1);
	}
}
