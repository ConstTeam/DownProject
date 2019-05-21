package skill;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.ConfigData;
import config.model.card.CardModel;
import config.model.skill.SkillModel;
import message.game.fight.FightMsgSend;
import module.area.Area;
import module.card.ArtifactCard;
import module.card.CardBase;
import module.card.FindCard;
import module.card.IArtifactStatus;
import module.card.ICardConst;
import module.card.ITroopStatus;
import module.card.SpellCard;
import module.card.TrapCard;
import module.card.TroopCard;
import module.fight.BattleRole;
import module.fight.IBattleObject;
import module.log.LogDetailInfo;
import module.scene.GameRoom;
import quest.QuestManager;
import util.ErrorPrint;
import util.Tools;

public class SkillManager implements ISkillConst, ICardConst, ITroopStatus, IArtifactStatus {

	private static final Logger logger = LoggerFactory.getLogger(SkillManager.class);

	private static SkillManager instance;

	public static SkillManager getInstance() {
		if (instance == null) {
			instance = new SkillManager();
		}

		return instance;
	}

	/**
	 * 技能触发判断
	 * 
	 * @param model
	 * @param card
	 * @param room
	 * @param area
	 * @return
	 */
	public boolean trigger(SkillArg arg) {
		SkillModel model = arg.getModel();
		CardBase card = arg.getTriggerOwner();
		String action = model.Genius;
		String trigger = model.Trigger;

		/*
		 * 判断触发条件
		 */
		if (!Tools.isEmptyString(trigger)) {

			switch (trigger) {
			case TriggerManager.DEATHCRY:
				if (card.getType() != CardModel.TROOP) {
					return false;
				}
				TroopCard troop = (TroopCard) card;
				if (!troop.isDead()) {
					return false;
				}
				break;
			}
		}
		if (action.indexOf("|") != -1) {
			boolean result = true;
			String[] split = action.split("\\|");
			for (String str : split) {
				if (!trigger(str, arg)) {
					result = false;
				}
			}
			return result;
		} else {
			return trigger(action, arg);
		}
	}

	private boolean trigger(String action, SkillArg arg) {
		/*
		 * 判断技能前置条件
		 */
		if (Tools.isEmptyString(action)) {
			return true;
		}
		SkillModel model = arg.getModel();
		CardBase card = arg.getTriggerOwner();
		GameRoom room = arg.getRoom();
		Area area = arg.getArea();
		int fighterId = arg.getPlayerId();
		CardBase attCard = arg.getAttCard();
		
		switch (action) {
		case PATHFINDER:
			if (area == null) {
				return false;
			}
			return card.getPathfinder() == area.getIndex();
		case LEADER:
			if (card.getType() != CardModel.TROOP) {
				return false;
			}
			TroopCard troop = (TroopCard) card;
			return troop.isLeader();
		case MY_TURN:
			room.getTriggerManager().addTriggerEvent(TriggerManager.MY_TURN, model, card);
			if (room.checkPlayer(card.getPlayerId())) {
				return true;
			}
			break;
		case THIS_TURN:
			room.getTriggerManager().addTriggerEvent(TriggerManager.THIS_TURN, model, card);
			return true;
		case THIS_ATTACK:
			room.getTriggerManager().addTriggerEvent(TriggerManager.THIS_ATTACK, model, card);
			return true;
		case IS_MY_TURN:
			return room.checkPlayer(card.getPlayerId());
		case IS_ENEMY_TURN:
			return !room.checkPlayer(card.getPlayerId());
		case AIR_TEMPLE:
		case EARTH_TEMPLE:
		case FIRE_TEMPLE:
		case WATER_TEMPLE:
			if (area == null) {
				return false;
			}
			if (area.getRune() == getRune(action) && area.getLevel() >= Area.MAX_LEVEL) {
				room.getTriggerManager().delTriggerEvent(TriggerManager.TEMPLE, card.getUid());
				return true;
			} else {
				if (Tools.isEmptyString(model.Trigger) || model.Trigger.indexOf(TriggerManager.TROOP_CHANGE) != -1) {
					room.getTriggerManager().addTriggerEvent(TriggerManager.TEMPLE, model, card);
				}
			}
			break;
		case NOT_EARTH_TEMPLE:
			if (area.getRune() != EARTH) {
				return true;
			}
			if (area.getLevel() < Area.MAX_LEVEL) {
				return true;
			}
			return false;
		case SUMMON_TROOP:
			if (area == null) {
				return false;
			}
			return area.haveTroopSeat();
		case SUMMON_TRAP:
			if (area == null) {
				return false;
			}
			return area.haveArtiOrTrapSeat();
		case OPP_AREA_NO_TROOP:
			room.getTriggerManager().addTriggerEvent(TriggerManager.TROOP_CHANGE, model, card);

			int oppoAreaIndex = getOppoAreaIndex(card.getAreaIndex());
			int playerId = card.getPlayerId();
			int enemyId = room.getEnemyId(playerId);
			BattleRole enemy = room.getBattleRole(enemyId);
			if (enemy.getArea(oppoAreaIndex).troopIsEmpty()) {
				return true;
			} else {
				this.removeEffect(TriggerManager.TROOP_CHANGE, room, card, model);
			}
			break;
			
		case AREA_NO_TROOP:
			room.getTriggerManager().addTriggerEvent(TriggerManager.TROOP_CHANGE, model, card);
			
			BattleRole role = room.getBattleRole(card.getPlayerId());
			if (role.getArea(card.getAreaIndex()).troopIsEmpty()) {
				return true;
			} else {
				this.removeEffect(TriggerManager.TROOP_CHANGE, room, card, model);
			}
			break;

		case OPP_AREA_STUN_TROOP:
			oppoAreaIndex = getOppoAreaIndex(card.getAreaIndex());
			playerId = card.getPlayerId();
			enemyId = room.getEnemyId(playerId);
			enemy = room.getBattleRole(enemyId);
			for (TroopCard temp : enemy.getArea(oppoAreaIndex).getTroops()) {
				if (temp.isStun()) {
					return true;
				}
			}
			return false;

		case OPP_AREA_FULL:
			room.getTriggerManager().addTriggerEvent(TriggerManager.TROOP_CHANGE, model, card);

			oppoAreaIndex = getOppoAreaIndex(card.getAreaIndex());
			playerId = card.getPlayerId();
			enemyId = room.getEnemyId(playerId);
			enemy = room.getBattleRole(enemyId);
			if (enemy.getArea(oppoAreaIndex).troopIsFull()) {
				return true;
			}
			break;

		case AREA_FULL:
			if (card.getArea() != null && card.getArea().troopIsFull()) {
				return true;
			}
			break;

		case AREA_TRAP:
			room.getTriggerManager().addTriggerEvent(TriggerManager.AREA_TRAP, model, card);
			room.getTriggerManager().addTriggerEvent(TriggerManager.TROOP_CHANGE, model, card);

			if (area == null) {
				return false;
			}
			if (area.getTrap() != null) {
				return true;
			} else {
				this.removeEffect(TriggerManager.TROOP_CHANGE, room, card, model);
			}
			break;
		case FIRST:
			if (card.getType() == CardModel.ARTIFACT) {
				if (room.getTriggerManager().addTriggerEvent(TriggerManager.FIRST, model, card)) {
					return true;
				}
			}
			playerId = card.getPlayerId();
			role = room.getBattleRole(playerId);
			return !role.isNotFirst();
		case SECOND:
			room.getTriggerManager().addTriggerEvent(TriggerManager.SECOND, model, card);
			playerId = card.getPlayerId();
			role = room.getBattleRole(playerId);
			if (role.isSecondAfter()) {
				return true;
			}
			break;
		case PLAY_CARD_3:
			playerId = card.getPlayerId();
			role = room.getBattleRole(playerId);
			Integer value = role.getStatus().get(BattleRole.PLAY_CARD_COUNT);
			if (value != null && value >= 3) {
				return true;
			}
			break;
		case PLAY_CARD_4:
			playerId = card.getPlayerId();
			role = room.getBattleRole(playerId);
			value = role.getStatus().get(BattleRole.PLAY_CARD_COUNT);
			if (value != null && value >= 4) {
				return true;
			}
			break;
		case NOT_FIRST:
			playerId = card.getPlayerId();
			role = room.getBattleRole(playerId);
			return role.isNotFirst();
		case MINE_HERO_LIFE_LE:
			role = room.getBattleRole(card.getPlayerId());
			if (role.getStatus(MINE_HERO_LIFE_LE)) {
				if (model.Cancel == 1) {
					room.getTriggerManager().addTriggerEvent(TriggerManager.MINE_HERO_LIFE, model, card);
				}
				int hp = role.getStatus().get(MINE_HERO_LIFE_LE);
				if (hp > 0 && role.getHp() <= hp) {
					return true;
				} else {
					this.removeEffect(TriggerManager.MINE_HERO_LIFE, room, card, model);
				}
			}
			break;
		case MINE_HERO_LIFE_GTE:
			role = room.getBattleRole(card.getPlayerId());
			if (role.getStatus(MINE_HERO_LIFE_GTE)) {
				if (model.Cancel == 1) {
					room.getTriggerManager().addTriggerEvent(TriggerManager.MINE_HERO_LIFE, model, card);
				}
				int hp = role.getStatus().get(MINE_HERO_LIFE_GTE);
				if (hp > 0 && role.getHp() >= hp) {
					return true;
				} else {
					this.removeEffect(TriggerManager.MINE_HERO_LIFE, room, card, model);
				}
			}
			break;
		case ENEMY_HERO_LIFE_LE:
			role = room.getBattleRole(card.getPlayerId());
			if (role.getStatus(ENEMY_HERO_LIFE_LE)) {
				enemyId = room.getEnemyId(role.getPlayerId());
				enemy = room.getBattleRole(enemyId);
				int hp = role.getStatus().get(ENEMY_HERO_LIFE_LE);
				if (hp > 0 && enemy.getHp() <= hp) {
					return true;
				}
			}
			break;
		case ENEMY_HERO_LIFE_GTE:
			role = room.getBattleRole(card.getPlayerId());
			if (role.getStatus(ENEMY_HERO_LIFE_GTE)) {
				enemyId = room.getEnemyId(role.getPlayerId());
				enemy = room.getBattleRole(enemyId);
				int hp = role.getStatus().get(ENEMY_HERO_LIFE_GTE);
				if (hp > 0 && enemy.getHp() >= hp) {
					return true;
				}
			}
			break;
		case BLESSING:
			playerId = card.getPlayerId();
			role = room.getBattleRole(playerId);
			return role.isBlessing();
		case COST_DOWN_1:
			role = room.getBattleRole(card.getPlayerId());
			if (card.getCost(role) <= 1) {
				return true;
			}
			break;
		case SAME:
			role = room.getBattleRole(card.getPlayerId());
			return role.isSameHandCard();
		case DWARF_TROOP:
			role = room.getBattleRole(card.getPlayerId());
			for (Area tempArea : role.getAreas()) {
				for (TroopCard tempTroop : tempArea.getTroops()) {
					if (CardModel.DWARF.equals(tempTroop.getSubType())) {
						return true;
					}
				}
			}
			return false;
		case ABERRATION_TROOP:
			role = room.getBattleRole(card.getPlayerId());
			for (Area tempArea : role.getAreas()) {
				for (TroopCard tempTroop : tempArea.getTroops()) {
					if (tempTroop.getUid() == card.getUid()) {
						continue;
					}
					if (tempTroop.isDead()) {
						continue;
					}
					if (CardModel.ABERRATION.equals(tempTroop.getSubType())) {
						return true;
					}
				}
			}
			this.removeEffect(ABERRATION_TROOP, room, card, model);
			return false;
		case DRIDER_TROOP_IN_HAND:
			role = room.getBattleRole(card.getPlayerId());
			ArrayList<CardBase> handCards = role.getHandCards();
			for (CardBase tempCard : handCards) {
				if (CardModel.TROOP == tempCard.getType() && CardModel.DRIDER.equals(tempCard.getSubType())) {
					return true;
				}
			}
			return false;
		case LOREMASTER_IN_HAND:
			role = room.getBattleRole(card.getPlayerId());
			handCards = role.getHandCards();
			for (CardBase tempCard : handCards) {
				if (CardModel.LOREMASTER.equals(tempCard.getSubType())) {
					return true;
				}
			}
			return false;
		case DEF_DAMAGE_10:
			role = room.getBattleRole(fighterId);
			if (role.getDamage() <= -10) {
				return true;
			}
			break;
			
		case DEF_DAMAGE_5:
			role = room.getBattleRole(fighterId);
			if (role.getDamage() <= -5) {
				return true;
			}
			break;

		case EFFECT_SUCC:
			return card.getStatus(EFFECT_SUCC);

		case IN_HAND:
			playerId = card.getPlayerId();
			role = room.getBattleRole(playerId);
			handCards = role.getHandCards();
			return handCards.indexOf(card) != -1;

		case TroopCard.FLIGHT:
			ArrayList<Object> targets = card.getTarget();
			if (targets.size() == 0) {
				return false;
			}
			Object object = targets.get(0);
			if (object instanceof TroopCard) {
				troop = (TroopCard) object;
				return troop.getStatus(TroopCard.FLIGHT);
			}
			return false;
		case FIRST_DECK_CARD_MODIFY:
			playerId = card.getPlayerId();
			role = room.getBattleRole(playerId);
			return !role.getStatus(BattleRole.DECK_CARD_MODIFY_COUNT);
		case ENEMY_HP13:
			enemy = room.getBattleRole(room.getEnemyId(card.getPlayerId()));
			if (enemy.getHp() == 13) {
				return true;
			}
			return false;
		case AREA_LV_UP:
			playerId = card.getPlayerId();
			role = room.getBattleRole(playerId);
			return role.isAreaLvUp();
		case ENEMY_CARD_6:
			enemy = room.getBattleRole(room.getEnemyId(card.getPlayerId()));
			if (enemy.getCardCount() >= 6) {
				return true;
			}
			break;
			
		case ATT_SELF:
			if (card.getUid() == attCard.getUid()) {
				return true;
			}
			break;
			
		case TRIGGER_SELF_AREA:
		case ATT_SELF_AREA:
			if (card.getPlayerId() != attCard.getPlayerId()) {
				return false;
			}
			Area tempArea = attCard.getArea() == null ? attCard.getOldArea() : attCard.getArea();
			if (tempArea == null) {
				return false;
			}
			if (card.getAreaIndex() != tempArea.getIndex()) {
				return false;
			}
			return true;
			
		case TRIGGER_MINE:
			if (card.getPlayerId() == attCard.getPlayerId()) {
				return true;
			}
			break;
			
		case ATT_OPP_AREA:
			if (card.getPlayerId() == attCard.getPlayerId()) {
				return false;
			}
			oppoAreaIndex = getOppoAreaIndex(card.getAreaIndex());
			if (oppoAreaIndex != attCard.getAreaIndex()) {
				return false;
			}
			return true;
			
		case ATT_ALIVE:
			if (attCard.isAlive()) {
				return true;
			}
			break;
			
		case DEF_SELF:
			IBattleObject attackTarget = arg.getDefCard();
			if (card.getUid() == attackTarget.getUid()) {
				return true;
			}
			break;
			
		case DEF_SELF_AREA:
			attackTarget = arg.getDefCard();
			if (attackTarget instanceof BattleRole) {
				return false;
			}
			TroopCard defCard = (TroopCard) attackTarget;
			if (card.getPlayerId() != defCard.getPlayerId()) {
				return false;
			}
			if (card.getAreaIndex() != defCard.getAreaIndex()) {
				return false;
			}
			return true;
			
		case DEF_STUN:
			attackTarget = arg.getDefCard();
			if (attackTarget instanceof BattleRole) {
				return false;
			}
			defCard = (TroopCard) attackTarget;
			if (defCard.isStun()) {
				return true;
			}
			break;
			
		case DEF_TROOP:
			attackTarget = arg.getDefCard();
			if (attackTarget instanceof TroopCard) {
				return true;
			}
			break;
			
		case DEF_HERO:
			attackTarget = arg.getDefCard();
			if (attackTarget instanceof BattleRole) {
				return true;
			}
			break;
			
		case DEF_MINE_HERO:
			attackTarget = arg.getDefCard();
			if (attackTarget instanceof BattleRole) {
				role = (BattleRole) attackTarget;
				if (card.getPlayerId() == role.getPlayerId()) {
					return true;
				}
			}
			break;
		
		case DEF_HERO_FIRST:
			attackTarget = arg.getDefCard();
			return isHeroBeAttackCount(attackTarget, 1);
			
		case DEF_HERO_SECOND:
			attackTarget = arg.getDefCard();
			return isHeroBeAttackCount(attackTarget, 2);
			
		case DEF_HERO_THIRD:
			attackTarget = arg.getDefCard();
			return isHeroBeAttackCount(attackTarget, 3);
			
		case DEF_ALIVE:
			attackTarget = arg.getDefCard();
			if (attackTarget instanceof TroopCard) {
				defCard = (TroopCard) attackTarget;
				if (defCard.isAlive()) {
					return true;
				}
			}
			break;
			
		case SELF:
			return card.getUid() == arg.getSelfCard().getUid();

		case SELF_ATK_LE_3:
		case SELF_ATK_LE_4:
			return true;
			
		case UNDEAD:
			if (card.getType() != CardModel.TROOP) {
				return false;
			}
			TroopCard tempTroop = (TroopCard) card;
			if (tempTroop.isUndead()) {
				return true;
			}
			return false;
			
		case ENEMY_STUN_COUNT:
			enemyId = room.getEnemyId(card.getPlayerId());
			enemy = room.getBattleRole(enemyId);
			if (enemy.getStunCount() > 0) {
				return true;
			}
			break;
			
		case DECK_ODD_COST:
			BattleRole fighter = arg.getFighter();
			ArrayList<CardBase> decks = fighter.getDecks();
			for (CardBase tempCard : decks) {
				if ((tempCard.getCost(fighter) % 2) == 0) {
					return false;
				}
			}
			return true;
			
		case TARGET_ORC:
			ArrayList<Object> target = room.getTarget(arg);
			if (target.size() == 0) {
				return false;
			}
			tempTroop = (TroopCard) target.get(0);
			if (CardModel.ORC.equals(tempTroop.getSubType())) {
				return true;
			}
			break;

		case TRIGGER_1001:
			if (card.getPlayerId() != attCard.getPlayerId()) {
				return false;
			}
			if (attCard.getId().equals("1001")) {
				return true;
			}
			break;

		case TARGET_10317:
			target = room.getTarget(arg);
			if (target.size() == 0) {
				return false;
			}
			tempTroop = (TroopCard) target.get(0);
			if (tempTroop.getId().equals("10317")) {
				return true;
			}
			break;
			
		case TARGET_DEAD:
			target = room.getTarget(arg);
			if (target.size() == 0) {
				return false;
			}
			Object obj = target.get(0);
			if (obj instanceof TroopCard) {
				tempTroop = (TroopCard) obj;
				if (tempTroop.isDead()) {
					return true;
				}
			}
			break;
			
		case TRIGGER_TROOP:
			CardBase trigger = arg.getTrigger();
			if (trigger instanceof TroopCard) {
				return true;
			}
			break;
			
		case TRIGGER_SPELL:
			trigger = arg.getTrigger();
			if (trigger instanceof SpellCard) {
				return true;
			}
			if (trigger instanceof TrapCard) {
				return true;
			}
			break;
			
		default:
			if (TriggerManager.PLAY_CARD.equals(model. Trigger)) {
				return true;
			}
			if (Tools.isNumber(model.Genius) && TriggerManager.DEATH.equals(model.Trigger)) {
				return true;
			}
			break;
		}
		return false;
	}

	private int getRune(String temple) {
		switch (temple) {
		case AIR_TEMPLE:
			return AIR;
		case EARTH_TEMPLE:
			return EARTH;
		case FIRE_TEMPLE:
			return FIRE;
		case WATER_TEMPLE:
			return WATER;
		}
		return -1;
	}
	
	private boolean isHeroBeAttackCount(IBattleObject obj, int count) {
		if (obj instanceof BattleRole) {
			BattleRole role = (BattleRole) obj;
			if (role.getStatus(TriggerManager.HERO_BE_ATTACK_COUNT) && role.getStatus().get(TriggerManager.HERO_BE_ATTACK_COUNT) == count) {
				return true;
			}
		}
		return false;
	}
	
	public int getOppoAreaIndex(int index) {
		return index;
	}

	/**
	 * 消除卡牌带来的技能效果
	 * 
	 * @param room
	 * @param selfCard
	 * @param self
	 */
	public void removeEffect(GameRoom room, CardBase selfCard, BattleRole self, boolean isStun) {
		HashMap<String, ArrayList<TroopCard>> effects = new HashMap<>();
		HashMap<String, Boolean> roleEffects = new HashMap<>();
		ArrayList<TroopCard> troops = new ArrayList<>();
		if (selfCard.getType() == CardModel.ARTIFACT) {
			Iterator<Entry<Integer, HashMap<Integer, Effect>>> iterator = selfCard.getEffects().entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<Integer, HashMap<Integer, Effect>> next = iterator.next();
				HashMap<Integer, Effect> value = next.getValue();
				for (Integer subIndex : value.keySet()) {
					Effect effect = value.get(subIndex);
					if (effect.isTemp()) {
						switch (effect.getEffectType()) {
						case BattleRole.AMPLIFY:
							self.addStatusCount(BattleRole.AMPLIFY, -effect.getValue());
							self.removeEffects(selfCard.getUid(), subIndex);
							roleEffects.put(effect.getEffectType(), true);
							break;

						case BattleRole.CONSTRUCT_TRAP_SELECT:
							self.removeConstructTrapSelect(selfCard);
							break;
							
						case DOUBLE_DAMAGE:
							int enemyId = room.getEnemyId(selfCard.getPlayerId());
							BattleRole enemy = room.getBattleRole(enemyId);
							enemy.addStatusCount(effect.getEffectType(), -1);
							break;
						}
					}
				}
			}
		}
		for (BattleRole role : room.getBattleRoles().values()) {
			for (Area area : role.getAreas()) {
				for (TroopCard card : area.getTroops()) {
					Iterator<Entry<Integer, HashMap<Integer, Effect>>> iterator = card.getEffects().entrySet().iterator();
					while (iterator.hasNext()) {
						Entry<Integer, HashMap<Integer, Effect>> next = iterator.next();
						Integer key = next.getKey();
						if ((card.getUid() == selfCard.getUid() && !isStun) || key == selfCard.getUid()) {
							HashMap<Integer, Effect> value = next.getValue();
							Iterator<Entry<Integer, Effect>> iterator2 = value.entrySet().iterator();
							while (iterator2.hasNext()) {
								Entry<Integer, Effect> entry = iterator2.next();
								Integer subIndex = entry.getKey();
								Effect effect = value.get(subIndex);
								if (effect.isTemp()) {
									delAttr(card, effect.getEffectType(), effect.getValue(), subIndex, role);
									setSyncList(card, effect.getEffectType(), effects, roleEffects, troops);
									iterator2.remove();
								}
							}
							if (card.getEffects().get(key).size() == 0) {
								iterator.remove();
							}
						}
					}
					if (card.getUid() == selfCard.getUid() && card.isDead()) {
						card.getEffects().clear();
					}
				}
			}
		}

		if (troops.size() > 0) {
			room.troopSync(selfCard.getPlayerId(), troops);
		}
		if (effects.size() > 0) {
			Iterator<Entry<String, ArrayList<TroopCard>>> iterator = effects.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, ArrayList<TroopCard>> next = iterator.next();
				String status = next.getKey();
				ArrayList<TroopCard> cards = next.getValue();
				room.troopStatusSync(cards, status);
			}
		}
		if (roleEffects.size() > 0) {
			Iterator<Entry<String, Boolean>> iterator = roleEffects.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, Boolean> next = iterator.next();
				String status = next.getKey();
				room.roleStatusSync(status, self);
			}
		}
	}

	public void troopStatusRemove(GameRoom room, TroopCard selfCard, String status) {
		Integer count = selfCard.getStatus().get(status);
		if (count == null || count <= 0) {
			return;
		}
		boolean needSend = false;
		Iterator<Entry<Integer, HashMap<Integer, Effect>>> iterator = selfCard.getEffects().entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Integer, HashMap<Integer, Effect>> next = iterator.next();
			Integer key = next.getKey();
			HashMap<Integer, Effect> value = next.getValue();
			Iterator<Entry<Integer, Effect>> iterator2 = value.entrySet().iterator();
			while (iterator2.hasNext()) {
				Entry<Integer, Effect> entry = iterator2.next();
				Integer subIndex = entry.getKey();
				Effect effect = value.get(subIndex);
				if (!effect.isTemp() && status.equals(effect.getEffectType())) {
					delAttr(selfCard, effect.getEffectType(), effect.getValue(), subIndex, null);
					needSend = true;
					iterator2.remove();
				}
			}
			if (selfCard.getEffects().get(key).size() == 0) {
				iterator.remove();
			}
		}

		if (selfCard.isAlive() && needSend) {
			room.troopStatusSync(selfCard, status);
		}
	}

	public void removeStunAmplify(GameRoom room, CardBase selfCard, BattleRole self) {
		HashMap<String, Boolean> roleEffects = new HashMap<>();
		ArrayList<TroopCard> troops = new ArrayList<>();
		Iterator<Entry<Integer, HashMap<Integer, Effect>>> iterator = selfCard.getEffects().entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Integer, HashMap<Integer, Effect>> next = iterator.next();
			Integer key = next.getKey();
			if (key == selfCard.getUid()) {
				continue;
			}
			HashMap<Integer, Effect> value = next.getValue();
			for (Integer subIndex : value.keySet()) {
				Effect effect = value.get(subIndex);
				if (effect.isTemp()) {
					switch (effect.getEffectType()) {
					case BattleRole.AMPLIFY:
						self.addStatusCount(BattleRole.AMPLIFY, -effect.getValue());
						self.removeEffects(selfCard.getUid(), subIndex);
						roleEffects.put(effect.getEffectType(), true);
						troops.add((TroopCard) selfCard);
						break;
					}
				}
			}
		}
		if (roleEffects.size() > 0) {
			Iterator<Entry<String, Boolean>> iter = roleEffects.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, Boolean> next = iter.next();
				String status = next.getKey();
				room.roleStatusSync(status, self);
			}
			room.troopStatusSync(troops, BattleRole.AMPLIFY);
		}
	}

	public void revertStunAmplify(GameRoom room, CardBase selfCard, BattleRole self) {
		HashMap<String, Boolean> roleEffects = new HashMap<>();
		ArrayList<TroopCard> troops = new ArrayList<>();
		Iterator<Entry<Integer, HashMap<Integer, Effect>>> iterator = selfCard.getEffects().entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Integer, HashMap<Integer, Effect>> next = iterator.next();
			Integer key = next.getKey();
			if (key == selfCard.getUid()) {
				continue;
			}
			HashMap<Integer, Effect> value = next.getValue();
			for (Integer subIndex : value.keySet()) {
				Effect effect = value.get(subIndex);
				if (effect.isTemp()) {
					switch (effect.getEffectType()) {
					case BattleRole.AMPLIFY:
						self.addStatusCount(BattleRole.AMPLIFY, effect.getValue());
						self.setEffects(selfCard.getUid(), subIndex, getEffect(effect));
						roleEffects.put(effect.getEffectType(), true);
						troops.add((TroopCard) selfCard);
						break;
					}
				}
			}
		}
		if (roleEffects.size() > 0) {
			Iterator<Entry<String, Boolean>> iter = roleEffects.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, Boolean> next = iter.next();
				String status = next.getKey();
				room.roleStatusSync(status, self);
			}
			room.troopStatusSync(troops, BattleRole.AMPLIFY);
		}
	}
	
	private void setSyncList(TroopCard card, String type, HashMap<String, ArrayList<TroopCard>> effects, HashMap<String, Boolean> roleEffects, ArrayList<TroopCard> troops) {
		switch (type) {
		case ADD_ATK:
		case ADD_ATK_HP:
		case ADD_HP:
			if (!card.isAlive()) {
				break;
			}
			troops.add(card);
			break;
		case BattleRole.AMPLIFY:
			roleEffects.put(type, true);
		case FLIGHT:
		case GUARDIAN:
		case LIFEDRAIN:
		case SPELL_BLOCK:
		case FORCE_SHIELD:
		case INVINCIBLE:
		case AVOID_ATTACKED:
		case AVOID_OPP_ATTACKED:
		case ALWAYS_ATTACK_HERO:
			if (!card.isAlive()) {
				break;
			}
			if (effects.get(type) == null) {
				effects.put(type, new ArrayList<>());
			}
			effects.get(type).add(card);
			break;

		case CANT_ATTACK:
			if (!card.isAlive()) {
				break;
			}
			if (effects.get(ATTACKED) == null) {
				effects.put(ATTACKED, new ArrayList<>());
			}
			effects.get(ATTACKED).add(card);
			break;

		case HERO_CANT_BE_ATTACK:
			roleEffects.put(type, true);
			break;
		case AREA_LV_UP_CHANGE_DRAW:
			roleEffects.put(type, true);
			break;
		}
	}

	/**
	 * 移除卡牌自身所有效果
	 * 
	 * @param room
	 * @param troop
	 */
	public void removeEffectByReturn(GameRoom room, TroopCard troop) {
		BattleRole self = room.getBattleRole(troop.getPlayerId());
		HashMap<String, Boolean> roleEffects = new HashMap<>();
		Iterator<Entry<Integer, HashMap<Integer, Effect>>> iterator = troop.getEffects().entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Integer, HashMap<Integer, Effect>> next = iterator.next();
			HashMap<Integer, Effect> value = next.getValue();
			for (Integer subIndex : value.keySet()) {
				Effect effect = value.get(subIndex);
				if (effect.isTemp()) {
					delAttr(troop, effect.getEffectType(), effect.getValue(), subIndex, self);
					switch (effect.getEffectType()) {
					case BattleRole.AMPLIFY:
						roleEffects.put(effect.getEffectType(), true);
					case HERO_CANT_BE_ATTACK:
						roleEffects.put(effect.getEffectType(), true);
						break;
					case AREA_LV_UP_CHANGE_DRAW:
						roleEffects.put(effect.getEffectType(), true);
						break;
					}
					troopEffectChangeSync(room, troop, effect);
				}
			}
			iterator.remove();
		}
		if (troop.isAttrChange()) {
			int enemyId = room.getEnemyId(troop.getPlayerId());
			room.troopSync(troop.getPlayerId(), enemyId, troop);
		}
		if (roleEffects.size() > 0) {
			Iterator<Entry<String, Boolean>> iter = roleEffects.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<String, Boolean> next = iter.next();
				String status = next.getKey();
				room.roleStatusSync(status, self);
			}
		}
	}

	private void troopEffectChangeSync(GameRoom room, TroopCard troop, Effect effect) {
		if (!troop.isChange()) {
			return;
		}
		if (room == null) {
			return;
		}
		switch (effect.getEffectType()) {
		case LEADER:
		case FLIGHT:
		case GUARDIAN:
		case LIFEDRAIN:
		case SPELL_BLOCK:
		case FORCE_SHIELD:
		case AVOID_ATTACKED:
		case AVOID_OPP_ATTACKED:
		case ALWAYS_ATTACK_HERO:
		case BattleRole.AMPLIFY:
			room.troopStatusSync(troop, effect.getEffectType());
			break;
		}
	}

	public void removeCostEffect(GameRoom room, CardBase selfCard, boolean isStun) {
		ArrayList<CardBase> costCards = new ArrayList<>();
		for (BattleRole role : room.getBattleRoles().values()) {
			boolean isSend = false;
			Iterator<Entry<Integer, HashMap<Integer, Effect>>> iterator = role.getEffects().entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<Integer, HashMap<Integer, Effect>> next = iterator.next();
				Iterator<Entry<Integer, Effect>> iterator2 = next.getValue().entrySet().iterator();
				while (iterator2.hasNext()) {
					Entry<Integer, Effect> next2 = iterator2.next();
					if (next.getKey() == selfCard.getUid()) {
						Effect effect = next2.getValue();
						if (effect.isTemp()) {
							if (isStun && effect.getEffectType().equals(String.valueOf(selfCard.getUid()))) {
								continue;
							}
							role.addCostArg(effect.getEffectType(), -effect.getValue());
							switch (effect.getEffectType()) {
							case MINE_TROOP_CARD:
								if (selfCard.getPlayerId() == role.getPlayerId()) {
									costCards.addAll(role.getHandCards());
									costCards.addAll(room.getTroopsByPlayerId(role.getPlayerId()));
								}
								break;
							case ENEMY_TROOP_CARD:
								if (selfCard.getPlayerId() != role.getPlayerId()) {
									costCards.addAll(role.getHandCards());
									costCards.addAll(room.getTroopsByPlayerId(role.getPlayerId()));
								}
								break;
							case ENEMY_SPELL_CARD:
								if (selfCard.getPlayerId() != role.getPlayerId()) {
									costCards.addAll(role.getHandCards());
								}
								break;
							case HAND_CARDS:
								if (selfCard.getPlayerId() == role.getPlayerId()) {
									costCards.addAll(role.getHandCards());
								}
								break;
							case AREA_COST:
								isSend = true;
								break;
							default:
								try {
									int cardUid = Integer.parseInt(effect.getEffectType());
									CardBase card = room.getCard(cardUid);
									if (card != null) {
										BattleRole fighter = room.getBattleRole(card.getPlayerId());
										if (card.getArea() != null || fighter.getHandCards().indexOf(card) != -1) {
											costCards.add(card);
										}
									}
								} catch (Exception e) {
								}
								break;
							}
							iterator2.remove();
							if (role.getCostArg().get(effect.getEffectType()) == 0) {
								role.getCostArg().remove(effect.getEffectType());
							}
						}
					}
				}
			}
			if (costCards.size() > 0) {
				room.costSync(null, costCards, null, true);
			}
			if (isSend) {
				room.areaLvUpNeedResource(role);
			}
		}
	}

	/**
	 * 移除特定卡牌效果
	 * 
	 * @param triggerEvent
	 * @param room
	 * @param selfCard
	 * @param model
	 */
	public void removeEffect(String triggerEvent, GameRoom room, CardBase selfCard, SkillModel model) {
		if (selfCard.getEffects() == null || selfCard.getEffects().size() == 0) {
			return;
		}
		BattleRole self = room.getBattleRole(selfCard.getPlayerId());
		ArrayList<TroopCard> troops = new ArrayList<>();
		HashMap<String, Boolean> roleEffects = new HashMap<>();
		HashMap<String, ArrayList<TroopCard>> effects = new HashMap<>();
		Iterator<Entry<Integer, HashMap<Integer, Effect>>> iterator = selfCard.getEffects().entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Integer, HashMap<Integer, Effect>> next = iterator.next();
			HashMap<Integer, Effect> value = next.getValue();
			Iterator<Entry<Integer, Effect>> effectIter = value.entrySet().iterator();

			while (effectIter.hasNext()) {
				Entry<Integer, Effect> entry = effectIter.next();
				Integer subIndex = entry.getKey();
				Effect effect = entry.getValue();
				if (model.Genius.equals(effect.getTriggerEvent())) {
					if (effect.isTemp()) {
						if (selfCard.getType() == CardModel.TROOP) {
							TroopCard troop = (TroopCard) selfCard;
							delAttr(troop, effect.getEffectType(), effect.getValue(), subIndex, self);
							setSyncList(troop, effect.getEffectType(), effects, roleEffects, troops);
							effectIter.remove();
						} else {
							switch (effect.getEffectType()) {
							case BattleRole.AMPLIFY:
								if (!selfCard.isStun()) {
									self.addStatusCount(BattleRole.AMPLIFY, -effect.getValue());
									self.removeEffects(selfCard.getUid(), subIndex);
								}
								effectIter.remove();
								roleEffects.put(effect.getEffectType(), true);
								break;

							case DOUBLE_AMPLIFY:
								self.setStatus(DOUBLE_AMPLIFY, false);
								roleEffects.put(BattleRole.AMPLIFY, true);
								break;
							}
						}
					}
				}
			}
		}
		
		if (troops.size() > 0) {
			room.troopSync(selfCard.getPlayerId(), troops);
		}
		if (effects.size() > 0) {
			Iterator<Entry<String, ArrayList<TroopCard>>> iter = effects.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, ArrayList<TroopCard>> next = iter.next();
				String status = next.getKey();
				ArrayList<TroopCard> cards = next.getValue();
				room.troopStatusSync(cards, status);
			}
		}
		if (roleEffects.size() > 0) {
			Iterator<Entry<String, Boolean>> iter = roleEffects.entrySet().iterator();
			while (iter.hasNext()) {
				Entry<String, Boolean> next = iter.next();
				String status = next.getKey();
				room.roleStatusSync(status, self);
			}
		}
	}

	public boolean triggerRegister(SkillArg arg) {

		SkillModel model = arg.getModel();
		CardBase selfCard = arg.getSelfCard();
		GameRoom room = arg.getRoom();

		switch (model.Trigger) {
		case TriggerManager.START:
		case TriggerManager.END:
		case TriggerManager.ENCHANT:
		case TriggerManager.BREACH:
		case TriggerManager.WARCRY:
			selfCard.setStatus(model.Trigger, true);
			if (selfCard.getType() == CardModel.SPELL) {
				room.getTriggerManager().addTriggerEvent(TriggerManager.END, model, selfCard);
			}
			return false;

		case TriggerManager.DEATHCRY:
			if (selfCard.getType() != CardModel.TROOP) {
				return false;
			}
			TroopCard troop = (TroopCard) selfCard;
			deathcry(room, troop);
			return false;

		case TriggerManager.AREA_TROOP_DEATH:
			if (SELF.equals(model.Target)) {
				room.getTriggerManager().addTriggerEvent(TriggerManager.DEATH, model, selfCard);
			} else {
				room.getTriggerManager().addTriggerEvent(TriggerManager.DEATH, model, selfCard, null);
			}
			return false;

		case TROOP_IN:
		case SPIDER_IN_AREA:
		case OPP_AREA_TROOP:
			room.getTriggerManager().addTriggerEvent(TriggerManager.ADD_TROOP, model, selfCard, null);
			return false;
		case TriggerManager.ARTI_BEFORE:
		case TriggerManager.TRAP_BEFORE:
		case TriggerManager.PLAY_CARD:
		case TriggerManager.ARTIFACT:
		case TriggerManager.TROOP:
		case TriggerManager.SPELL:
		case TriggerManager.SPELL_AFTER:
		case TriggerManager.PLANT:
		case TriggerManager.TRAP:
		case TriggerManager.TALE:
		case TriggerManager.SECOND:
		case TriggerManager.CURE_HERO:
		case TriggerManager.DEATH:
		case TriggerManager.CREATE_CARD:
		case TriggerManager.SELF_AREA_LV_UP:
			if (SELF.equals(model.Target)) {
				room.getTriggerManager().addTriggerEvent(model.Trigger, model, selfCard);
			} else {
				room.getTriggerManager().addTriggerEvent(model.Trigger, model, selfCard, null);
			}
			return false;

		case TriggerManager.TARGET:
			selfCard.setStatus(model.Trigger, true);
		case TriggerManager.AREA_LV_UP:
		case TriggerManager.FIND_TALE:
		case TriggerManager.COST_UP_7:
		case TriggerManager.TRIGGER_TRAP:
		case TriggerManager.AREA_TROOP:
		case TriggerManager.TROOP_CHANGE:
		case TriggerManager.FIRST:
		case TriggerManager.FIRST_TROOP:
		case TriggerManager.FIRST_SPELL:
		case TriggerManager.MOVE:
			room.getTriggerManager().addTriggerEvent(model.Trigger, model, selfCard);
			return false;
			
		case TriggerManager.ARTI_TROOP_CHANGE:
			room.getTriggerManager().addTriggerEvent(TriggerManager.TROOP_CHANGE, model, selfCard);
			break;
			
		case TriggerManager.ATTACK_BEFORE:
		case TriggerManager.ATTACK_AFTER:
		case TriggerManager.DAMAGE_BEFORE:
		case TriggerManager.DAMAGE_AFTER:
		case TriggerManager.DAMAGE_ALIVE:
		case TriggerManager.DISCARDS:
			if (room.getTriggerManager().addTriggerEvent(model.Trigger, model, selfCard, null)) {
				return false;
			}
			break;
		case TriggerManager.GUARDIAN_IN_AREA:
		case TriggerManager.WARCRY_IN_AREA:
			if (SELF.equals(model.Target)) {
				room.getTriggerManager().addTriggerEvent(TriggerManager.AREA_TROOP, model, selfCard);
			} else {
				room.getTriggerManager().addTriggerEvent(TriggerManager.AREA_TROOP, model, selfCard, null);
			}
			return false;

		case TriggerManager.DECK_CARD_MODIFY:
			return false;
		case TriggerManager.DRAW:
			room.getTriggerManager().addTriggerEvent(TriggerManager.DRAW, model, selfCard, null);
			return false;
		}

		if (!this.trigger(arg)) {
			return false;
		}
		return effect(arg);
	}

	/**
	 * Start Enchant Warcry 开始、蛊惑、战吼时的技能效果触发
	 * 
	 * @param room
	 * @param fighterId
	 * @param selfCard
	 * @param area
	 * @param self
	 * @param model
	 * @param sendType
	 * @return
	 */
	public boolean triggerEffect(SkillArg arg) {
		if (!this.trigger(arg)) {
			return false;
		}
		SkillModel model = arg.getModel();
		CardBase trigger = arg.getTriggerOwner();
		GameRoom room = arg.getRoom();
		switch (model.Trigger) {
		case TriggerManager.DEATHCRY:
			TroopCard card = (TroopCard) trigger;
			room.troopDeathcrySync(card);
			break;
		}
		return effect(arg);
	}

	/**
	 * 技能效果
	 * 
	 * @param room      当前游戏房间
	 * @param fighterId 当前操作执行玩家
	 * @param enemyId   敌方玩家
	 * @param selfCard  当前操作对应卡牌
	 * @param area      卡牌所在区域
	 * @param self      卡牌所属玩家
	 * @param model     执行的技能
	 * @param sendType
	 */
	public boolean effect(SkillArg arg) {

		boolean result = true;
		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		BattleRole self = arg.getFighter();
		SkillModel model = arg.getModel();

		String type = model.Type;
		switch (type) {
		case ADD_ATK:
		case ADD_ATK_HP:
		case ADD_HP:
		case SET_ATK:
		case SWITCH_ATK_HP:
			ArrayList<TroopCard> troops = troopAttr(arg);
			room.troopSync(self.getPlayerId(), troops);
			if (troops == null || troops.size() == 0) {
				result = false;
			}
			break;
		case SET_ATK_BY_HP:
			troops = setAtkByHp(arg);
			room.troopSync(self.getPlayerId(), troops);
			if (troops == null || troops.size() == 0) {
				result = false;
			}
			break;
			
		case HEAL:
			heal(arg);
			break;

		case ADD_RESOURCE:
			self.addResource();
			room.resourceSync(self);
			break;

		case GUARDIAN_REMOVE:
			troops = troopStatusRemove(arg, GUARDIAN);
			break;

		case FORCE_SHIELD_REMOVE:
			troops = troopStatusRemove(arg, FORCE_SHIELD);
			break;

		case SPELL_BLOCK_REMOVE:
			troops = troopStatusRemove(arg, SPELL_BLOCK);
			break;
			
		case FLIGHT_REMOVE:
			troops = troopStatusRemove(arg, FLIGHT);
			break;
			
		case INVINCIBLE_REMOVE:
			troops = troopStatusRemove(arg, INVINCIBLE);
			break;
			
		case ATTACKED:
			troops = troopStatus(arg, type);
			room.troopStatusSync(troops, type);
			room.troopStatusSync(troops, TroopCard.ENCHANT);
			if (troops == null || troops.size() == 0) {
				result = false;
			}
			break;

		case SPEED:
			troops = troopStatus(arg, type);
			room.troopStatusSync(troops, TroopCard.SLEEP);
			room.troopStatusSync(troops, TroopCard.SPEED);
			room.troopStatusSync(troops, TroopCard.ATTACKED);
			room.troopStatusSync(troops, TroopCard.ENCHANT);
			if (troops == null || troops.size() == 0) {
				result = false;
			}
			break;
			
		case GUARDIAN:
		case FLIGHT:
		case LIFEDRAIN:
		case FORCE_SHIELD:
		case SPELL_BLOCK:
		case INVINCIBLE:
		case AVOID_ATTACKED:
		case AVOID_OPP_ATTACKED:
		case ALWAYS_ATTACK_HERO:
			troops = troopStatus(arg, type);
			room.troopStatusSync(troops, type);
			if (troops == null || troops.size() == 0) {
				result = false;
			}
			break;
			
		case ATTACK_LIMIT_TROOP:
		case SPLASH:
		case EXCESS_DAMAGE:
		case HERO_ATTACKER:
		case RANDOM_TARGET:
		case OPP_AREA_ATTACK:
		case DOUBLE_DAMAGE:
		case MERMAID_LOVER:
		case ATTACK_LIMIT_ONCE:
		case ENEMY_CANT_CURE:
		case CANT_ATTACK:
		case ATTACK_INVINCIBLE:
		case DEFENDER_INVINCIBLE:
		case UNDEAD:
			troops = troopStatus(arg, type);
			if (troops == null || troops.size() == 0) {
				result = false;
			}
			break;

		case HERO_CANT_BE_ATTACK:
			troops = troopStatus(arg, type);
			room.roleCanBeAttack(self);
			if (troops == null || troops.size() == 0) {
				result = false;
			}
			break;

		case AREA_LV_UP_CHANGE_DRAW:
			troops = troopStatus(arg, type);
			room.areaLvUpChangeDraw(self);
			if (troops == null || troops.size() == 0) {
				result = false;
			}
			break;
			
		case ATTACK_LIMIT_SPELL:
			self.getAttackLimitSpell().add(selfCard);
			troops = troopStatus(arg, type);
			if (troops == null || troops.size() == 0) {
				result = false;
			}
			break;
			
		case ATTACK_TYPE:
			attackType(arg);
			break;

		case IMMUNE_SPELLBLOCK:
		case DRAW_CHANGE:
		case CURE_TO_REDUCE:
			selfCard.setStatus(type, true);
			break;
			
		case BattleRole.AMPLIFY:
			amplify(arg);
			break;

		case REDUCE_HP_OPP_AREA_TROOP:
			reduceHpOppAreaTroop(arg);
			break;

		case REDUCE_HP_RANDOM:
			reduceHpRandom(arg);
			break;

		case REDUCE_HP:
			reduceHp(arg);
			break;

		case KILL:
			troops = kill(arg);
			break;
			
		case KILL_ANYWHERE:
			killAnywhere(arg);
			break;

		case FILL_ONE:
			ArrayList<CardBase> cards = summon(arg, false, false);
			break;

		case FILL_FULL:
			fillFull(arg);
			break;

		case SUMMON:
			cards = summon(arg, true, false);
			break;

		case SUMMON_IN_HAND:
			cards = summonInHand(arg);
			break;

		case CHANGE:
			change(arg);
			break;
			
		case CHANGE_AND_RETURN:
			changeAndReturn(arg);
			break;

		case CHANGE_ANYWHERE:
			changeAnywhere(arg);
			break;
			
		case STUN:
			troops = stun(arg);
			break;

		case STUN_REMOVE:
			troops = stunRemove(arg);
			break;

		case SUPER_LEADER:
			troops = superLeader(arg);
			room.troopStatusSync(troops, TroopCard.LEADER);
			break;

		case LEADER:
			troops = leader(arg);
			room.troopStatusSync(troops, TroopCard.LEADER);
			break;

		case FIND:
			find(arg, 3);
			break;
			
		case FIND_5:
			find(arg, 5);
			break;
			
		case CONSTRUCT_TRAP:
			constructTrap(arg);
			break;

		case DRAW:
			self = room.getBattleRole(arg.getTriggerOwner().getPlayerId());
			
			cards = draw(arg);
			cards = room.drawCardAndSync(self, cards);
			selfCard.setEffectCards(cards);
			break;

		case DRAW_TO_ENEMY:
			int enemyId = room.getEnemyId(arg.getTriggerOwner().getPlayerId());
			BattleRole enemy = room.getBattleRole(enemyId);
			arg.setFighter(enemy);
			
			cards = draw(arg);
			cards = room.drawCardAndSync(enemy, cards);
			selfCard.setEffectCards(cards);
			break;

		case TURN:
			trun(arg);
			break;

		case COST:
			cards = cost(arg);
			room.costSync(null, cards, null, false);
			result = cards.size() > 0;
			break;

		case SHUFFLE:
			shuffle(arg);
			break;

		case COPY_SHUFFLE:
			copyShuffle(arg);
			break;

		case RETURN:
			cards = returnHandCard(arg, null);
			room.returnHandCardsSync(self, cards);
			break;
			
		case RETURN_TO_MINE:
			cards = returnHandCard(arg, self);
			room.returnHandCardsSync(self, cards);
			break;

		case MOVE:
			move(arg);
			break;

		case MOVE_SELF:
			moveSelf(arg);
			break;

		case MOVE_TO_SELF_AREA:
			moveToSelfArea(arg);
			break;
			
		case CHECK:
			check(arg);
			break;

		case CONTROL:
			troops = control(arg);
			room.troopStatusSync(troops, TroopCard.CONTROL);
			room.troopStatusSync(troops, TroopCard.SLEEP);
			room.troopStatusSync(troops, TroopCard.ATTACKED);
			room.troopStatusSync(troops, TroopCard.ENCHANT);
			break;

		case AWAKE:
			troops = awake(arg);
			room.troopStatusSync(troops, TroopCard.SLEEP);
			room.troopStatusSync(troops, TroopCard.ATTACKED);
			room.troopStatusSync(troops, TroopCard.ENCHANT);
			break;

		case AWAKE_FLIGHT:
			troops = awakeFlight(arg);
			room.troopStatusSync(troops, TroopCard.SLEEP);
			room.troopStatusSync(troops, TroopCard.ATTACKED);
			room.troopStatusSync(troops, TroopCard.ENCHANT);
			break;

		case AWAKE_ONLY:
			troops = awakeOnly(arg);
			room.troopStatusSync(troops, TroopCard.SLEEP);
			room.troopStatusSync(troops, TroopCard.ENCHANT);
			break;

		case AWAKE_NEARBY_AND_ORC_ATT2:
			troops = awakeNearbyAndOrcAtt2(arg);
			room.troopStatusSync(troops, TroopCard.SLEEP);
			room.troopStatusSync(troops, TroopCard.ATTACKED);
			room.troopStatusSync(troops, TroopCard.ENCHANT);
			room.troopSync(self.getPlayerId(), troops);
			break;

		case MINE_HERO_LIFE_LE:
		case MINE_HERO_LIFE_GTE:
		case ENEMY_HERO_LIFE_LE:
		case ENEMY_HERO_LIFE_GTE:
			heroLife(arg);
			break;

		case DISCARDS:
			discards(arg);
			break;
			
		case ENEMY_DISCARDS:
			enemy = room.getBattleRole(room.getEnemyId(selfCard.getPlayerId()));
			arg.setFighter(enemy);
			discards(arg);
			break;
			
		case ADD_REPL_RESOURCE:
			addReplResource(arg);
			break;

		case AREA_LV_UP:
			areaLvUp(arg);
			break;

		case COPY:
			cards = copy(arg);
			room.drawCardAndSync(self, cards);
			break;
			
		case SAVE:
			save(arg);
			break;
		
		case KILL_TROOP_COUNT:
			selfCard.setStatusTrun(type, 0);
			break;
			
		case REVEAL:
			reveal(arg);
			break;
		
		case FIREPACT:
		case EARTHPACT:
		case WATERPACT:
		case AIRPACT:
			runepact(arg);
			break;
			
		case ADD_RES_STOP:
			addResStop(arg);
			break;
			
		case BattleRole.CONSTRUCT_TRAP_SELECT:
			constructTrapSelectCount(arg);
			break;
			
		case BattleRole.EXTRA_TURN:
			extraTurn(arg);
			break;
			
		case DESTROY:
			destroy(arg);
			break;
			
		case REPLACE:
			replaceCard(arg);
			break;
			
		case DOUBLE_AMPLIFY:
			doubleAmplify(arg);
			break;
			
		case COPY_AND_PLAY:
			copyAndPlay(arg);
			break;
		}
		return result;
	}

	private ArrayList<CardBase> draw(SkillArg arg) {

		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		BattleRole self = arg.getFighter();
		SkillModel model = arg.getModel();
		
		String target = model.Target;
		ArrayList<CardBase> cards = new ArrayList<>();
		
		int count = expression(model.Value, arg);
		if (!Tools.isEmptyString(target)) {
			switch (target) {
			case FIRST_FIND:
			case SAVE:
				target = self.getFirstFind(selfCard.getUid());
				if (target == null) {
					return cards;
				}
				break;
			case DECK_TRAP:
				CardBase trap = null;
				ArrayList<CardBase> decks = self.getDecks();
				for (CardBase card : decks) {
					if (card.getType() == CardModel.TRAP) {
						trap = card;
					}
				}
				if (trap != null) {
					if (self.getDecks().remove(trap)) {
						if (selfCard.getDrawCostNumber() != null) {
							Integer cost = selfCard.getDrawCostNumber();
							trap.addCost(cost);
						} else if (selfCard.isDrawCostBySubType()) {
							Integer cost = selfCard.getDrawCostNumberBySubType(trap.getSubType());
							if (cost != null) {
								trap.addCost(cost);
							}
						}
						cards.add(trap);
						room.deckCardNumberSync(self);
					}
				}
				return cards;
			case ENEMY_DECK:
				int enemyId = room.getEnemyId(self.getPlayerId());
				BattleRole enemy = room.getBattleRole(enemyId);
				for (int i = 0; i < count; i++) {
					CardBase card = enemy.deal();
					if (card == null) {
						return cards;
					}
					room.deckCardNumberSync(enemy);
					card.setPlayerId(self.getPlayerId());
					card.setStatus(ENEMY_DECK, true);
					cards.add(card);
					self.setStatusTrun(BattleRole.LAST_DRAW_COST, card.getCost(self));
				}
				return cards;
			case BattleRole.LAST_TROOP:
				target = String.valueOf(self.getStatusCount(BattleRole.LAST_TROOP));
				break;
			}
		}

		if (Tools.isEmptyString(target)) {
			for (int i = 0; i < count; i++) {
				if (selfCard.getDrawCostNumber() != null || selfCard.isDrawCostBySubType()) {
					cards.add(selfCard);
				} else {
					selfCard.setStatus(SkillManager.DRAW_DECK, true);
					cards.add(selfCard);
				}
				logger.info("玩家：{}，房间Id：{}，卡牌Id:{}，触发条件：{}。抽取卡牌。", self.getPlayerId(), room.getRoomId(), model.ID,
						model.Genius);
			}
			return cards;
		}

		HashMap<String, CardModel> map = ConfigData.cardModelBySubType.get(target);
		if (map != null && map.size() > 0) {
			ArrayList<CardModel> list = new ArrayList<>();
			list.addAll(map.values());
			for (int i = 0; i < count; i++) {
				int random = Tools.random(0, list.size() - 1);
				CardModel cardModel = list.get(random);
				CardBase card = room.createCard(self.getPlayerId(), cardModel.ID);
				if (card == null) {
					continue;
				}
				if (selfCard.getDrawCostNumber() != null) {
					Integer cost = selfCard.getDrawCostNumber();
					card.addCost(cost);
				} else if (selfCard.isDrawCostBySubType()) {
					Integer cost = selfCard.getDrawCostNumberBySubType(card.getSubType());
					if (cost != null) {
						card.addCost(cost);
					}
				}
				cards.add(card);
			}
			return cards;
		}

		map = ConfigData.cardModelByQuality.get(target);
		if (map != null && map.size() > 0) {
			ArrayList<CardModel> list = new ArrayList<>();
			list.addAll(map.values());
			for (int i = 0; i < count; i++) {
				int random = Tools.random(0, list.size() - 1);
				CardModel cardModel = list.get(random);
				CardBase card = room.createCard(self.getPlayerId(), cardModel.ID);
				if (card == null) {
					continue;
				}
				if (selfCard.getDrawCostNumber() != null) {
					Integer cost = selfCard.getDrawCostNumber();
					card.addCost(cost);
				} else if (selfCard.isDrawCostBySubType()) {
					Integer cost = selfCard.getDrawCostNumberBySubType(card.getSubType());
					if (cost != null) {
						card.addCost(cost);
					}
				}
				cards.add(card);
			}
			return cards;
		}

		if (ConfigData.cardModels.get(target) != null) {
			for (int i = 0; i < count; i++) {
				CardBase card = room.createCard(self.getPlayerId(), target);
				if (card == null) {
					continue;
				}
				if (selfCard.getDrawCostNumber() != null) {
					Integer cost = selfCard.getDrawCostNumber();
					card.addCost(cost);
				} else if (selfCard.isDrawCostBySubType()) {
					Integer cost = selfCard.getDrawCostNumberBySubType(card.getSubType());
					if (cost != null) {
						card.addCost(cost);
					}
				}
				cards.add(card);
			}
			return cards;
		}
		return cards;
	}

	private void find(SkillArg arg, int num) {

		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		BattleRole self = arg.getFighter();
		SkillModel model = arg.getModel();
		
		String target = model.Target;
		ArrayList<CardModel> list = new ArrayList<>();
		HashMap<String, CardModel> map = ConfigData.cardModelBySubType.get(target);
		if (map == null || map.size() < num) {
			return;
		}
		list.addAll(map.values());
		int count = expression(model.Value, arg);
		for (int i = 0; i < count; i++) {
			list.clear();
			list.addAll(map.values());
			FindCard findCard = new FindCard();
			findCard.setCardId(model.ID);
			findCard.setCardUid(selfCard.getUid());
			findCard.setType(FindCard.FIND);
			for (int j = 0; j < num; j++) {
				int random = Tools.random(0, list.size() - 1);
				CardModel cardModel = list.remove(random);
				CardBase card = room.createCard(self.getPlayerId(), cardModel.ID);
				if (card != null) {
					findCard.addCards(card);
				}
			}
			if (self.getFindCards().size() == 0) {
				findCard.setFirst(true);
			}
			self.getFindCards().add(findCard);
			logger.info("玩家：{}，房间Id：{}，卡牌Id:{}。Find信息存储。", self.getPlayerId(), room.getRoomId(), findCard.getCardId());
		}
		room.findCardSync(self);
		if (self.isState()) {
			self.setInterruptSkillArg(arg);;
		}
	}

	private void constructTrap(SkillArg arg) {

		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		BattleRole self = arg.getFighter();
		SkillModel model = arg.getModel();
		
		HashMap<String, ArrayList<CardBase>> trapCards = room.getConstructTrapCards();
		if (trapCards.size() == 0) {
			HashMap<String, CardModel> input = ConfigData.cardModelBySubType.get(CardModel.TRAP_INPUT);
			if (input == null || input.size() < 3) {
				return;
			}
			HashMap<String, CardModel> output = ConfigData.cardModelBySubType.get(CardModel.TRAP_OUTPUT);
			if (output == null || output.size() < 3) {
				return;
			}

			ArrayList<CardBase> inputList = new ArrayList<>();
			ArrayList<CardBase> outputList = new ArrayList<>();
			for (CardModel cardModel : input.values()) {
				CardBase card = room.createCard(0, cardModel.ID);
				if (card != null) {
					inputList.add(card);
				}
			}
			for (CardModel cardModel : output.values()) {
				CardBase card = room.createCard(0, cardModel.ID);
				if (card != null) {
					outputList.add(card);
				}
			}

			trapCards.put(CardModel.TRAP_INPUT, inputList);
			trapCards.put(CardModel.TRAP_OUTPUT, outputList);
		}

		int count = expression(model.Value, arg);
		for (int i = 0; i < count; i++) {
			constructTrap(trapCards, CardModel.TRAP_INPUT, model, selfCard, self);
			constructTrap(trapCards, CardModel.TRAP_OUTPUT, model, selfCard, self);
			logger.info("玩家：{}，房间Id：{}，卡牌Id:{}。组装陷阱信息存储。", self.getPlayerId(), room.getRoomId(), model.ID);
		}
		room.findCardSync(self);
		if (self.isState()) {
			self.setInterruptSkillArg(arg);
		}
	}

	private void constructTrap(HashMap<String, ArrayList<CardBase>> trapCards, String type, SkillModel model,
			CardBase selfCard, BattleRole self) {
		ArrayList<CardBase> list = new ArrayList<>();
		list.addAll(trapCards.get(type));
		FindCard findCard = new FindCard();
		findCard.setCardId(model.ID);
		findCard.setCardUid(selfCard.getUid());
		findCard.setType(FindCard.CONSTRUCT_TRAP);
		findCard.setOutput(CardModel.TRAP_OUTPUT.equals(type));
		if (findCard.isOutput()) {
			if (model.Target.equals(SELF_AREA)) {
				findCard.setAreaIndex(selfCard.getAreaIndex());
			}
		}
		int size = self.getConstructTrapSelect();
		for (int j = 0; j < size; j++) {
			int random = Tools.random(0, list.size() - 1);
			CardBase card = list.remove(random);
			findCard.addCards(card);
			logger.debug("组装陷阱{}：{} {}", type, card.getRealId(), card.getUid());
		}
		list.clear();
		if (self.getFindCards().size() == 0) {
			findCard.setFirst(true);
		}
		self.getFindCards().add(findCard);
	}

	private void trun(SkillArg arg) {
		CardBase selfCard = arg.getSelfCard();
		if (selfCard.getStatus(ArtifactCard.TRUN)) {
			return;
		}
		selfCard.setStatus(ArtifactCard.TRUN, true);
		selfCard.setStatus(ArtifactCard.TRUN_COUNT, true);
	}

	private void deathcry(GameRoom room, TroopCard card) {
		card.setStatus(TriggerManager.DEATHCRY, true);
		BattleRole role = room.getBattleRole(card.getPlayerId());
		role.getQuestManager().cardStateCount(card, TriggerManager.DEATHCRY);
	}

	private void amplify(SkillArg arg) {
		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		Area area = arg.getArea();
		SkillModel model = arg.getModel();
		
		switch (model.Target) {
		case C_TROOP:
		case C_TROOP_MINE:
		case C_TROOP_ENEMY:
			ArrayList<Object> target = room.getTarget(arg);
			TroopCard troop = (TroopCard) target.get(0);
			amplify(arg, troop);
			break;

		case SELF:
			if (selfCard.getType() != CardModel.TROOP && selfCard.getType() != CardModel.ARTIFACT) {
				return;
			}
			amplify(arg, selfCard);
			break;

		case SELF_AREA:
			for (TroopCard card : area.getTroops()) {
				amplify(arg, card);
			}
			break;
		}
	}

	private void amplify(SkillArg arg, CardBase card) {
		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		SkillModel model = arg.getModel();
		int playerId = card.getPlayerId();
		BattleRole fighter = room.getBattleRole(playerId);
		if (card.isStun() && card.getUid() == selfCard.getUid()) {
			return;
		}
		if (model.Cancel == 0) {
			selfCard = card;
		}
		int valueInt = expression(model.Value, arg);
		Effect effect = fighter.getEffects(card.getUid(), model.Index);
		if (effect == null) {
			setEffects(card, selfCard, model, valueInt);
			card.addStatusCount(BattleRole.AMPLIFY, valueInt);
			if (!card.isStun()) {
				fighter.setEffects(card.getUid(), model.Index, getEffectByTarget(card, model, valueInt));
				fighter.addStatusCount(BattleRole.AMPLIFY, valueInt);
				room.amplifySync(card, valueInt);
				room.roleStatusSync(fighter, BattleRole.AMPLIFY);
			}
		} else {
			if (!effect.isRepeat()) {
				if (effect.getValue() != valueInt) {
					fighter.addStatusCount(BattleRole.AMPLIFY, -effect.getValue());
					card.addStatusCount(BattleRole.AMPLIFY, -effect.getValue());

					setEffects(card, selfCard, model, valueInt);
					card.addStatusCount(BattleRole.AMPLIFY, valueInt);
					if (!card.isStun()) {
						fighter.setEffects(card.getUid(), model.Index, getEffectByTarget(card, model, valueInt));
						fighter.addStatusCount(BattleRole.AMPLIFY, valueInt);
					}
					room.amplifySync(card, valueInt);
					room.roleStatusSync(fighter, BattleRole.AMPLIFY);
				}
			} else {
				int value = effect.getValue() + valueInt;
				setEffects(card, selfCard, model, value);
				card.addStatusCount(BattleRole.AMPLIFY, valueInt);
				
				if (!card.isStun()) {
					effect.setValue(value);
					fighter.addStatusCount(BattleRole.AMPLIFY, valueInt);
					room.amplifySync(card, valueInt);
					room.roleStatusSync(fighter, BattleRole.AMPLIFY);
				}
			}
		}
	}

	private void addReplResource(SkillArg arg) {
		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		SkillModel model = arg.getModel();
		int valueInt = expression(model.Value);
		switch (model.Target) {
		case ENEMY_HERO:
			int enemyId = room.getEnemyId(selfCard.getPlayerId());
			BattleRole enemy = room.getBattleRole(enemyId);
			enemy.addReplResourceOnly(valueInt);
			room.resourceSync(enemy);
			break;
		case SELF_HERO:
			BattleRole self = room.getBattleRole(selfCard.getPlayerId());
			self.addReplResourceOnly(valueInt);
			room.resourceSync(self);
			break;
		}
	}

	/**
	 * 部队牌 设置属性
	 * 
	 * @param arg
	 * @param card
	 * @param status
	 * @param result
	 */
	private void troopStatus(SkillArg arg, TroopCard card, String status, ArrayList<TroopCard> result) {
		
		CardBase selfCard = arg.getSelfCard();
		if (arg.getTriggerOwner().getType() == CardModel.TRAP) {
			selfCard = arg.getTriggerOwner();
		}
		SkillModel model = arg.getModel();
		Effect effect = card.getEffects(selfCard.getUid(), model.Index);
		GameRoom room = arg.getRoom();
		BattleRole role = room.getBattleRole(card.getPlayerId());
		role.getQuestManager().cardStateCount(card, status);
		
		if (model.Repeat == 0 && effect != null) {
			return;
		}
		int sendType = arg.getSendType();
		if (status.equals(SPEED)) {
			card.setSleep(false);
		}
		card.setStatus(status, true);
		card.setChange(true);
		setEffects(card, selfCard, model, 1);
		if (result != null && card.getArea() != null) {
			setTroopSyncResult(result, selfCard, card, sendType);
		}
	}

	private boolean cardStatus(SkillArg arg, CardBase card, String status) {
		
		CardBase selfCard = arg.getSelfCard();
		if (arg.getTriggerOwner().getType() == CardModel.TRAP) {
			selfCard = arg.getTriggerOwner();
		}
		SkillModel model = arg.getModel();
		Effect effect = card.getEffects(selfCard.getUid(), model.Index);
		if (effect == null) {
			card.setStatus(status, true);
			setEffects(card, selfCard, model, 1);
			return true;
		}
		return false;
	}
	
	private void heroLife(SkillArg arg) {
		BattleRole self = arg.getFighter();
		SkillModel model = arg.getModel();
		int valueInt = expression(model.Value);
		self.setStatusTrun(model.Type, valueInt);
	}

	private ArrayList<TroopCard> superLeader(SkillArg arg) {

		GameRoom room = arg.getRoom();
		BattleRole self = arg.getFighter();
		
		ArrayList<Object> target = room.getTarget(arg);
		if (target.size() <= 0) {
			return leader(arg);
		}
		Area area = null;
		for (Object obj : target) {
			TroopCard card = (TroopCard) obj;
			if (area == null) {
				area = self.getArea(card.getAreaIndex());
			} else {
				move(room, card, area);
			}
		}
		return leader(arg);
	}

	private ArrayList<TroopCard> leader(SkillArg arg) {

		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		SkillModel model = arg.getModel();
		int sendType = arg.getSendType();
		
		ArrayList<TroopCard> result = new ArrayList<>();
		switch (model.Target) {
		case C_TROOP:
		case C_TROOP_MINE:
		case C_TROOP_ENEMY:
			ArrayList<Object> target = room.getTarget(arg);
			if (target.size() <= 0) {
				return result;
			}
			TroopCard card = (TroopCard) target.get(0);
			if (TrapTriggerManager.getInstance().spellBlock(room, card, selfCard)) {
				break;
			}
			if (isSpellBlock(room, selfCard, card)) {
				break;
			}
			card.setStatus(TroopCard.LEADER, true);
			card.addLeaderSkillTriggers(model);
			setTroopSyncResult(result, selfCard, card, sendType);
			BattleRole cardOwner = room.getBattleRole(card.getPlayerId());
			room.areaSkillEffect(cardOwner.getArea(card.getAreaIndex()));
			break;
		default:
			card = (TroopCard) selfCard;
			card.setStatus(TroopCard.LEADER, true);
			setEffects(card, selfCard, model, 1);
			card.addLeaderSkillTriggers(model);
			setTroopSyncResult(result, selfCard, card, sendType);
			cardOwner = room.getBattleRole(card.getPlayerId());
			room.areaSkillEffect(cardOwner.getArea(card.getAreaIndex()));
		}
		return result;
	}

	/**
	 * 填满
	 * 
	 * @param arg
	 * @return
	 */
	private ArrayList<CardBase> fillFull(SkillArg arg) {

		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		Area area = arg.getArea();
		BattleRole self = arg.getFighter();
		SkillModel model = arg.getModel();

		ArrayList<CardBase> result = new ArrayList<>();
		selfCard.setEffectCards(result);
		
		switch (model.Target) {
		case ALL_ENEMY_AREA:
			arg.setFighter(room.getBattleRole(room.getEnemyId(self.getPlayerId())));
		case ALL_SELF_AREA:
		case SELF_AREA:
		case TRIGGER_AREA:
		case C_AREA:
		case C_AREA_MINE:
		case C_AREA_ENEMY:
		case C_TROOP_MINE:
		case C_TEMPLE_MINE:
			result.addAll(summon(arg, false, true));
			break;
		case OTHER_AREA:
			for (Area tempArea : self.getAreas()) {
				if (tempArea.getIndex() == area.getIndex()) {
					continue;
				}
				arg.setArea(tempArea);
				result.addAll(summon(arg, false, true));
			}
			break;
			
		case WATER_TEMPLE:
			if (arg.getFighter().getWaterTemple() != 0) {
				for (Area tempArea : arg.getFighter().getAreas()) {
					if (tempArea.getRune() != WATER || tempArea.getLevel() != Area.MAX_LEVEL) {
						continue;
					}
					arg.setArea(tempArea);
					result.addAll(summon(arg, false, true));
				}
			}
			break;
			
		case WATER_AREA:
			for (Area tempArea : arg.getFighter().getAreas()) {
				if (tempArea.getRune() != WATER || tempArea.getLevel() < 1) {
					continue;
				}
				arg.setArea(tempArea);
				result.addAll(summon(arg, false, true));
			}
			break;
		}
		return result;
	}

	/**
	 * 召唤、填充
	 * 
	 * @param isSummon 是否为召唤
	 * @return
	 */
	private ArrayList<CardBase> summon(SkillArg arg, boolean isSummon, boolean isFull) {

		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		Area area = arg.getArea();
		BattleRole self = arg.getFighter();
		SkillModel model = arg.getModel();
		int sendType = arg.getSendType();
		int count = 1;
		
		ArrayList<CardBase> result = new ArrayList<>();
		ArrayList<TroopCard> troops = new ArrayList<>();
		String cardId = getRandomCardIdBySubType(model.Value);
		TroopCard copy = null;
		if (COPY.equals(cardId)) {
			cardId = selfCard.getRealId();
			copy = (TroopCard) selfCard;
		}
		switch (model.Target) {
		case ALL_ENEMY_AREA:
		case ALL_SELF_AREA:
			for (Area tempArea : self.getAreas()) {
				if (isFull) {
					CardModel cardModel = ConfigData.cardModels.get(cardId);
					if (cardModel == null) {
						return result;
					}
					if (cardModel.type == CardModel.TROOP) {
						count = Area.ROW_MAX_INDEX - tempArea.getTroops().size();
					} else {
						count = Area.SUB_ROW_MAX - tempArea.getArtiTraps().size();
					}
				}
				for (int i = 0; i < count; i++) {
					CardBase card = room.summon(self.getPlayerId(), tempArea.getIndex(), cardId, copy, null, isSummon);
					if (card == null) {
						continue;
					}
					if (!COPY.equals(model.Value)) {
						cardId = getRandomCardIdBySubType(model.Value);
					}
					if (card.getType() == CardModel.TROOP) {
						TroopCard troop = (TroopCard) card;
						setTroopSyncResult(troops, selfCard, troop, sendType);
					} else {
						result.add(card);
					}
				}
			}
			break;

		case C_AREA:
		case C_AREA_MINE:
		case C_AREA_ENEMY:
		case C_TEMPLE_MINE:
			ArrayList<Object> target = room.getTarget(arg);
			if (target.size() == 0) {
				return result;
			}
			Area targetArea = (Area) target.get(0);
			if (isFull) {
				CardModel cardModel = ConfigData.cardModels.get(cardId);
				if (cardModel == null) {
					return result;
				}
				if (cardModel.type == CardModel.TROOP) {
					count = Area.ROW_MAX_INDEX - targetArea.getTroops().size();
				} else {
					count = Area.SUB_ROW_MAX - targetArea.getArtiTraps().size();
				}
			}
			for (int i = 0; i < count; i++) {
				CardBase card = room.summon(targetArea.getPlayerId(), targetArea.getIndex(), cardId, copy, null, isSummon);
				if (card == null) {
					continue;
				}
				if (!COPY.equals(model.Value)) {
					cardId = getRandomCardIdBySubType(model.Value);
				}
				if (card.getType() == CardModel.TROOP) {
					TroopCard troop = (TroopCard) card;
					setTroopSyncResult(troops, selfCard, troop, sendType);
				} else {
					result.add(card);
				}
			}
			break;

		case SELF_AREA:
		case OTHER_AREA:
		case WATER_TEMPLE:
		case WATER_AREA:
		case TRIGGER_AREA:
			if (area == null) {
				return result;
			}
			if (isFull) {
				CardModel cardModel = ConfigData.cardModels.get(cardId);
				if (cardModel == null) {
					return result;
				}
				if (cardModel.type == CardModel.TROOP) {
					count = Area.ROW_MAX_INDEX - area.getTroops().size();
				} else {
					count = Area.SUB_ROW_MAX - area.getArtiTraps().size();
				}
			}
			for (int i = 0; i < count; i++) {
				CardBase card = room.summon(area.getPlayerId(), area.getIndex(), cardId, copy, null, isSummon);
				if (card == null) {
					continue;
				}
				if (!COPY.equals(model.Value)) {
					cardId = getRandomCardIdBySubType(model.Value);
				}
				if (card.getType() == CardModel.TROOP) {
					TroopCard troop = (TroopCard) card;
					setTroopSyncResult(troops, selfCard, troop, sendType);
				} else {
					result.add(card);
				}
			}
			break;

		case C_TROOP_MINE:
			target = room.getTarget(arg);
			if (target.size() == 0) {
				return result;
			}
			copy = (TroopCard) target.get(0);
			cardId = copy.getRealId();
			area = copy.getArea();
			if (area == null) {
				return result;
			}
			result.add(copy);
			if (isFull) {
				CardModel cardModel = ConfigData.cardModels.get(cardId);
				if (cardModel == null) {
					return result;
				}
				if (cardModel.type == CardModel.TROOP) {
					count = Area.ROW_MAX_INDEX - area.getTroops().size();
				} else {
					count = Area.SUB_ROW_MAX - area.getArtiTraps().size();
				}
			}
			for (int i = 0; i < count; i++) {
				CardBase card = room.summon(area.getPlayerId(), area.getIndex(), cardId, copy, null, isSummon);
				if (card == null) {
					continue;
				}
				if (card.getType() == CardModel.TROOP) {
					TroopCard troop = (TroopCard) card;
					setTroopSyncResult(troops, selfCard, troop, sendType);
				} else {
					result.add(card);
				}
			}
			break;
		}
		result.addAll(troops);
		return result;
	}

	/**
	 * 从手牌召唤
	 * 
	 * @param arg
	 * @return
	 */
	private ArrayList<CardBase> summonInHand(SkillArg arg) {

		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		Area area = arg.getArea();
		BattleRole self = arg.getFighter();
		SkillModel model = arg.getModel();
		int sendType = arg.getSendType();

		ArrayList<CardBase> result = new ArrayList<>();
		ArrayList<TroopCard> troops = new ArrayList<>();
		String cardId = model.Value;
		switch (model.Target) {
		case TRIGGER_AREA:
		case SELF_AREA:
			if (area == null) {
				return result;
			}
			CardBase card = null;
			ArrayList<CardBase> handCards = self.getHandCards();
			for (CardBase temp : handCards) {
				if (temp.getId().equals(cardId)) {
					card = temp;
					break;
				}
			}
			if (card == null) {
				return result;
			}
			if (card.getType() != CardModel.TROOP) {
				return result;
			}
			TroopCard troop = (TroopCard) card;
			troop.setSleep(true);
			CardBase resultCard = room.summon(self.getPlayerId(), self, area, card, null, null, false, true);
			if (resultCard != null) {
				room.removeHandCard(self, card);
			}
			setTroopSyncResult(troops, selfCard, troop, sendType);
			break;
		}
		result.addAll(troops);
		return result;
	}
	
	public String getRandowCardId(String cardId) {
		String[] ids = Tools.splitToString(cardId);
		if (ids.length == 0) {
			return cardId;
		}
		if (ids.length == 1) {
			return ids[0];
		}
		int index = Tools.random(0, ids.length - 1);
		return ids[index];
	}

	private String getRandomCardIdBySubType(String subType) {
		return SkillManager.getInstance().getRandowCardId(getRandomCardIdBySubType(subType, 0));
	}

	private String getRandomCardIdBySubType(String subType, int type) {
		HashMap<String, CardModel> map = ConfigData.cardModelBySubType.get(subType);
		if (map != null && map.size() > 0) {
			ArrayList<CardModel> list = new ArrayList<>();
			list.addAll(map.values());
			int random = Tools.random(0, list.size() - 1);
			CardModel cardModel = list.get(random);
			if (type != 0) {
				if (cardModel.type == type) {
					return cardModel.ID;
				}
				list.clear();
				list.addAll(map.values());
				Collections.shuffle(list);
				for (CardModel model : map.values()) {
					if (model.type == type) {
						return cardModel.ID;
					}
				}
			}
			return cardModel.ID;
		}
		return subType;
	}


	private void changeAndReturn(SkillArg arg) {

		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		Area area = arg.getArea();
		BattleRole self = arg.getFighter();
		SkillModel model = arg.getModel();
		String cardId = getRandomCardIdBySubType(model.Value, CardModel.TROOP);
		ArrayList<CardBase> result = new ArrayList<>();
		
		switch (model.Target) {
		case SELF:
			if (area == null) {
				return;
			}
			if (selfCard.getType() != CardModel.TROOP) {
				return;
			}
			TroopCard oldCard = (TroopCard) selfCard;
			CardBase card = room.change(self.getPlayerId(), area.getIndex(), cardId, oldCard);
			arg.setSelfCard(card);
			returnHandCard(arg, card, result, self);
			room.returnHandCardsSync(self, result);
			break;
		}
	}
	
	private void change(SkillArg arg) {

		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		CardBase owner = arg.getTriggerOwner();
		Area area = arg.getArea();
		SkillModel model = arg.getModel();
		String cardId = getRandomCardIdBySubType(model.Value, CardModel.TROOP);
		
		switch (model.Target) {
		case SELF:
			if (area == null) {
				return;
			}
			if (owner.getType() != CardModel.TROOP) {
				return;
			}
			TroopCard oldCard = (TroopCard) owner;
			room.change(oldCard.getPlayerId(), area.getIndex(), cardId, oldCard);
			break;
		case C_TROOP:
		case C_TROOP_MINE:
		case C_TROOP_ENEMY:
			ArrayList<Object> targets = room.getTarget(arg);
			if (targets.size() == 0) {
				return;
			}
			TroopCard troop = (TroopCard) targets.get(0);
			if (TrapTriggerManager.getInstance().spellBlock(room, troop, selfCard)) {
				break;
			}
			if (isSpellBlock(room, selfCard, troop)) {
				break;
			}
			room.change(troop.getPlayerId(), troop.getAreaIndex(), cardId, troop);
			break;
		case NON_ABERRATION:
			for (BattleRole role : room.getBattleRoles().values()) {
				ArrayList<TroopCard> troops = room.getTroopsByPlayerId(role.getPlayerId(), selfCard, model);
				for (TroopCard temp : troops) {
					if (CardModel.ABERRATION.equals(temp.getSubType())) {
						continue;
					}
					if (isSpellBlock(room, selfCard, temp)) {
						break;
					}
					room.change(temp.getPlayerId(), temp.getAreaIndex(), cardId, temp);
				}
			}
			break;
		case DECK_TROOP_1: 
			oldCard = (TroopCard) selfCard;
			TroopCard copy = (TroopCard) getDeckNext(room, selfCard, CardModel.TROOP, null, null);
			CardBase card = room.createCard(selfCard.getPlayerId(), copy.getRealId());
			if (card == null) {
				return;
			}
			TroopCard change = (TroopCard) card;
			change.copy(copy);
			room.change(oldCard.getPlayerId(), selfCard.getAreaIndex(), change, oldCard);
			break;

		case ENEMY_TROOP_ATTACK_LESS_SELF:
			if (selfCard.getType() != CardModel.TROOP) {
				return;
			}
			troop = (TroopCard) selfCard;
			ArrayList<TroopCard> troops = room.getTroopsByPlayerId(room.getEnemyId(selfCard.getPlayerId()));
			for (TroopCard temp : troops) {
				if (temp.getRealAttack() < troop.getRealAttack()) {
					if (isSpellBlock(room, selfCard, temp)) {
						break;
					}
					room.change(temp.getPlayerId(), temp.getAreaIndex(), cardId, temp);
				}
			}
			break;
		}
	}

	private void changeAnywhere(SkillArg arg) {

		GameRoom room = arg.getRoom();
		BattleRole fighter = arg.getFighter();
		SkillModel model = arg.getModel();
		String target = model.Target;
		String cardId = model.Value;
		
		changeArea(room, fighter, target, cardId);
		changeHandcard(room, fighter, target, cardId);
		changeDeck(room, fighter, target, cardId);
	}
	
	private void changeArea(GameRoom room, BattleRole fighter, String target, String cardId) {
		CardModel cardModel = ConfigData.cardModels.get(target);
		if (cardModel == null) {
			return;
		}
		/*
		 * 转换场上部队
		 */
		if (cardModel.type == CardModel.TROOP) {
			ArrayList<TroopCard> troops = room.getTroopsByPlayerId(fighter.getPlayerId());
			for (TroopCard temp : troops) {
				if (target.equals(temp.getId())) {
					room.change(temp.getPlayerId(), temp.getAreaIndex(), cardId, temp);
				}
			}
		}
	}
	
	private void changeHandcard(GameRoom room, BattleRole fighter, String target, String cardId) {
		/*
		 * 转换手牌
		 */
		ArrayList<CardBase> handCards = new ArrayList<>();
		handCards.addAll(fighter.getHandCards());
		ArrayList<CardBase> from = new ArrayList<>();
		ArrayList<CardBase> to = new ArrayList<>();
		for (CardBase temp : handCards) {
			if (target.equals(temp.getId())) {
				int index = fighter.getHandCards().indexOf(temp);
				CardBase createCard = room.createCard(fighter.getPlayerId(), cardId);
				if (createCard == null) {
					continue;
				}
				room.removeHandCard(fighter, temp);
				room.addHandCard(fighter, createCard, index);
				from.add(temp);
				to.add(createCard);
			}
		}
		room.transform(fighter, from, to);
	}
	
	private void changeDeck(GameRoom room, BattleRole fighter, String target, String cardId) {
		/*
		 * 转换卡组
		 */
		ArrayList<CardBase> decks = new ArrayList<>();
		decks.addAll(fighter.getDecks());
		for (CardBase temp : decks) {
			if (target.equals(temp.getId())) {
				int index = fighter.getDecks().indexOf(temp);
				CardBase createCard = room.createCard(fighter.getPlayerId(), cardId);
				if (createCard == null) {
					continue;
				}
				fighter.getDecks().remove(temp);
				fighter.getDecks().add(index, createCard);
			}
		}
	}
	
	private void areaLvUp(SkillArg arg) {
		
		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		Area area = arg.getArea();
		SkillModel model = arg.getModel();
		BattleRole self = arg.getFighter();
		
		switch (model.Target) {
		case ALL_SELF_AREA:
			for (Area tempArea : self.getAreas()) {
				if (tempArea.getLevel() < Area.MAX_LEVEL) {
					room.areaLvUp(tempArea, self);
				}
			}
			break;

		case SELF_AREA:
			if (area.getLevel() < Area.MAX_LEVEL) {
				room.areaLvUp(area, self);
			}
			break;

		case C_AREA_MINE:
		case C_GRID_MINE:
			ArrayList<Object> target = room.getTarget(arg);
			target = room.getTarget(arg);
			if (target.size() == 0) {
				return;
			}
			Area targetArea = (Area) target.get(0);
			if (targetArea.getLevel() < Area.MAX_LEVEL) {
				room.areaLvUp(targetArea, self);
			}
			break;

		case LV_UP_ENEMY_RANDOM_AREA:
			int enemyId = room.getEnemyId(selfCard.getPlayerId());
			BattleRole enemy = room.getBattleRole(enemyId);
			ArrayList<Area> list = new ArrayList<>();
			for (Area tempArea : enemy.getAreas()) {
				if (tempArea.getLevel() < Area.MAX_LEVEL) {
					list.add(tempArea);
				}
			}
			if (list.size() == 0) {
				return;
			}
			int index = Tools.random(0, list.size() - 1);
			Area tempArea = list.get(index);
			room.areaLvUp(tempArea, enemy);
			break;
		}
	}

	private ArrayList<CardBase> copy(SkillArg arg) {
		
		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		SkillModel model = arg.getModel();
		BattleRole self = arg.getFighter();
		ArrayList<CardBase> cards = new ArrayList<>();
		
		switch (model.Target) {
		case TOP_IS_TROOP:
			BattleRole role = room.getBattleRole(selfCard.getPlayerId());
			ArrayList<CardBase> decks = role.getDecks();
			if (decks.size() == 0) {
				return cards;
			}
			CardBase cardBase = decks.get(0);
			if (cardBase.getType() != CardModel.TROOP) {
				return cards;
			}
			TroopCard copy = (TroopCard) cardBase;
			CardBase card = room.createCard(role.getPlayerId(), copy.getRealId());
			if (card == null) {
				return cards;
			}
			TroopCard troop = (TroopCard) card;
			troop.copy(copy);
			cards.add(card);
			break;
		case C_HAND_CARD_MINE:
			ArrayList<Object> target = room.getTarget(arg);
			if (target.size() <= 0) {
				room.sendTargetSelect(selfCard, arg);
				return cards;
			}
			cardBase = (CardBase) target.get(0);
			int valueInt = expression(model.Value);
			for (int i = 0; i < valueInt; i++) {
				card = room.createCard(self.getPlayerId(), cardBase.getRealId());
				if (card == null) {
					return cards;
				}
				card.copy(cardBase);
				cards.add(card);
			}
			break;
		}
		return cards;
	}

	private void save(SkillArg arg) {
		
		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		SkillModel model = arg.getModel();
		BattleRole self = arg.getFighter();

		ArrayList<Object> target = room.getTarget(arg);
		if (target.size() <= 0) {
			return;
		}
		switch (model.Target) {
		case C_HAND_CARD_MINE:
		case C_HAND_CARD_SPELL:
			CardBase card = (CardBase) target.get(0);
			self.setFirstFind(selfCard.getUid(), card.getRealId());
			break;
		}
	}

	private void reveal(SkillArg arg) {
		
		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		SkillModel model = arg.getModel();

		switch (model.Target) {
		case SELF:
			room.revealCardSync(selfCard);
			break;
		case MINE_DECK:
			BattleRole role = room.getBattleRole(selfCard.getPlayerId());
			ArrayList<CardBase> decks = role.getDecks();
			reveal(arg, decks);
			room.deckCardNumberSync(role);
			break;
		case ENEMY_DECK:
			int enemyId = room.getEnemyId(selfCard.getPlayerId());
			BattleRole enemy = room.getBattleRole(enemyId);
			decks = enemy.getDecks();
			reveal(arg, decks);
			room.deckCardNumberSync(enemy);
			break;
		}
	}
	
	private void reveal(SkillArg arg, ArrayList<CardBase> decks) {
		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		SkillModel model = arg.getModel();
		BattleRole self = arg.getFighter();
		
		if (decks.size() == 0) {
			return;
		}
		ArrayList<CardBase> cards = new ArrayList<>();
		cards.addAll(decks);
		
		FindCard findCard = new FindCard();
		findCard.setCardId(model.ID);
		findCard.setCardUid(selfCard.getUid());
		findCard.setType(FindCard.REVEAL);
		
		if (cards.size() > 5) { 
			Collections.shuffle(cards);
			for (int i = 0; i < 5; i++) {
				CardBase cardBase = cards.get(i);
				decks.remove(cardBase);
				findCard.addCards(cardBase);
			}
		} else {
			for (CardBase card : cards) {
				decks.remove(card);
				findCard.addCards(card);
			}
		}
		if (self.getFindCards().size() == 0) {
			findCard.setFirst(true);
		}
		self.getFindCards().add(findCard);
		logger.info("玩家：{}，房间Id：{}，卡牌Id:{}。揭示信息存储。", self.getPlayerId(), room.getRoomId(), findCard.getCardId());
		room.findCardSync(self);
	}

	private void runepact(SkillArg arg) {
		GameRoom room = arg.getRoom();
		SkillModel model = arg.getModel();
		BattleRole fighter = arg.getFighter();
		int valueInt = expression(model.Value, arg);
		fighter.addStatusCount(model.Type, valueInt);
		room.roleStatusSync(fighter, model.Type);
	}

	private void addResStop(SkillArg arg) {
		BattleRole fighter = arg.getFighter();
		fighter.setStatus(ADD_RES_STOP, true);
	}

	private void extraTurn(SkillArg arg) {
		BattleRole fighter = arg.getFighter();
		fighter.setStatus(BattleRole.EXTRA_TURN, true);
	}

	private void doubleAmplify(SkillArg arg) {
		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		SkillModel model = arg.getModel();
		BattleRole fighter = arg.getFighter();
		fighter.setStatus(DOUBLE_AMPLIFY, true);
		setEffects(selfCard, selfCard, model, 1);
		room.roleStatusSync(fighter, BattleRole.AMPLIFY);
	}

	private void copyAndPlay(SkillArg arg) {
		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		CardBase trigger = arg.getTrigger();
		CardBase card = room.createCard(trigger.getPlayerId(), trigger.getId());
		if (card == null) {
			return;
		}
		card.copy(trigger);
		card.setCost(0);
		card.setStatus(COPY_AND_PLAY, true);
		
		BattleRole self = room.getBattleRole(trigger.getPlayerId());
		int enemyId = room.getEnemyId(self.getPlayerId());
		
		FindCard findCard = new FindCard();
		findCard.setCardId(selfCard.getId());
		findCard.setCardUid(selfCard.getUid());
		findCard.setType(FindCard.SUMMON_COPY);
		findCard.addCards(card);
		if (self.getFindCards().size() == 0) {
			findCard.setFirst(true);
		}
		self.getFindCards().add(findCard);

		FightMsgSend.enemyCardSync(room.getSession(enemyId), findCard.getCards(), self);
		room.findCardSync(self);
		if (self.isState()) {
			self.setInterruptSkillArg(arg);;
		}
	}

	private void attackType(SkillArg arg) {
		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		if (selfCard.getType() != CardModel.TROOP) {
			return;
		}
		TroopCard troop = (TroopCard) selfCard;
		SkillModel model = arg.getModel();
		int value = expression(model.Value);
		troop.setAttackType(value);
		if (troop.getArea() != null) {
			room.troopStatusSync(troop, TroopCard.ATTACK_TYPE);
		}
	}

	private void replaceCard(SkillArg arg) {
		BattleRole fighter = arg.getFighter();
		SkillModel model = arg.getModel();
		String cardId = model.Target;
		String replaceCardId = model.Value;
		if (ConfigData.cardModels.get(cardId) == null) {
			return;
		}
		if (ConfigData.cardModels.get(replaceCardId) == null) {
			return;
		}
		fighter.setReplaceCard(cardId, replaceCardId);
	}
	
	private void constructTrapSelectCount(SkillArg arg) {
		SkillModel model = arg.getModel();
		BattleRole fighter = arg.getFighter();
		CardBase card = arg.getSelfCard();
		int valueInt = expression(model.Value);
		setEffects(card, card, model, valueInt);
		fighter.setConstructTrapSelect(valueInt);
		card.setStatusTrun(BattleRole.CONSTRUCT_TRAP_SELECT, valueInt);
	}
	
	private void move(SkillArg arg) {

		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		SkillModel model = arg.getModel();
		selfCard.setStatus(EFFECT_SUCC, false);
		
		ArrayList<Object> target = room.getTarget(arg);
		if (target.size() <= 1) {
			return;
		}
		switch (model.Target) {
		case C_TROOP:
		case C_TROOP_MINE:
		case C_TROOP_ENEMY:
		case C_TROOP_FLIGHT_MINE:
			TroopCard card = (TroopCard) target.get(0);
			Area area = (Area) target.get(1);
			if (TrapTriggerManager.getInstance().spellBlock(room, card, selfCard)) {
				break;
			}
			if (isSpellBlock(room, selfCard, card)) {
				break;
			}
			boolean result = move(room, card, area);
			selfCard.setStatus(EFFECT_SUCC, result);
			room.triggerEffect(TriggerManager.MOVE, card.getPlayerId(), 1);
			break;
		}
		return;
	}
	
	private void moveSelf(SkillArg arg) {

		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		SkillModel model = arg.getModel();
		selfCard.setStatus(EFFECT_SUCC, false);
		
		ArrayList<Object> target = room.getTarget(arg);
		if (target.size() < 1) {
			return;
		}
		switch (model.Target) {
		case C_AREA_MINE:
			TroopCard card = (TroopCard) target.get(0);
			Area area = (Area) target.get(1);
			if (TrapTriggerManager.getInstance().spellBlock(room, card, selfCard)) {
				break;
			}
			if (isSpellBlock(room, selfCard, card)) {
				break;
			}
			boolean result = move(room, card, area);
			selfCard.setStatus(EFFECT_SUCC, result);
			room.triggerEffect(TriggerManager.MOVE, card.getPlayerId(), 1);
			break;
		}
		return;
	}

	private void moveToSelfArea(SkillArg arg) {

		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		SkillModel model = arg.getModel();
		selfCard.setStatus(EFFECT_SUCC, false);
		
		ArrayList<Object> target = room.getTarget(arg);
		if (target.size() <= 0) {
			return;
		}
		if (selfCard.getType() != CardModel.TROOP) {
			return;
		}
		if (selfCard.getAreaIndex() == -1) {
			return;
		}
		switch (model.Target) {
		case C_TROOP:
		case C_TROOP_MINE:
		case C_TROOP_ENEMY:
		case C_TROOP_FLIGHT_MINE:
			TroopCard card = (TroopCard) target.get(0);
			BattleRole role = room.getBattleRole(selfCard.getPlayerId());
			Area area = role.getArea(selfCard.getAreaIndex());
			if (TrapTriggerManager.getInstance().spellBlock(room, card, selfCard)) {
				break;
			}
			if (isSpellBlock(room, selfCard, card)) {
				break;
			}
			boolean result = move(room, card, area);
			selfCard.setStatus(EFFECT_SUCC, result);
			room.triggerEffect(TriggerManager.MOVE, card.getPlayerId(), 1);
			break;
		}
		return;
	}

	private void check(SkillArg arg) {

		GameRoom room = arg.getRoom();
		BattleRole self = arg.getFighter();
		
		if (self.getDecks().size() == 0) {
			return;
		}
		CardBase card = self.getDecks().get(0);
		self.setStatus(BattleRole.CHECK, true);
		room.checkCardSync(self, card);
		if (self.isState()) {
			self.setInterruptSkillArg(arg);
		}
	}

	private ArrayList<TroopCard> control(SkillArg arg) {

		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		SkillModel model = arg.getModel();
		int sendType = arg.getSendType();
		
		ArrayList<TroopCard> result = new ArrayList<>();
		ArrayList<Object> target = room.getTarget(arg);
		if (target.size() <= 0) {
			return result;
		}
		switch (model.Target) {
		case C_TROOP_ENEMY:
			TroopCard card = (TroopCard) target.get(0);
			if (TrapTriggerManager.getInstance().spellBlock(room, card, selfCard)) {
				break;
			}
			if (isSpellBlock(room, selfCard, card)) {
				break;
			}
			BattleRole role = room.getBattleRole(selfCard.getPlayerId());
			Area area = role.getArea(selfCard.getAreaIndex());
			if (move(room, card, area)) {
				control(card, role);
				setTroopSyncResult(result, selfCard, card, sendType);
			}
			break;
		}
		return result;
	}

	private void control(CardBase selfCard, BattleRole role) {
		TroopCard card = (TroopCard) selfCard;
		card.setPlayerId(role.getPlayerId());
		card.setStatus(TroopCard.CONTROL, true);
		card.setSleep(false);
		card.setStatus(TroopCard.ATTACKED, false);
	}

	private ArrayList<TroopCard> awakeOnly(SkillArg arg) {

		CardBase selfCard = arg.getSelfCard();
		BattleRole self = arg.getFighter();
		SkillModel model = arg.getModel();
		int sendType = arg.getSendType();
		
		ArrayList<TroopCard> result = new ArrayList<>();
		switch (model.Target) {
		case CardModel.FAE:
			if (isTargetSubType(model.Target)) {
				for (Area tempArea : self.getAreas()) {
					for (TroopCard tempTroop : tempArea.getTroops()) {
						if (model.Target.equals(tempTroop.getSubType())) {
							tempTroop.setSleep(false);
							tempTroop.setStatus(TroopCard.AWAKE_ONLY, true);
							setTroopSyncResult(result, selfCard, tempTroop, sendType);
						}
					}
				}
			}
			break;
		}
		return result;
	}

	private ArrayList<TroopCard> awakeFlight(SkillArg arg) {

		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		SkillModel model = arg.getModel();
		int sendType = arg.getSendType();
		
		ArrayList<TroopCard> result = new ArrayList<>();
		ArrayList<Object> target = room.getTarget(arg);
		if (target.size() <= 0) {
			return result;
		}
		switch (model.Target) {
		case C_AREA_MINE:
			target = room.getTarget(arg);
			if (target.size() == 0) {
				return result;
			}
			Area targetArea = (Area) target.get(0);
			for (TroopCard temp : targetArea.getTroops()) {
				if (temp.isFlight()) {
					awake(room, temp);
					setTroopSyncResult(result, selfCard, temp, sendType);
				}
			}
			break;
		}
		return result;
	}

	private ArrayList<TroopCard> awake(SkillArg arg) {

		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		SkillModel model = arg.getModel();
		int sendType = arg.getSendType();
		
		ArrayList<TroopCard> result = new ArrayList<>();
		switch (model.Target) {
		case C_TROOP:
		case C_TROOP_MINE:
		case C_TROOP_ENEMY:
		case C_TROOP_AREA_SELF:
		case C_TROOP_AREA_SELF_UNAWAKE:
			ArrayList<Object> target = room.getTarget(arg);
			if (target.size() <= 0) {
				return result;
			}
			TroopCard card = (TroopCard) target.get(0);
			awake(room, card);
			setTroopSyncResult(result, selfCard, card, sendType);
			break;
		case SELF:
			card = (TroopCard) selfCard;
			awake(room, card);
			setTroopSyncResult(result, selfCard, card, sendType);
			break;
		}
		return result;
	}

	private ArrayList<TroopCard> awakeNearbyAndOrcAtt2(SkillArg arg) {

		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		SkillModel model = arg.getModel();
		int sendType = arg.getSendType();
		
		ArrayList<TroopCard> result = new ArrayList<>();
		ArrayList<Object> target = room.getTarget(arg);
		if (target.size() <= 0) {
			return result;
		}
		switch (model.Target) {
		case C_TROOP_MINE:
			target = room.getTarget(arg);
			if (target.size() == 0) {
				return result;
			}
			TroopCard troop = (TroopCard) target.get(0);
			BattleRole role = room.getBattleRole(troop.getPlayerId());
			Area area = role.getArea(troop.getAreaIndex());
			for (TroopCard temp : area.getTroops()) {
				if (awake(room, temp)) {
					if (CardModel.ORC.equals(temp.getSubType())) {
						temp.addAttack(2);
					}
					setTroopSyncResult(result, selfCard, temp, sendType);
				}
			}
			break;
		}
		return result;
	}

	private boolean awake(GameRoom room, CardBase selfCard) {
		if (!room.checkPlayer(selfCard.getPlayerId())) {
			return false;
		}
		TroopCard card = (TroopCard) selfCard;
		if (card.isSleep() || card.getStatus(TroopCard.ATTACKED)) {
			card.setSleep(false);
			card.setStatus(TroopCard.ATTACKED, false);
			card.setChange(true);
			return true;
		}
		return false;
	}

	public boolean move(GameRoom room, TroopCard card, Area area) {
		if (area == null) {
			return false;
		}
		if (area.getLevel() <= 0) {
			return false;
		}
		if (area.troopIsFull()) {
			return false;
		}
		int oldAreaIndex = card.getAreaIndex();
		int oldMainIndex = card.getMainRowIndex();
		BattleRole self = room.getBattleRole(card.getPlayerId());
		Area oldArea = self.getArea(card.getAreaIndex());
		if (oldArea.equals(area)) {
			return false;
		}
		card.setOldArea(oldArea);
		removeEffectByReturn(room, card);
		removeCostEffect(room, card, false);
		removeEffect(room, card, self, false);
		oldArea.removeCard(card);
		area.addTroop(card);
		room.troopMoveSync(card, area, oldAreaIndex, oldMainIndex);
		room.cardDeath(card);
		room.checkDead(card);
		
		reloadEffect(room, card, card.getPlayerId(), area, self);
		room.areaSkillEffect(area);
		room.triggerEffect(TriggerManager.TROOP_CHANGE, self.getPlayerId(), 1);
		room.triggerEffect(TriggerManager.AREA_TROOP, self.getPlayerId(), card, 1);
		room.triggerEffect(TriggerManager.ADD_TROOP, self.getPlayerId(), card, 1);
		room.attackLimitTroopSync(self, oldArea, null);
		room.attackLimitTroopSync(self, area, null);
		return true;
	}

	public void reloadEffect(GameRoom room, CardBase card, int playerId, Area area, BattleRole fighter) {
		HashMap<Integer, SkillModel> skill = ConfigData.skillModels.get(card.getRealId());
		if (skill == null || skill.size() == 0) {
			return;
		}
		for (SkillModel model : skill.values()) {
			if (SkillManager.getInstance().isAutoTrigger(model)) {
				continue;
			}
			switch (model.Genius) {
			case AIR_TEMPLE:
			case FIRE_TEMPLE:
			case EARTH_TEMPLE:
			case WATER_TEMPLE:
			case PATHFINDER:
				SkillArg arg = new SkillArg(room, playerId, card, area, fighter, model, 1);
				triggerRegister(arg);
				break;
			}
		}
	}

	private ArrayList<TroopCard> stun(SkillArg arg) {

		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		SkillModel model = arg.getModel();
		int sendType = arg.getSendType();

		ArrayList<TroopCard> result = new ArrayList<>();
		int valueInt = expression(model.Value, arg);
		if (valueInt == 0) {
			return result;
		}
		switch (model.Target) {
		case SELF:
			TroopCard troop = (TroopCard) selfCard;
			stun(room, troop, valueInt);
			setTroopSyncResult(result, selfCard, troop, sendType);
			break;
			
		case ATTACKER:
			troop = (TroopCard) arg.getAttCard();
			if (TrapTriggerManager.getInstance().spellBlock(room, troop, selfCard)) {
				break;
			}
			if (isSpellBlock(room, arg.getTriggerOwner(), troop)) {
				break;
			}
			stun(room, troop, valueInt);
			setTroopSyncResult(result, selfCard, troop, sendType);
			break;
			
		case TRIGGER:
			troop = (TroopCard) arg.getTrigger();
			if (TrapTriggerManager.getInstance().spellBlock(room, troop, selfCard)) {
				break;
			}
			if (isSpellBlock(room, arg.getTriggerOwner(), troop)) {
				break;
			}
			stun(room, troop, valueInt);
			setTroopSyncResult(result, selfCard, troop, sendType);
			break;
			
		case C_TROOP:
		case C_TROOP_MINE:
		case C_TROOP_ENEMY:
		case C_TROOP_STUN:
		case C_TROOP_AREA_OPPO:
			ArrayList<Object> target = room.getTarget(arg);
			if (target.size() == 0) {
				return result;
			}
			TroopCard card = (TroopCard) target.get(0);
			if (TrapTriggerManager.getInstance().spellBlock(room, card, selfCard)) {
				break;
			}
			if (isSpellBlock(room, selfCard, card)) {
				break;
			}
			stun(room, card, valueInt);
			setTroopSyncResult(result, selfCard, card, sendType);
			break;
		case C_AREA:
		case C_AREA_MINE:
		case C_AREA_ENEMY:
			target = room.getTarget(arg);
			if (target.size() == 0) {
				return result;
			}
			Area area = (Area) target.get(0);
			if (TrapTriggerManager.getInstance().spellBlock(room, area, selfCard)) {
				break;
			}
			for (TroopCard tempTroop : area.getTroops()) {
				if (isSpellBlock(room, selfCard, tempTroop)) {
					continue;
				}
				stun(room, tempTroop, valueInt);
				setTroopSyncResult(result, selfCard, tempTroop, sendType);
			}
			break;
		case OPP_AREA_TROOP:
			ArrayList<TroopCard> oppAreaTroop = SkillTargetSelect.getInstance().getOppAreaTroop(room, selfCard);
			for (TroopCard t : oppAreaTroop) {
				if (isSpellBlock(room, selfCard, t)) {
					continue;
				}
				stun(room, t, valueInt);
				setTroopSyncResult(result, selfCard, t, sendType);
			}
			break;
		case ENEMY_ALL_TROOP:
			ArrayList<TroopCard> enemyTroops = room.getTroopsByPlayerId(room.getEnemyId(selfCard.getPlayerId()));
			if (enemyTroops.size() == 0) {
				return result;
			}
			for (TroopCard tempTroop : enemyTroops) {
				if (isSpellBlock(room, selfCard, tempTroop)) {
					continue;
				}
				stun(room, tempTroop, valueInt);
				setTroopSyncResult(result, selfCard, tempTroop, sendType);
			}
			break;
		case TROOP_NEARBY:
			target = room.getTarget(arg);
			if (target.size() == 0) {
				return result;
			}
			CardBase temp = (CardBase) target.get(0);
			BattleRole role = room.getBattleRole(temp.getPlayerId());
			area = role.getArea(temp.getAreaIndex());
			for (TroopCard tempTroop : area.getTroops()) {
				if (tempTroop.getUid() == temp.getUid()) {
					continue;
				}
				if (isSpellBlock(room, selfCard, tempTroop)) {
					continue;
				}
				stun(room, tempTroop, valueInt);
				setTroopSyncResult(result, selfCard, tempTroop, sendType);
			}
			break;
		case OPP_AREA_RANDOM_TROOP:
			BattleRole fighter = room.getBattleRole(room.getEnemyId(selfCard.getPlayerId()));
			int oppoAreaIndex = SkillManager.getInstance().getOppoAreaIndex(selfCard.getAreaIndex());
			area = fighter.getArea(oppoAreaIndex);
			ArrayList<TroopCard> troops = area.getTroops();
			if (troops.size() == 0) {
				break;
			}
			int random = Tools.random(0, troops.size() - 1);
			troop = troops.get(random);
			if (isSpellBlock(room, selfCard, troop)) {
				break;
			}
			stun(room, troop, valueInt);
			setTroopSyncResult(result, selfCard, troop, sendType);
			break;
		case ENEMY_RANDOM_TROOP:
			enemyTroops = room.getTroopsByPlayerId(room.getEnemyId(selfCard.getPlayerId()));
			if (enemyTroops.size() == 0) {
				return result;
			}
			random = Tools.random(0, enemyTroops.size() - 1);
			troop = enemyTroops.get(random);
			stun(room, troop, valueInt);
			setTroopSyncResult(result, selfCard, troop, sendType);
			break;
		}
		return result;
	}

	private ArrayList<TroopCard> stunRemove(SkillArg arg) {

		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		SkillModel model = arg.getModel();
		int sendType = arg.getSendType();
		
		ArrayList<TroopCard> result = new ArrayList<>();
		int valueInt = expression(model.Value);
		switch (model.Target) {
		case SELF:
			TroopCard troop = (TroopCard) selfCard;
			stunRemove(room, troop, valueInt);
			setTroopSyncResult(result, selfCard, troop, sendType);
			break;
		case C_TROOP:
		case C_TROOP_MINE:
		case C_TROOP_ENEMY:
			ArrayList<Object> target = room.getTarget(arg);
			if (target.size() == 0) {
				return result;
			}
			TroopCard card = (TroopCard) target.get(0);
			if (TrapTriggerManager.getInstance().spellBlock(room, card, selfCard)) {
				break;
			}
			if (isSpellBlock(room, selfCard, card)) {
				break;
			}
			stunRemove(room, card, valueInt);
			setTroopSyncResult(result, selfCard, card, sendType);
			break;
		case C_AREA:
		case C_AREA_MINE:
		case C_AREA_ENEMY:
			target = room.getTarget(arg);
			if (target.size() == 0) {
				return result;
			}
			Area area = (Area) target.get(0);
			if (TrapTriggerManager.getInstance().spellBlock(room, area, selfCard)) {
				break;
			}
			for (TroopCard tempTroop : area.getTroops()) {
				if (isSpellBlock(room, selfCard, tempTroop)) {
					continue;
				}
				stunRemove(room, tempTroop, valueInt);
				setTroopSyncResult(result, selfCard, tempTroop, sendType);
			}
			break;
		case OPP_AREA_TROOP:
			ArrayList<TroopCard> oppAreaTroop = SkillTargetSelect.getInstance().getOppAreaTroop(room, selfCard);
			for (TroopCard t : oppAreaTroop) {
				if (isSpellBlock(room, selfCard, t)) {
					continue;
				}
				stunRemove(room, t, valueInt);
				setTroopSyncResult(result, selfCard, t, sendType);
			}
			break;
		case MINE_ALL_TROOP:
			ArrayList<TroopCard> troops = room.getTroopsByPlayerId(selfCard.getPlayerId());
			if (troops.size() == 0) {
				return result;
			}
			for (TroopCard tempTroop : troops) {
				if (isSpellBlock(room, selfCard, tempTroop)) {
					continue;
				}
				stunRemove(room, tempTroop, valueInt);
				setTroopSyncResult(result, selfCard, tempTroop, sendType);
			}
			break;
		case TROOP_NEARBY:
			target = room.getTarget(arg);
			if (target.size() == 0) {
				return result;
			}
			CardBase temp = (CardBase) target.get(0);
			BattleRole role = room.getBattleRole(temp.getPlayerId());
			area = role.getArea(temp.getAreaIndex());
			for (TroopCard tempTroop : area.getTroops()) {
				if (tempTroop.getUid() == temp.getUid()) {
					continue;
				}
				if (isSpellBlock(room, selfCard, tempTroop)) {
					continue;
				}
				stunRemove(room, tempTroop, valueInt);
				setTroopSyncResult(result, selfCard, tempTroop, sendType);
			}
			break;
		case ATTACKER:
			troop = (TroopCard) arg.getAttCard();
			stunRemove(room, troop, valueInt);
			setTroopSyncResult(result, selfCard, troop, sendType);
			break;
		}
		return result;
	}

	private void stun(GameRoom room, TroopCard card, int valueInt) {
		BattleRole fighter = room.getBattleRole(card.getPlayerId());
		card.addStatusCount(TroopCard.STUN, valueInt);
		removeEffect(room, card, fighter, true);
		removeCostEffect(room, card, true);
		removeStunAmplify(room, card, fighter);
		room.stunSync(card);
		return;
	}

	public void stunRemove(GameRoom room, TroopCard card, int valueInt) {
		int playerId = card.getPlayerId();
		BattleRole fighter = room.getBattleRole(playerId);
		Area area = fighter.getArea(card.getAreaIndex());
		if (card.isStun()) {
			if (valueInt == 0) {
				card.setStatus(TroopCard.STUN, false);
			} else {
				card.addStatusCount(TroopCard.STUN, -valueInt);
				if (card.isStun()) {
					return;
				}
			}
			stunRemove(room, card, fighter, area);
		} else if (valueInt == -1) {
			stunRemove(room, card, fighter, area);
		}
		return;
	}

	public void stunRemove(GameRoom room, TroopCard card, BattleRole fighter, Area area) {
		int playerId = card.getPlayerId();
		revertStunAmplify(room, card, fighter);
		room.skillEffect(card, playerId, area, fighter, 1, false);
		room.triggerEffect(TriggerManager.TROOP_CHANGE, fighter.getPlayerId(), 1);
		room.stunSync(card);
	}
	
	private ArrayList<CardBase> returnHandCard(SkillArg arg, BattleRole role) {

		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		SkillModel model = arg.getModel();
		
		ArrayList<CardBase> result = new ArrayList<>();
		selfCard.setEffectCards(result);
		
		switch (model.Target) {
		case SELF:
			returnHandCard(arg, selfCard, result, role);
			break;
		case C_TROOP:
		case C_TROOP_MINE:
		case C_TROOP_ENEMY:
		case C_TROOP_COST_BELOW_1_ENEMY:
			ArrayList<Object> target = room.getTarget(arg);
			if (target.size() == 0) {
				return result;
			}
			TroopCard troop = (TroopCard) target.get(0);
			if (TrapTriggerManager.getInstance().spellBlock(room, troop, selfCard)) {
				break;
			}
			if (isSpellBlock(room, selfCard, troop)) {
				break;
			}
			returnHandCard(arg, troop, result, role);
			break;
		case C_TRAP:
		case C_TRAP_MINE:
		case C_TRAP_ENEMY:
		case C_ARTIFACT:
		case C_ARTIFACT_MINE:
		case C_ARTIFACT_ENEMY:
		case C_TRAP_OR_ARTI:
		case C_TRAP_OR_ARTI_MINE:
		case C_TRAP_OR_ARTI_ENEMY:
			target = room.getTarget(arg);
			if (target.size() == 0) {
				return result;
			}
			CardBase card = (CardBase) target.get(0);
			returnHandCard(arg, card, result, role);
			break;
		case C_AREA:
		case C_AREA_MINE:
		case C_AREA_ENEMY:
			target = room.getTarget(arg);
			if (target.size() == 0) {
				return result;
			}
			Area area = (Area) target.get(0);
			if (TrapTriggerManager.getInstance().spellBlock(room, area, selfCard)) {
				break;
			}
			for (TroopCard t : area.getTroops()) {
				if (isSpellBlock(room, selfCard, t)) {
					continue;
				}
				returnHandCard(arg, t, result, role);
			}
			for (CardBase c : area.getArtiTraps()) {
				returnHandCard(arg, c, result, role);
			}
			break;

		case OPP_AREA_TROOP:
			ArrayList<TroopCard> oppAreaTroop = SkillTargetSelect.getInstance().getOppAreaTroop(room, selfCard);
			for (TroopCard t : oppAreaTroop) {
				if (isSpellBlock(room, selfCard, t)) {
					continue;
				}
				returnHandCard(arg, t, result, role);
			}
			break;
			
		case OPP_AREA_TROOP_AND_ARTI:
			ArrayList<CardBase> troopAndArtis = SkillTargetSelect.getInstance().getOppAreaTroopAndArti(room, selfCard);
			for (CardBase c : troopAndArtis) {
				if (c.getType() == CardModel.TROOP) {
					TroopCard t = (TroopCard)c;
					if (isSpellBlock(room, selfCard, t)) {
						continue;
					}
				}
				returnHandCard(arg, c, result, role);
			}
			break;

		case ENEMY_ALL_TROOP:
			int enemyId = room.getEnemyId(selfCard.getPlayerId());
			ArrayList<TroopCard> troopsByPlayerId = room.getTroopsByPlayerId(enemyId);
			for (TroopCard t : troopsByPlayerId) {
				if (isSpellBlock(room, selfCard, t)) {
					continue;
				}
				returnHandCard(arg, t, result, role);
			}
			break;

		case MINE_ALL_TROOP:
			ArrayList<TroopCard> troops = room.getTroopsByPlayerId(selfCard.getPlayerId(), selfCard, model);
			for (TroopCard t : troops) {
				returnHandCard(arg, t, result, role);
			}
			break;
			
		case ENEMY_TROOP_ARTI_COST:
			int valueInt = expression(model.Value);
			enemyId = room.getEnemyId(selfCard.getPlayerId());
			BattleRole enemy = room.getBattleRole(enemyId);
			troopsByPlayerId = room.getTroopsByPlayerId(enemyId);
			for (TroopCard t : troopsByPlayerId) {
				if (t.getCost(enemy) <= valueInt) {
					if (isSpellBlock(room, selfCard, t)) {
						continue;
					}
					returnHandCard(arg, t, result, role);
				}
			}
			for (Area tempArea : enemy.getAreas()) {
				for (ArtifactCard artifact : tempArea.getArtifact()) {
					if (artifact.getCost(enemy) <= valueInt) {
						returnHandCard(arg, artifact, result, role);
					}
				}
			}
			break;
		case ENEMY_RANDOM_TROOP:
			ArrayList<TroopCard> enemyTroops = room.getTroopsByPlayerId(room.getEnemyId(selfCard.getPlayerId()));
			if (enemyTroops.size() == 0) {
				return result;
			}
			int random = Tools.random(0, enemyTroops.size() - 1);
			troop = enemyTroops.get(random);
			returnHandCard(arg, troop, result, role);
			break;

		case OPP_AREA_ATK_LESS_1:
			if (selfCard.getType() != CardModel.TROOP) {
				return result;
			}
			troop = (TroopCard) selfCard;
			oppAreaTroop = SkillTargetSelect.getInstance().getOppAreaTroop(room, selfCard);
			for (TroopCard t : oppAreaTroop) {
				if (t.getRealAttack() > 1) {
					continue;
				}
				if (isSpellBlock(room, selfCard, t)) {
					continue;
				}
				returnHandCard(arg, t, result, role);
			}
			break;
		}
		return result;
	}

	private void returnHandCard(SkillArg arg, CardBase card, ArrayList<CardBase> result, BattleRole self) {
		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		BattleRole fighter = room.getBattleRole(card.getPlayerId());
		if (self == null) {
			self = fighter;
		}
		Area area = card.getArea();
		if (area != null) {
			removeEffect(room, card, fighter, false);
			room.getTriggerManager().delTriggerEvent(card);
			room.getTriggerManager().addTriggerEvent(card);
			if (card.getType() == CardModel.TROOP) {
				TroopCard troop = (TroopCard) card;
				removeEffectByReturn(room, troop);
				removeCostEffect(room, card, false);
				room.triggerEffect(TriggerManager.TROOP_CHANGE, fighter.getPlayerId(), 1);
				room.attackLimitTroopSync(fighter, area, troop);
				troop.setStatus(UNDEAD, false);
			}

			room.addHandCard(self, card);
			result.add(card);
			
			card.setOldOwnerId(card.getPlayerId());
			card.setPlayerId(self.getPlayerId());
			card.setDead(false);
			if (selfCard.getStatus().get(RETURN) != null) {
				Integer value = selfCard.getStatus().get(RETURN);
				card.addCost(value);
			}
			area.removeCard(card);
		} else {
			room.getTriggerManager().addTriggerEvent(card);
			room.addHandCard(self, card);
			
			card.setOldOwnerId(card.getPlayerId());
			card.setPlayerId(self.getPlayerId());
			if (selfCard.getStatus().get(RETURN) != null) {
				Integer value = selfCard.getStatus().get(RETURN);
				card.addCost(value);
			}
			
			ArrayList<CardBase> cards = new ArrayList<>();
			cards.add(card);
			card.setDead(false);
			room.drawCardSync(self, cards);
		}
	}

	private void discards(SkillArg arg) {
		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		SkillModel model = arg.getModel();
		
		ArrayList<CardBase> result = new ArrayList<>();
		switch (model.Target) {
		case ENEMY:
			if (arg.getTriggerOwner() != null && arg.getTriggerOwner().getType() == CardModel.TRAP) {
				selfCard = arg.getTriggerOwner();
			}
			BattleRole enemy = room.getBattleRole(room.getEnemyId(selfCard.getPlayerId()));
			discards(arg, enemy, result);
			room.discardSync(enemy.getPlayerId(), result);
			if (result.size() > 0) {
				selfCard.setStatus(EFFECT_SUCC, true);
			}
			break;
		case C_HAND_CARD_MINE:
		case C_HAND_CARD_TROOP:
			ArrayList<Object> target = room.getTarget(arg);
			if (target.size() == 0) {
				return;
			}
			CardBase card = (CardBase) target.get(0);
			BattleRole role = room.getBattleRole(card.getPlayerId());
			result.add(card);
			role.getDiscards().add(card);
			room.removeHandCard(role, card);
			room.discardSync(role.getPlayerId(), result);
			if (result.size() > 0) {
				selfCard.setStatus(EFFECT_SUCC, true);
			}
			selfCard.addStatusCount(DISCARD_COST, card.getCost(role));
			return;
		case C_HAND_CARD_ENEMY:
			target = room.getTarget(arg);
			if (target.size() == 0) {
				return;
			}
			card = (CardBase) target.get(0);
			enemy = room.getBattleRole(card.getPlayerId());
			result.add(card);
			enemy.getDiscards().add(card);
			room.removeHandCard(enemy, card);
			room.discardSync(enemy.getPlayerId(), result);
			if (result.size() > 0) {
				selfCard.setStatus(EFFECT_SUCC, true);
			}
			selfCard.addStatusCount(DISCARD_COST, card.getCost(enemy));
			return;
		case EFFECT_CARD:
			ArrayList<CardBase> drawCards = selfCard.getEffectCards();
			if (drawCards == null || drawCards.size() == 0) {
				return;
			}
			role = room.getBattleRole(selfCard.getPlayerId());
			ArrayList<CardBase> handCards = role.getHandCards();
			for (CardBase tempCard : drawCards) {
				if (handCards.indexOf(tempCard) == -1) {
					continue;
				}
				result.add(tempCard);
				role.getDiscards().add(tempCard);
				room.removeHandCard(role, tempCard);
			}
			room.discardSync(role.getPlayerId(), result);
			if (result.size() > 0) {
				selfCard.setStatus(EFFECT_SUCC, true);
			}
			return;
		}
		return;
	}

	private void discards(SkillArg arg, BattleRole fighter, ArrayList<CardBase> result) {
		GameRoom room = arg.getRoom();
		SkillModel model = arg.getModel();
		switch (model.Value) {
		case ALL:
			result.addAll(fighter.getHandCards());
			fighter.getDiscards().addAll(fighter.getHandCards());
			room.removeHandCard(fighter, null);
			break;
		case COST_DOWN_4:
			ArrayList<CardBase> list = new ArrayList<>();
			list.addAll(fighter.getHandCards());
			for (CardBase card : list) {
				if (card.getCost(fighter) <= 4) {
					result.add(card);
					fighter.getDiscards().add(card);
					room.removeHandCard(fighter, card);
				}
			}
			break;
		default:
			list = new ArrayList<>();
			list.addAll(fighter.getHandCards());
			Collections.shuffle(list);
			int valueInt = expression(model.Value);
			for (int i = 0; i < valueInt; i++) {
				if (list.size() == 0) {
					break;
				}
				CardBase card = list.remove(0);
				result.add(card);
				fighter.getDiscards().add(card);
				room.removeHandCard(fighter, card);
				room.triggerEffect(TriggerManager.DISCARDS, card.getPlayerId(), card, 1);
				logger.info("玩家：{}，房间Id：{}，弃牌，卡牌Id:{}，卡牌UId:{}。", fighter.getPlayerId(), room.getRoomId(), card.getRealId(),
						card.getUid());
			}
			break;
		}
	}

	private void reduceHpOppAreaTroop(SkillArg arg) {
		SkillModel model = arg.getModel();
		GameRoom room = arg.getRoom();

		switch (model.Target) {
		case C_TROOP:
		case C_TROOP_MINE:
		case C_TROOP_ENEMY:
		case C_TROOP_TEMPLE_FIRE_MINE:
			ArrayList<Object> target = room.getTarget(arg);
			if (target.size() == 0) {
				return;
			}
			TroopCard troop = (TroopCard) target.get(0);
			BattleRole fighter = room.getBattleRole(room.getEnemyId(troop.getPlayerId()));
			int oppoAreaIndex = SkillManager.getInstance().getOppoAreaIndex(troop.getAreaIndex());
			Area area = fighter.getArea(oppoAreaIndex);
			reduceAreaTroop(arg, area, troop);
			break;
		}
	}

	private void reduceHpRandom(SkillArg arg) {
		GameRoom room = arg.getRoom();
		SkillModel model = arg.getModel();
		
		switch (model.Target) {
		case C_AREA:
		case C_AREA_MINE:
		case C_AREA_ENEMY:
			ArrayList<Object> target = room.getTarget(arg);
			if (target.size() == 0) {
				return;
			}
			Area area = (Area) target.get(0);
			reduceRandomEnemyArea(arg, area);
			break;

		case ENEMY_ALL_TROOP:
			randomEnemyTroop(arg);
			break;

		case ENEMY:
			randomEnemy(arg);
			break;
		}
	}

	private void reduceHp(SkillArg arg) {

		SkillModel model = arg.getModel();
		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();

		switch (model.Target) {
		case SELF:
			TroopCard troop = (TroopCard) selfCard;
			arg.setFighter(room.getBattleRole(troop.getPlayerId()));
			reduceHp(arg, troop, model.Value);
			break;
			
		case TRIGGER:
			troop = (TroopCard) arg.getTrigger();
			arg.setFighter(room.getBattleRole(troop.getPlayerId()));
			reduceHp(arg, troop, model.Value);
			break;

		case ATTACKER:
			troop = (TroopCard) arg.getAttCard();
			arg.setFighter(room.getBattleRole(troop.getPlayerId()));
			reduceHp(arg, troop, model.Value);
			break;
			
		case C_TROOP:
		case C_TROOP_MINE:
		case C_TROOP_ENEMY:
		case C_TROOP_AREA_OPPO:
			ArrayList<Object> target = room.getTarget(arg);
			if (target.size() == 0) {
				return;
			}
			troop = (TroopCard) target.get(0);
			arg.setFighter(room.getBattleRole(troop.getPlayerId()));
			reduceHp(arg, troop, model.Value);
			break;

		case OPP_AREA_TROOP:
			if (arg.getTriggerOwner() != null && arg.getTriggerOwner().getType() == CardModel.TRAP) {
				selfCard = arg.getTriggerOwner();
			}
			BattleRole fighter = room.getBattleRole(room.getEnemyId(selfCard.getPlayerId()));
			int oppoAreaIndex = SkillManager.getInstance().getOppoAreaIndex(selfCard.getAreaIndex());
			Area area = fighter.getArea(oppoAreaIndex);
			arg.setFighter(fighter);
			reduceAreaTroop(arg, area, null);
			break;

		case C_AREA:
		case C_AREA_MINE:
		case C_AREA_ENEMY:
			target = room.getTarget(arg);
			if (target.size() == 0) {
				return;
			}
			area = (Area) target.get(0);
			reduceAreaTroop(arg, area, null);
			break;

		case SELF_HERO:
			CardBase triggerOwner = arg.getTriggerOwner();
			BattleRole mine = room.getBattleRole(triggerOwner.getPlayerId());
			reduceHpByHero(arg, mine);
			break;

		case ENEMY_HERO:
			triggerOwner = arg.getTriggerOwner();
			BattleRole enemy = room.getBattleRole(room.getEnemyId(triggerOwner.getPlayerId()));
			reduceHpByHero(arg, enemy);
			break;

		case C_HERO_OR_TROOP:
		case C_HERO_OR_TROOP_MINE:
		case C_HERO_OR_TROOP_ENEMY:
			reduceHeroOrTroop(arg);
			break;

		case ENEMY_ALL_TROOP:
			ArrayList<TroopCard> enemyTroops = room.getTroopsByPlayerId(room.getEnemyId(selfCard.getPlayerId()));
			reduceTroops(arg, enemyTroops);
			break;

		case ENEMY_ALL_STUN:
			enemyTroops = room.getTroopsByPlayerId(room.getEnemyId(selfCard.getPlayerId()));
			if (enemyTroops.size() == 0) {
				return;
			}
			int valueInt = getAmplify(arg, arg.getModel().Value, null);
			for (TroopCard tempTroop : enemyTroops) {
				if (tempTroop.isStun()) {
					reduceHp(arg, tempTroop, valueInt, true);
				}
			}
			break;

		case TROOP_NEARBY:
			target = room.getTarget(arg);
			if (target.size() == 0) {
				return;
			}
			troop = (TroopCard) target.get(0);
			BattleRole role = room.getBattleRole(troop.getPlayerId());
			area = troop.getArea() == null ? troop.getOldArea() : troop.getArea();
			reduceAreaTroop(arg, area, troop);
			break;

		case ENEMY_RANDOM_TROOP:
			reduceEnemyRandomTroop(arg);
			break;

		case ENEMY_RANDOM:
			reduceEnemyRandom(arg);
			break;

		case TARGET_OPP_AREA_RANDOM_TROOP:
			target = room.getTarget(arg);
			if (target.size() == 0) {
				return;
			}
			CardBase card = (CardBase) target.get(0);

			fighter = room.getBattleRole(room.getEnemyId(card.getPlayerId()));
			oppoAreaIndex = SkillManager.getInstance().getOppoAreaIndex(card.getAreaIndex());
			area = fighter.getArea(oppoAreaIndex);
			arg.setArea(area);
			arg.setFighter(fighter);
			reduceAreaRandomTroop(arg, card);
			break;

		case OPP_AREA_RANDOM_TROOP:
			if (arg.getTriggerOwner() != null && arg.getTriggerOwner().getType() == CardModel.TRAP) {
				selfCard = arg.getTriggerOwner();
			}
			fighter = room.getBattleRole(room.getEnemyId(selfCard.getPlayerId()));
			oppoAreaIndex = SkillManager.getInstance().getOppoAreaIndex(selfCard.getAreaIndex());
			area = fighter.getArea(oppoAreaIndex);
			arg.setArea(area);
			arg.setFighter(fighter);
			reduceAreaRandomTroop(arg, null);
			break;

		case FIRE_TEMPLE_OPP:
			role = room.getBattleRole(selfCard.getPlayerId());
			fighter = room.getBattleRole(room.getEnemyId(selfCard.getPlayerId()));
			for (Area tempArea : role.getAreas()) {
				if (tempArea.getRune() != SkillManager.FIRE || tempArea.getLevel() != Area.MAX_LEVEL) {
					continue;
				}
				Area enemyArea = fighter.getArea(getOppoAreaIndex(tempArea.getIndex()));
				arg.setFighter(fighter);
				reduceAreaTroop(arg, enemyArea, null);
			}
			break;

		case ALL:
			int playerId = selfCard.getPlayerId();
			int enemyId = room.getEnemyId(selfCard.getPlayerId());
			ArrayList<TroopCard> troops = room.getTroopsByPlayerId(playerId);
			troops.addAll(room.getTroopsByPlayerId(enemyId));
			reduceTroops(arg, troops);

			enemy = room.getBattleRole(enemyId);
			arg.setFighter(enemy);
			reduceHpByHero(arg, enemy);

			mine = room.getBattleRole(playerId);
			reduceHpByHero(arg, mine);

			break;
		}
		return;
	}

	/**
	 * 对所有敌方部队造成伤害
	 * 
	 * @param room
	 * @param model
	 * @param self
	 * @param selfCard
	 */
	private void reduceTroops(SkillArg arg, ArrayList<TroopCard> troops) {
		if (troops.size() == 0) {
			return;
		}
		int valueInt = getAmplify(arg, arg.getModel().Value, null);
		for (TroopCard troop : troops) {
			reduceHp(arg, troop, valueInt, true);
		}
	}

	/**
	 * 对一个敌方随机部队施放伤害
	 * 
	 * @param room
	 * @param model
	 * @param self
	 * @param selfCard
	 */
	private void reduceEnemyRandomTroop(SkillArg arg) {
		GameRoom room = arg.getRoom();
		SkillModel model = arg.getModel();
		BattleRole fighter = arg.getFighter();

		ArrayList<TroopCard> enemyTroops = room.getTroopsByPlayerId(room.getEnemyId(fighter.getPlayerId()));
		if (enemyTroops.size() == 0) {
			return;
		}
		int random = Tools.random(0, enemyTroops.size() - 1);
		TroopCard troop = enemyTroops.get(random);
		reduceHp(arg, troop, model.Value);
	}

	/**
	 * 对区域一个敌方随机部队施放伤害
	 * 
	 * @param room
	 * @param model
	 * @param self
	 * @param selfCard
	 */
	private void reduceAreaRandomTroop(SkillArg arg, CardBase target) {
		Area area = arg.getArea();
		SkillModel model = arg.getModel();
		if (area == null) {
			return;
		}
		ArrayList<TroopCard> troops = area.getTroops();
		if (troops.size() == 0) {
			return;
		}
		int random = Tools.random(0, troops.size() - 1);
		TroopCard troop = troops.get(random);
		int valueInt = getAmplify(arg, model.Value, target);
		reduceHp(arg, troop, valueInt, true);
	}

	/**
	 * 对一个敌方随机目标施放伤害
	 * 
	 * @param room
	 * @param model
	 * @param self
	 * @param selfCard
	 */
	private void reduceEnemyRandom(SkillArg arg) {
		GameRoom room = arg.getRoom();
		SkillModel model = arg.getModel();
		BattleRole fighter = arg.getFighter();

		int enemyId = room.getEnemyId(fighter.getPlayerId());
		BattleRole enemy = room.getBattleRole(enemyId);
		ArrayList<TroopCard> enemyTroops = room.getTroopsByPlayerId(enemyId);
		IBattleObject obj = room.random(enemyTroops, enemy);
		if (obj == null) {
			logger.info("敌方玩家：{}，房间Id：{}。攻击失败，没有可攻击的对象。", enemyId, room.getRoomId());
			return;
		}
		if (obj instanceof TroopCard) {
			TroopCard troop = (TroopCard) obj;
			reduceHp(arg, troop, model.Value);
		} else if (obj instanceof BattleRole) {
			BattleRole role = (BattleRole) obj;

			reduceHpByHero(arg, role);
		}
	}

	/**
	 * 对一个指定部队或英雄造成伤害
	 * 
	 * @param room
	 * @param model
	 * @param self
	 * @param selfCard
	 */
	private void reduceHeroOrTroop(SkillArg arg) {
		GameRoom room = arg.getRoom();
		SkillModel model = arg.getModel();

		ArrayList<Object> target = room.getTarget(arg);
		if (target.size() == 0) {
			return;
		}
		Object obj = target.get(0);
		if (obj instanceof TroopCard) {
			TroopCard troop = (TroopCard) obj;
			reduceHp(arg, troop, model.Value);
		} else if (obj instanceof BattleRole) {
			BattleRole role = (BattleRole) obj;
			reduceHpByHero(arg, role);
		}
	}

	/**
	 * 对指定区域部队造成伤害
	 * 
	 * @param room
	 * @param selfCard
	 * @param model
	 * @param self
	 * @param area
	 * @param target
	 */
	private void reduceAreaTroop(SkillArg arg, Area area, TroopCard target) {
		ArrayList<TroopCard> areaTroops = new ArrayList<>();
		areaTroops.addAll(area.getTroops());
		if (areaTroops.size() == 0) {
			return;
		}

		int valueInt = getAmplify(arg, arg.getModel().Value, target);
		for (TroopCard troop : areaTroops) {
			if (target != null && troop.getUid() == target.getUid()) {
				continue;
			}
			reduceHp(arg, troop, valueInt, true);
		}
	}

	/**
	 * 对区域内随机敌方部队造成伤害
	 * 
	 * @param room
	 * @param selfCard
	 * @param model
	 * @param self
	 * @param area
	 */
	private void reduceRandomEnemyArea(SkillArg arg, Area area) {
		ArrayList<TroopCard> enemyTroops = area.getTroops();
		if (enemyTroops.size() == 0) {
			return;
		}
		int valueInt = getAmplify(arg, arg.getModel().Value, null);
		reduceRandom(arg, enemyTroops, null, valueInt);
	}

	private void reduceRandom(SkillArg arg, ArrayList<TroopCard> troops, BattleRole fighter, int value) {
		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();

		HashMap<Integer, Integer> tempHpMap = new HashMap<>();
		HashMap<Integer, Object> result = new HashMap<>();
		for (int i = 0; i < value; i++) {
			IBattleObject obj = room.random(troops, fighter);
			if (obj == null) {
				logger.info("敌方玩家：{}，房间Id：{}。攻击失败，没有可攻击的对象。", fighter.getPlayerId(), room.getRoomId());
				return;
			}
			if (obj instanceof TroopCard) {
				TroopCard troop = (TroopCard) obj;
				if (tempHpMap.get(troop.getUid()) == null) {
					tempHpMap.put(troop.getUid(), troop.getHp());
					result.put(troop.getUid(), troop);
				}
				reduceHp(arg, troop, 1, true);
				if (troop.getHp() <= 0) {
					troops.remove(troop);
				}
				if (troops.size() == 0 && fighter == null) {
					break;
				}
			} else if (obj instanceof BattleRole) {
				BattleRole role = (BattleRole) obj;
				if (tempHpMap.get(role.getUid()) == null) {
					tempHpMap.put(role.getUid(), role.getHp());
					result.put(role.getUid(), role);
				}
				if (role.getStatus(TroopCard.DOUBLE_DAMAGE)) {
					reduceHeroHp(room, selfCard, role, 2);
				} else {
					reduceHeroHp(room, selfCard, role, 1);
				}
				room.settlement(role.getPlayerId());
			}
		}
	}

	/**
	 * 对所有敌方部队随机造成伤害
	 * 
	 * @param room
	 * @param model
	 * @param self
	 * @return
	 */
	private ArrayList<TroopCard> randomEnemyTroop(SkillArg arg) {
		GameRoom room = arg.getRoom();
		ArrayList<TroopCard> enemyTroops = room.getTroopsByPlayerId(room.getEnemyId(arg.getFighter().getPlayerId()));
		if (enemyTroops.size() == 0) {
			return enemyTroops;
		}
		int valueInt = getAmplify(arg, arg.getModel().Value, null);
		reduceRandom(arg, enemyTroops, null, valueInt);
		return enemyTroops;
	}

	/**
	 * 对所有敌方随机造成伤害
	 * 
	 * @param room
	 * @param model
	 * @param self
	 * @return
	 */
	private ArrayList<TroopCard> randomEnemy(SkillArg arg) {
		GameRoom room = arg.getRoom();
		int enemyId = room.getEnemyId(arg.getFighter().getPlayerId());
		BattleRole enemy = room.getBattleRole(enemyId);
		ArrayList<TroopCard> enemyTroops = room.getTroopsByPlayerId(enemyId);
		int valueInt = getAmplify(arg, arg.getModel().Value, null);
		reduceRandom(arg, enemyTroops, enemy, valueInt);
		return enemyTroops;
	}

	private ArrayList<TroopCard> destroy(SkillArg arg) {

		SkillModel model = arg.getModel();
		CardBase selfCard = arg.getSelfCard();
		GameRoom room = arg.getRoom();
		selfCard.setStatus(EFFECT_SUCC, false);
		
		ArrayList<TroopCard> result = new ArrayList<>();
		switch (model.Target) {
		case C_AREA:
		case C_AREA_MINE:
		case C_AREA_ENEMY:
		case C_TEMPLE_MINE:
			ArrayList<Object> target = room.getTarget(arg);
			if (target.size() == 0) {
				return result;
			}
			Area targetArea = (Area) target.get(0);
			if (targetArea.getLevel() == 0) {
				return result;
			}
			targetArea.setLevel(-1);
			BattleRole role = room.getBattleRole(targetArea.getPlayerId());
			role.setAreaCount();
			room.destoryArea(role.getPlayerId(), targetArea);
			room.areaLvUpChangeDraw(role);
			break;
			
		}
		return result;
	}
	
	private ArrayList<TroopCard> killAnywhere(SkillArg arg) {

		SkillModel model = arg.getModel();
		CardBase selfCard = arg.getSelfCard();
		GameRoom room = arg.getRoom();
		selfCard.setStatus(EFFECT_SUCC, false);
		
		ArrayList<TroopCard> result = new ArrayList<>();
		switch (model.Target) {
		case C_TROOP_ENEMY:
			ArrayList<Object> target = room.getTarget(arg);
			if (target.size() == 0) {
				return result;
			}
			TroopCard card = (TroopCard) target.get(0);
			arg.setFighter(room.getBattleRole(card.getPlayerId()));
			killAllAnywhere(arg, card.getId());
			break;
		}
		return result;
	}
	
	private ArrayList<TroopCard> kill(SkillArg arg) {

		SkillModel model = arg.getModel();
		CardBase selfCard = arg.getSelfCard();
		GameRoom room = arg.getRoom();
		selfCard.setStatus(EFFECT_SUCC, false);
		
		ArrayList<TroopCard> result = new ArrayList<>();
		switch (model.Target) {
		case C_TROOP:
		case C_TROOP_MINE:
		case C_TROOP_ENEMY:
		case C_TROOP_AREA_SELF:
		case C_TROOP_AREA_OPPO_COST1:
		case C_TROOP_COST_ABOVE_7_ENEMY:
		case C_TROOP_ATK_BELOW_3_ENEMY:
			ArrayList<Object> target = room.getTarget(arg);
			if (target.size() == 0) {
				return result;
			}
			TroopCard card = (TroopCard) target.get(0);
			kill(arg, card);
			break;

		case C_TROOP_STUN:
		case C_TROOP_STUN_ENEMY:
			target = room.getTarget(arg);
			if (target.size() == 0) {
				return result;
			}
			card = (TroopCard) target.get(0);
			if (!card.isStun()) {
				return result;
			}
			kill(arg, selfCard);
			break;

		case C_TRAP:
		case C_TRAP_MINE:
		case C_TRAP_ENEMY:
		case C_ARTIFACT:
		case C_ARTIFACT_MINE:
		case C_ARTIFACT_ENEMY:
		case C_TRAP_OR_ARTI:
		case C_TRAP_OR_ARTI_MINE:
		case C_TRAP_OR_ARTI_ENEMY:
		case C_TRAP_OR_ARTI_COST_BELOW_2_ENEMY:
		case C_ARTIFACT_COST_BELOW_4_ENEMY:
			target = room.getTarget(arg);
			if (target.size() == 0) {
				return result;
			}
			CardBase cardBase = (CardBase) target.get(0);
			kill(arg, cardBase);
			break;

		case RANDOM_ARTI:
			BattleRole enemy = room.getBattleRole(room.getEnemyId(selfCard.getPlayerId()));
			ArrayList<CardBase> list = new ArrayList<>();
			for (Area area : enemy.getAreas()) {
				list.addAll(area.getArtifact());
			}
			BattleRole fighter = room.getBattleRole(selfCard.getPlayerId());
			for (Area area : fighter.getAreas()) {
				list.addAll(area.getArtifact());
			}
			if (list.size() > 0) {
				int random = Tools.random(0, list.size() - 1);
				CardBase c = list.get(random);
				kill(arg, c);
			}
			break;

		case ENEMY_RANDOM_ARTI:
			enemy = room.getBattleRole(room.getEnemyId(selfCard.getPlayerId()));
			list = new ArrayList<>();
			for (Area area : enemy.getAreas()) {
				list.addAll(area.getArtifact());
			}
			if (list.size() > 0) {
				int random = Tools.random(0, list.size() - 1);
				CardBase c = list.get(random);
				kill(arg, c);
			}
			break;

		case ENEMY_RANDOM_ARTI_AND_TRAP:
			enemy = room.getBattleRole(room.getEnemyId(selfCard.getPlayerId()));
			list = new ArrayList<>();
			for (Area area : enemy.getAreas()) {
				list.addAll(area.getArtiTraps());
			}
			if (list.size() > 0) {
				int random = Tools.random(0, list.size() - 1);
				CardBase c = list.get(random);
				kill(arg, c);
			}
			break;
			
		case TROOP_ATTACK:
			ArrayList<TroopCard> troops = room.getTroopsByPlayerId(selfCard.getPlayerId());
			troops.addAll(room.getTroopsByPlayerId(room.getEnemyId(selfCard.getPlayerId())));
			killTroopLimitAttack(arg, troops);
			break;

		case ENEMY_TROOP_ATTACK:
			troops = room.getTroopsByPlayerId(room.getEnemyId(selfCard.getPlayerId()));
			killTroopLimitAttack(arg, troops);
			break;

		case ENEMY_RANDOM_HALF:
			killEnemyRandomHalf(arg);
			break;
			
		case SELF:
			kill(arg, selfCard);
			break;

		case TRIGGER:
			kill(arg, arg.getTrigger());
			break;
			
		case ATTACKER:
			kill(arg, arg.getAttCard());
			break;
			
		case C_AREA:
		case C_AREA_MINE:
		case C_AREA_ENEMY:
		case C_TEMPLE_MINE:
			target = room.getTarget(arg);
			if (target.size() == 0) {
				return result;
			}
			Area targetArea = (Area) target.get(0);
			killArea(arg, targetArea, model.Value);
			break;
			
		case TARGET_OPP_AREA:
			target = room.getTarget(arg);
			if (target.size() == 0) {
				return result;
			}
			targetArea = (Area) target.get(0);
			int oppoAreaIndex = getOppoAreaIndex(targetArea.getIndex());
			int enemyId = room.getEnemyId(targetArea.getPlayerId());
			enemy = room.getBattleRole(enemyId);
			targetArea = enemy.getArea(oppoAreaIndex);
			killArea(arg, targetArea, model.Value);
			break;
			
		case ALL:
			killAll(arg);
			break;

		case ENEMY_RANDOM_TROOP:
			ArrayList<TroopCard> enemyTroops = room.getTroopsByPlayerId(room.getEnemyId(selfCard.getPlayerId()));
			if (enemyTroops.size() == 0) {
				return result;
			}
			int random = Tools.random(0, enemyTroops.size() - 1);
			TroopCard troop = enemyTroops.get(random);
			kill(arg, troop);
			break;

		case ENEMY_HERO:
			enemy = room.getBattleRole(room.getEnemyId(selfCard.getPlayerId()));
			int hp = enemy.getHp();
			enemy.setHp(0);
			room.reduceHpSync(enemy.getPlayerId(), selfCard.getUid(), enemy.getUid(), hp);
			room.settlement(enemy.getPlayerId());
			break;

		case OPP_AREA_ARTI_AND_TRAP:
			ArrayList<CardBase> cards = SkillTargetSelect.getInstance().getOppAreaArtiAndTrap(room, selfCard);
			for (CardBase c : cards) {
				kill(arg, c);
			}
			break;

		case OPP_AREA_TRAP:
			cards = SkillTargetSelect.getInstance().getOppAreaTrap(room, selfCard);
			for (CardBase c : cards) {
				kill(arg, c);
			}
			break;
			
		case OPP_AREA_COST_LESS_1:
			troops = SkillTargetSelect.getInstance().getOppAreaTroop(room, selfCard);
			enemyId = room.getEnemyId(selfCard.getPlayerId());
			enemy = room.getBattleRole(enemyId);
			for (TroopCard tempTroop : troops) {
				if (tempTroop.getCost(enemy) <= 1) {
					kill(arg, tempTroop);
				}
			}
			break;
			
		case ENEMY_ALL_TRAP:
			enemyId = room.getEnemyId(selfCard.getPlayerId());
			enemy = room.getBattleRole(enemyId);
			for (Area area : enemy.getAreas()) {
				for (TrapCard trap : area.getTrap()) {
					kill(arg, trap);
				}
			}
			break;
			
		case ENEMY_ALL_ARTI_AND_TRAP:
			enemyId = room.getEnemyId(selfCard.getPlayerId());
			enemy = room.getBattleRole(enemyId);
			for (Area area : enemy.getAreas()) {
				for (CardBase c : area.getArtiTraps()) {
					kill(arg, c);
				}
			}
			break;
		case ALL_TROOP:
			killAllTroop(arg);
			break;
		case SELF_AREA_RANDOM_1001:
			Area area = arg.getArea();
			troops = area.getTroops();
			if (troops.size() == 0) {
				return result;
			}
			Collections.shuffle(troops);
			for (TroopCard tempTroop : troops) {
				if (tempTroop.getId().equals("1001")) {
					kill(arg, tempTroop);
					return result;
				}
			}
			break;
		case MINE_ALL_TROOP_1001:
			troops = room.getTroopsByPlayerId(selfCard.getPlayerId(), selfCard, model);
			for (TroopCard tempTroop : troops) {
				if (tempTroop.getId().equals("1001")) {
					kill(arg, tempTroop);
				}
			}
			break;
		case MINE_DECK_COST_4:
			BattleRole role = room.getBattleRole(selfCard.getPlayerId());
			ArrayList<CardBase> decks = new ArrayList<>();
			decks.addAll(role.getDecks());
			for (CardBase tempCard : decks) {
				if (tempCard.getCost(role) <= 4) {
					role.getDecks().remove(tempCard);
				}
			}
			room.deckCardNumberSync(role);
			break;
		case EFFECT_CARD:
			ArrayList<CardBase> effectCards = selfCard.getEffectCards();
			if (effectCards == null || effectCards.size() == 0) {
				return result;
			}
			for (CardBase temp : effectCards) {
				kill(arg, temp);
			}
			break;
		}
		return result;
	}
	
	private void killArea(SkillArg arg, Area area, String type) {
		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		if (Tools.isEmptyString(type) || type.indexOf("Troop") != -1) {
			for (TroopCard troop : area.getTroops()) {
				if (isSpellBlock(room, selfCard, troop)) {
					continue;
				}
				kill(arg, troop);
			}
		}
		if (Tools.isEmptyString(type) || type.indexOf("Trap") != -1) {
			for (CardBase card : area.getTrap()) {
				kill(arg, card);
			}
		}
		if (Tools.isEmptyString(type) || type.indexOf("Arti") != -1) {
			for (CardBase card : area.getArtifact()) {
				kill(arg, card);
			}
		}
	}
	
	private void killAll(SkillArg arg) {
		GameRoom room = arg.getRoom();
		for (BattleRole role : room.getBattleRoles().values()) {
			for (Area area : role.getAreas()) {
				for (TroopCard troop : area.getTroops()) {
					kill(arg, troop);
				}
				for (CardBase card : area.getArtiTraps()) {
					kill(arg, card);
				}
			}
		}
	}

	private void killAllTroop(SkillArg arg) {
		GameRoom room = arg.getRoom();
		for (BattleRole role : room.getBattleRoles().values()) {
			ArrayList<TroopCard> troops = room.getTroopsByPlayerId(role.getPlayerId());
			for (TroopCard troop : troops) {
				kill(arg, troop);
			}
		}
	}

	private void killAllAnywhere(SkillArg arg, String cardId) {
		GameRoom room = arg.getRoom();
		BattleRole fighter = arg.getFighter();
		for (Area area : fighter.getAreas()) {
			for (TroopCard troop : area.getTroops()) {
				if (troop.getId().equals(cardId)) {
					kill(arg, troop);
				}
			}
		}
		
		ArrayList<CardBase> discards = new ArrayList<>();
		ArrayList<CardBase> handCards = new ArrayList<>();
		handCards.addAll(fighter.getHandCards());
		for (CardBase card : handCards) {
			if (card.getId().equals(cardId)) {
				room.removeHandCard(fighter, card);
				discards.add(card);
			}
		}
		room.discardSync(fighter.getPlayerId(), discards);
		
		ArrayList<CardBase> decks = new ArrayList<>();
		decks.addAll(fighter.getDecks());
		for (CardBase card : decks) {
			if (card.getId().equals(cardId)) {
				fighter.getDecks().remove(card);
			}
		}
		room.deckCardNumberSync(fighter);
	}

	private void kill(SkillArg arg, CardBase card) {
		if (card.isDead()) {
			return;
		}
		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		switch (card.getType()) {
		case CardModel.ARTIFACT:
		case CardModel.TRAP:
			room.cardKill(card);
			room.destoryCard(card.getPlayerId(), card);
			selfCard.setStatus(EFFECT_SUCC, true);
			BattleRole role = room.getBattleRole(arg.getPlayerId());
			role.getQuestManager().addCount(QuestManager.KILL, QuestManager.ARTIFACT_OR_TRAP, 1);
			break;

		case CardModel.TROOP:
			TroopCard troop = (TroopCard) card;
			if (isSpellBlock(room, arg.getTriggerOwner(), troop)) {
				break;
			}
			if (selfCard.getUid() == troop.getUid() && TriggerManager.WARCRY.equals(arg.getModel().Trigger)) {
				break;
			}
			troop.setHp(0);
			room.cardDeath(troop);
			room.deathcrySync(troop);
			if (!troop.isUndead()) {
				room.destoryCard(troop.getPlayerId(), troop);
			}
			room.deathcry(troop);
			selfCard.setStatus(EFFECT_SUCC, true);
			if (selfCard.getStatus().get(KILL_TROOP_COUNT) != null) {
				selfCard.addStatusCount(KILL_TROOP_COUNT, 1);
			}
			break;
		}
	}

	private void killEnemyRandomHalf(SkillArg arg) {
		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		ArrayList<CardBase> list = room.getCardsByPlayerId(room.getEnemyId(selfCard.getPlayerId()));
		int size = list.size();
		if (list.size() == 0) {
			return;
		}
		Collections.shuffle(list);
		int count = (size % 2) == 0 ? size / 2 : size / 2 + 1;
		for (int i = 0; i < count; i++) {
			CardBase card = list.get(i);
			kill(arg, card);
		}
	}

	private void killTroopLimitAttack(SkillArg arg, ArrayList<TroopCard> list) {
		SkillModel model = arg.getModel();
		int valueInt = expression(model.Value);
		for (TroopCard troop : list) {
			if (troop.getRealAttack() <= valueInt) {
				kill(arg, troop);
			}
		}
	}

	/**
	 * 部队牌增减属性
	 * 
	 * @param selfCard
	 * @param area
	 * @param self
	 * @param model
	 * @param sendType
	 * @return
	 */
	private ArrayList<TroopCard> troopAttr(SkillArg arg) {
		ArrayList<TroopCard> result = new ArrayList<>();
		SkillModel model = arg.getModel();
		String target = model.Target;
		String value = model.Value;
		Area area = arg.getArea();
		CardBase selfCard = arg.getSelfCard();
		GameRoom room = arg.getRoom();

		switch (target) {
		case SELF_AREA:
			for (TroopCard card : area.getTroops()) {
				addAttrSync(arg, card, value, result);
			}
			break;
		case ATTACKER:
			TroopCard attacker = (TroopCard) arg.getAttCard();
			addAttrSync(arg, attacker, value, result);
			break;
		case TRIGGER:
			TroopCard targetTroop = (TroopCard) arg.getTrigger();
			addAttrSync(arg, targetTroop, value, result);
			break;
		case DEFENDER:
			IBattleObject defCard = arg.getDefCard();
			if (defCard instanceof TroopCard) {
				TroopCard card = (TroopCard) defCard;
				addAttrSync(arg, card, value, result);
			}
			break;
		case OPP_AREA_TROOP:
			ArrayList<TroopCard> oppAreaTroop = SkillTargetSelect.getInstance().getOppAreaTroop(room, selfCard);
			for (TroopCard card : oppAreaTroop) {
				addAttrSync(arg, card, value, result);
			}
			break;
		case SELF_AREA_ORC:
			for (TroopCard card : area.getTroops()) {
				if (CardModel.ORC.equals(card.getSubType())) {
					addAttrSync(arg, card, value, result);
				}
			}
			break;
		case DECK_TROOP:
			BattleRole role = room.getBattleRole(selfCard.getPlayerId());
			ArrayList<CardBase> decks = role.getDecks();
			for (CardBase temp : decks) {
				if (temp.getType() != CardModel.TROOP) {
					continue;
				}
				TroopCard card = (TroopCard) temp;
				addAttr(arg, card, value);
			}
			break;
		case DECK_TROOP_1:
			CardBase next = getDeckNext(room, selfCard, CardModel.TROOP, null, null);
			if (next == null) {
				return result;
			}
			TroopCard card = (TroopCard) next;
			addAttr(arg, card, value);
			break;
		case DECK_COYOTLE_TROOP_1:
			next = getDeckNext(room, selfCard, CardModel.TROOP, null, CardModel.COYOTLE);
			if (next == null) {
				return result;
			}
			card = (TroopCard) next;
			addAttr(arg, card, value);
			break;
		case DECK_COYOTLE_TROOP_3:
			ArrayList<CardBase> deckNext = getDeckNext(room, selfCard, 3, CardModel.TROOP, null, CardModel.COYOTLE);
			for (CardBase temp : deckNext) {
				card = (TroopCard) temp;
				addAttr(arg, card, value);
			}
			break;
		case DECK_COYOTLE_TROOP_5:
			deckNext = getDeckNext(room, selfCard, 5, CardModel.TROOP, null, CardModel.COYOTLE);
			for (CardBase temp : deckNext) {
				card = (TroopCard) temp;
				addAttr(arg, card, value);
			}
			break;
		case DECK_COYOTLE_TROOP_1_5:
			deckNext = getDeckNext(room, selfCard, 5, CardModel.TROOP, null, CardModel.COYOTLE);
			if (deckNext.size() == 0) {
				return result;
			}
			int index = Tools.random(0, deckNext.size() - 1);
			card = (TroopCard) deckNext.get(index);
			addAttr(arg, card, value);
			break;
		case SELF:
			card = (TroopCard) selfCard;
			addAttrSync(arg, card, value, result);
			break;
		case SELF_ANYWHERE:
			card = (TroopCard) selfCard;
			role = room.getBattleRole(selfCard.getPlayerId());
			arg.setFighter(role);
			if (role.getHandCards().indexOf(selfCard) != -1) {
				addAttr(arg, card, value);
				room.handcardAttrSync(card);
			} else if (selfCard.getAreaIndex() != -1) {
				addAttrSync(arg, card, value, result);
			} else {
				addAttr(arg, card, value);
			}
			break;
		case C_TROOP:
		case C_TROOP_MINE:
		case C_TROOP_ENEMY:
		case C_TROOP_ORC_MINE:
		case C_TROOP_AWAKE_MINE:
		case C_TROOP_AREA_SELF:
			ArrayList<Object> targets = room.getTarget(arg);
			if (targets.size() != 0) {
				card = (TroopCard) targets.get(0);
				if (TrapTriggerManager.getInstance().spellBlock(room, card, selfCard)) {
					break;
				}
				if (isSpellBlock(room, selfCard, card)) {
					break;
				}
				if (C_TROOP_ORC_MINE.equals(target)) {
					if (!CardModel.ORC.equals(card.getSubType())) {
						break;
					}
				}
				addAttrSync(arg, card, value, result);
			}
			break;
			
		case C_AREA:
		case C_AREA_MINE:
		case C_AREA_ENEMY:
			targets = room.getTarget(arg);
			if (targets.size() == 0) {
				return result;
			}
			Area targetArea = (Area) targets.get(0);
			for (TroopCard temp : targetArea.getTroops()) {
				addAttrSync(arg, temp, value, result);
			}
			break;
			
		case GUARDIANS:
			for (Area tempArea : arg.getFighter().getAreas()) {
				for (TroopCard troop : tempArea.getTroops()) {
					if (troop.isGuardian()) {
						if (troop.getUid() == selfCard.getUid()) {
							if (TriggerManager.WARCRY.equals(model.Trigger)) {
								continue;
							}
						}
						addAttrSync(arg, troop, value, result);
					}
				}
			}
			break;
		case COST_DOWN_1:
			for (Area tempArea : arg.getFighter().getAreas()) {
				for (TroopCard troop : tempArea.getTroops()) {
					if (troop.getCost(arg.getFighter()) <= 1) {
						addAttrSync(arg, troop, value, result);
					}
				}
			}
			break;
		case COST_UP_7:
			for (CardBase tempCard : arg.getFighter().getHandCards()) {
				if (tempCard.getType() != CardModel.TROOP) {
					continue;
				}
				TroopCard tempTroop = (TroopCard) tempCard;
				if (tempCard.getCost(arg.getFighter()) >= 7) {
					addAttr(arg, tempTroop, value);
					room.handcardAttrSync(tempTroop);
				}
			}
			break;
		case NEARBY:
			TroopCard troop = (TroopCard) selfCard;
			role = room.getBattleRole(troop.getPlayerId());
			area = role.getArea(troop.getAreaIndex());
			troop.addLeaderSkillTriggers(model);
			for (TroopCard tempTroop : area.getTroops()) {
				if (troop.getUid() == tempTroop.getUid()) {
					continue;
				}
				addAttrSync(arg, tempTroop, value, result);
			}

			break;
		case SELF_AREA_RANDOM:
			ArrayList<TroopCard> troops = area.getTroops();
			if (troops.size() == 0) {
				return result;
			}
			index = Tools.random(0, troops.size() - 1);
			card = troops.get(index);
			addAttrSync(arg, card, value, result);
			break;
		case MINE_ALL_TROOP:
			troops = room.getTroopsByPlayerId(arg.getFighter().getPlayerId(), selfCard, model);
			for (TroopCard tempTroop : troops) {
				addAttrSync(arg, tempTroop, value, result);
			}
			break;
		case ENEMY_ALL_TROOP:
			troops = room.getTroopsByPlayerId(room.getEnemyId(arg.getFighter().getPlayerId()));
			for (TroopCard tempTroop : troops) {
				addAttrSync(arg, tempTroop, value, result);
			}
			break;
		case WATER_TEMPLE:
			if (arg.getFighter().getWaterTemple() != 0) {
				for (Area tempArea : arg.getFighter().getAreas()) {
					if (tempArea.getRune() != WATER || tempArea.getLevel() != Area.MAX_LEVEL) {
						continue;
					}
					for (TroopCard tempTroop : tempArea.getTroops()) {
						addAttrSync(arg, tempTroop, value, result);
					}
				}
			}
			break;
		case EFFECT_CARD:
			ArrayList<CardBase> drawCards = selfCard.getEffectCards();
			if (drawCards == null || drawCards.size() == 0) {
				return result;
			}
			for (CardBase temp : drawCards) {
				if (temp.getType() == CardModel.TROOP) {
					TroopCard tempTroop = (TroopCard) temp;
					role = room.getBattleRole(tempTroop.getPlayerId());
					if (role.getHandCards().indexOf(tempTroop) != -1) {
						addAttr(arg, tempTroop, value);
						room.handcardAttrSync(tempTroop);
					} else if (tempTroop.getAreaIndex() != -1) {
						addAttrSync(arg, tempTroop, value, result);
					} else {
						addAttr(arg, tempTroop, value);
					}
				}
			}
			break;
		default:
			troops = null;
			ArrayList<CardBase> cards = null;
			if (target.indexOf(SELF_AREA) != -1) {
				target = target.substring(target.indexOf("-") + 1, target.length());
				troops = area.getTroops();
			} else if (target.indexOf(DECK_TROOP) != -1) {
				target = target.substring(target.indexOf("-") + 1, target.length());
				troops = new ArrayList<>();
				cards = arg.getFighter().getDecks();
			} else {
				troops = room.getTroopsByPlayerId(arg.getFighter().getPlayerId(), selfCard, model);
			}
			if (isTargetSubType(target)) {
				if (troops != null) {
					for (TroopCard tempTroop : troops) {
						if (target.equals(tempTroop.getSubType())) {
							addAttrSync(arg, tempTroop, value, result);
						}
					}
				} else if (cards != null) {
					for (CardBase temp : cards) {
						if (temp.getType() != CardModel.TROOP) {
							continue;
						}
						TroopCard tempTroop = (TroopCard) temp;
						if (target.equals(tempTroop.getSubType())) {
							addAttrSync(arg, tempTroop, value, result);
						}
					}
				}
			} else if (isCardId(target)) {
				if (troops != null) {
					for (TroopCard tempTroop : troops) {
						if (target.equals(tempTroop.getId())) {
							addAttrSync(arg, tempTroop, value, result);
						}
					}
				} else if (cards != null) {
					for (CardBase temp : cards) {
						if (temp.getType() != CardModel.TROOP) {
							continue;
						}
						TroopCard tempTroop = (TroopCard) temp;
						if (target.equals(tempTroop.getId())) {
							addAttrSync(arg, tempTroop, value, result);
						}
					}
				}
			}
			break;
		}

		return result;
	}
	
	private void addAttrSync(SkillArg arg, TroopCard troop, String value, ArrayList<TroopCard> result) {
		boolean alive = troop.isAlive();
		addAttr(arg, troop, value);
		if (alive) {
			setTroopSyncResult(result, arg.getSelfCard(), troop, arg.getSendType());
		}
	}

	/**
	 * 部队牌 攻击低于血量，使其攻击等于血量
	 * 
	 * @param arg
	 * @return
	 */
	private ArrayList<TroopCard> setAtkByHp(SkillArg arg) {
		ArrayList<TroopCard> result = new ArrayList<>();
		SkillModel model = arg.getModel();
		switch (model.Target) {
		case MINE_ALL_TROOP:
			for (Area tempArea : arg.getFighter().getAreas()) {
				for (TroopCard card : tempArea.getTroops()) {
					if (card.getRealAttack() >= card.getHp()) {
						continue;
					}
					boolean alive = card.isAlive();
					addAttr(arg, card, String.valueOf(card.getHp() - card.getRealAttack()));
					if (alive) {
						setTroopSyncResult(result, arg.getSelfCard(), card, arg.getSendType());
					}
				}
			}
			break;
		}

		return result;
	}

	private ArrayList<TroopCard> troopStatus(SkillArg arg, String status) {
		ArrayList<TroopCard> result = new ArrayList<>();
		SkillModel model = arg.getModel();
		String target = model.Target;
		Area area = arg.getArea();
		CardBase selfCard = arg.getSelfCard();
		BattleRole self = arg.getFighter();
		GameRoom room = arg.getRoom();

		switch (target) {
		case C_TROOP:
		case C_TROOP_MINE:
		case C_TROOP_ENEMY:
		case C_TROOP_ORC_MINE:
		case C_TROOP_AWAKE_MINE:
			ArrayList<Object> targets = room.getTarget(arg);
			if (targets.size() == 0) {
				return result;
			}
			TroopCard troop = (TroopCard) targets.get(0);
			if (TrapTriggerManager.getInstance().spellBlock(room, troop, selfCard)) {
				break;
			}
			if (isSpellBlock(room, selfCard, troop)) {
				break;
			}
			if (C_TROOP_ORC_MINE.equals(target)) {
				if (!CardModel.ORC.equals(troop.getSubType())) {
					break;
				}
			}
			troopStatus(arg, troop, status, result);
			break;
		case C_AREA:
		case C_AREA_MINE:
		case C_AREA_ENEMY:
			targets = room.getTarget(arg);
			if (targets.size() == 0) {
				return result;
			}
			Area targetArea = (Area) targets.get(0);
			for (TroopCard temp : targetArea.getTroops()) {
				troopStatus(arg, temp, status, result);
			}
			break;
		case SELF:
			troop = (TroopCard) arg.getTriggerOwner();
			troopStatus(arg, troop, status, result);
			break;
		case SELF_AREA:
			if (selfCard.getType() == CardModel.TROOP) {
				troop = (TroopCard) selfCard;
				troop.addLeaderSkillTriggers(model);
			}
			for (TroopCard card : area.getTroops()) {
				troopStatus(arg, card, status, result);
			}
			break;

		case SELF_AREA_ORC:
			if (selfCard.getType() != CardModel.TROOP) {
				return result;
			}
			troop = (TroopCard) selfCard;
			troop.addLeaderSkillTriggers(model);
			for (TroopCard card : area.getTroops()) {
				if (CardModel.ORC.equals(card.getSubType())) {
					troopStatus(arg, card, status, result);
				}
			}
			break;
		case ATTACKER:
			TroopCard attacker = (TroopCard) arg.getAttCard();
			troopStatus(arg, attacker, status, result);
			break;
			
		case DEFENDER:
			IBattleObject defCard = arg.getDefCard();
			if (defCard instanceof TroopCard) {
				TroopCard card = (TroopCard) defCard;
				troopStatus(arg, card, status, result);
			}
			break;
			
		case TRIGGER:
			TroopCard targetTroop = (TroopCard) arg.getTrigger();
			troopStatus(arg, targetTroop, status, result);
			break;
		case OPP_AREA_TROOP:
			ArrayList<TroopCard> oppAreaTroop = SkillTargetSelect.getInstance().getOppAreaTroop(room, selfCard);
			for (TroopCard card : oppAreaTroop) {
				troopStatus(arg, card, status, result);
			}
			break;
		case DECK_TROOP_1:
			CardBase next = getDeckNext(room, selfCard, CardModel.TROOP, model.Value, null);
			if (next == null) {
				return result;
			}
			TroopCard card = (TroopCard) next;
			troopStatus(arg, card, status, null);
			break;
		case DECK_TROOP_3:
			ArrayList<CardBase> deckNext = getDeckNext(room, selfCard, 3, CardModel.TROOP, model.Value, null);
			for (CardBase temp : deckNext) {
				card = (TroopCard) temp;
				troopStatus(arg, card, status, null);
			}
			break;
		case DECK_COYOTLE_TROOP_1:
			next = getDeckNext(room, selfCard, CardModel.TROOP, model.Value, CardModel.COYOTLE);
			if (next == null) {
				return result;
			}
			card = (TroopCard) next;
			troopStatus(arg, card, status, null);
			break;
		case DECK_COYOTLE_TROOP_3:
			deckNext = getDeckNext(room, selfCard, 3, CardModel.TROOP, model.Value, CardModel.COYOTLE);
			for (CardBase temp : deckNext) {
				card = (TroopCard) temp;
				troopStatus(arg, card, status, null);
			}
			break;
		case DECK_COYOTLE_TROOP_5:
			deckNext = getDeckNext(room, selfCard, 5, CardModel.TROOP, model.Value, CardModel.COYOTLE);
			for (CardBase temp : deckNext) {
				card = (TroopCard) temp;
				troopStatus(arg, card, status, null);
			}
			break;
		case DECK_COYOTLE_TROOP_1_5:
			deckNext = getDeckNext(room, selfCard, 5, CardModel.TROOP, model.Value, CardModel.COYOTLE);
			if (deckNext.size() == 0) {
				return result;
			}
			int index = Tools.random(0, deckNext.size() - 1);
			card = (TroopCard) deckNext.get(index);
			troopStatus(arg, card, status, null);
			break;
		case SELF_ANYWHERE:
			card = (TroopCard) selfCard;
			BattleRole role = room.getBattleRole(selfCard.getPlayerId());
			arg.setFighter(role);
			if (role.getHandCards().indexOf(selfCard) != -1) {
				troopStatus(arg, card, status, null);
			} else if (selfCard.getAreaIndex() != -1) {
				troopStatus(arg, card, status, result);
			} else {
				troopStatus(arg, card, status, null);
			}
			break;
		case GUARDIANS:
			for (Area tempArea : self.getAreas()) {
				for (TroopCard tempTroop : tempArea.getTroops()) {
					if (tempTroop.isGuardian()) {
						if (tempTroop.getUid() == selfCard.getUid()) {
							if (TriggerManager.WARCRY.equals(model.Trigger)) {
								continue;
							}
						}
						troopStatus(arg, tempTroop, status, result);
					}
				}
			}
			break;
		case FLIGHTS:
			for (Area tempArea : self.getAreas()) {
				for (TroopCard tempTroop : tempArea.getTroops()) {
					if (tempTroop.isFlight()) {
						if (tempTroop.getUid() == selfCard.getUid()) {
							if (TriggerManager.WARCRY.equals(model.Trigger)) {
								continue;
							}
						}
						troopStatus(arg, tempTroop, status, result);
					}
				}
			}
			break;
		case STUNS:
			for (Area tempArea : self.getAreas()) {
				for (TroopCard tempTroop : tempArea.getTroops()) {
					if (tempTroop.isStun()) {
						if (tempTroop.getUid() == selfCard.getUid()) {
							if (TriggerManager.WARCRY.equals(model.Trigger)) {
								continue;
							}
						}
						troopStatus(arg, tempTroop, status, result);
					}
				}
			}
			break;
		case MINE_AWAKE_TROOP:
			for (Area tempArea : self.getAreas()) {
				for (TroopCard tempTroop : tempArea.getTroops()) {
					if (tempTroop.isEnchant()) {
						if (tempTroop.getUid() == selfCard.getUid()) {
							if (TriggerManager.WARCRY.equals(model.Trigger)) {
								continue;
							}
						}
						troopStatus(arg, tempTroop, status, result);
					}
				}
			}
			break;
		case COST_DOWN_1:
			for (Area tempArea : arg.getFighter().getAreas()) {
				for (TroopCard tempTroop : tempArea.getTroops()) {
					if (tempTroop.getCost(arg.getFighter()) <= 1) {
						troopStatus(arg, tempTroop, status, result);
					}
				}
			}
			break;
		case NEARBY:
			troop = (TroopCard) selfCard;
			role = room.getBattleRole(troop.getPlayerId());
			area = role.getArea(troop.getAreaIndex());
			troop.addLeaderSkillTriggers(model);
			for (TroopCard tempTroop : area.getTroops()) {
				if (troop.getUid() == tempTroop.getUid()) {
					continue;
				}
				troopStatus(arg, tempTroop, status, result);
			}

			break;
		case ORC_NEARBY:
			troop = (TroopCard) selfCard;
			troop.addLeaderSkillTriggers(model);
			for (TroopCard tempTroop : area.getTroops()) {
				if (tempTroop.getUid() == troop.getUid()) {
					continue;
				}
				if (CardModel.ORC.equals(tempTroop.getSubType())) {
					troopStatus(arg, tempTroop, status, result);
				}
			}
			break;
		case SELF_AREA_RANDOM:
			ArrayList<TroopCard> troops = area.getTroops();
			if (troops.size() == 0) {
				return result;
			}
			index = Tools.random(0, troops.size() - 1);
			card = troops.get(index);
			troopStatus(arg, card, status, result);
			break;
		case MINE_ALL_TROOP:
			troops = room.getTroopsByPlayerId(arg.getFighter().getPlayerId(), selfCard, model);
			for (TroopCard tempTroop : troops) {
				troopStatus(arg, tempTroop, status, result);
			}
			break;
		case WATER_TEMPLE:
			if (self.getWaterTemple() != 0) {
				for (Area tempArea : self.getAreas()) {
					if (tempArea.getRune() != WATER || tempArea.getLevel() != Area.MAX_LEVEL) {
						continue;
					}
					for (TroopCard tempTroop : tempArea.getTroops()) {
						troopStatus(arg, tempTroop, status, result);
					}
				}
			}
			break;
		case COST_EQUAL:
			role = room.getBattleRole(selfCard.getPlayerId());
			troops = room.getTroopsByPlayerId(selfCard.getPlayerId());
			for (TroopCard temp : troops) {
				if (temp.getCost(role) == selfCard.getCost(role)) {
					troopStatus(arg, temp, status, result);
				}
			}
			break;
		case ENEMY_HERO:
			if (cardStatus(arg, selfCard, status)) {
				int enemyId = room.getEnemyId(selfCard.getPlayerId());
				BattleRole enemy = room.getBattleRole(enemyId);
				enemy.addStatusCount(status, 1);
			}
			break;
		case EFFECT_CARD:
			ArrayList<CardBase> drawCards = selfCard.getEffectCards();
			if (drawCards == null || drawCards.size() == 0) {
				return result;
			}
			for (CardBase temp : drawCards) {
				if (temp.getType() == CardModel.TROOP) {
					TroopCard tempTroop = (TroopCard) temp;
					role = room.getBattleRole(selfCard.getPlayerId());
					arg.setFighter(role);
					if (role.getHandCards().indexOf(tempTroop) != -1) {
						troopStatus(arg, tempTroop, status, null);
					} else if (tempTroop.getAreaIndex() != -1) {
						troopStatus(arg, tempTroop, status, result);
					} else {
						troopStatus(arg, tempTroop, status, null);
					}
				}
			}
			break;
			
		default:
			if (target.indexOf(SELF_AREA) != -1) {
				target = target.substring(target.indexOf("-") + 1, target.length());
				troops = area.getTroops();
			} else {
				troops = room.getTroopsByPlayerId(arg.getFighter().getPlayerId(), selfCard, model);
			}
			if (isTargetSubType(target)) {
				for (TroopCard tempTroop : troops) {
					if (target.equals(tempTroop.getSubType())) {
						troopStatus(arg, tempTroop, status, result);
					}
				}
			} else if (isCardId(target)) {
				for (TroopCard tempTroop : troops) {
					if (target.equals(tempTroop.getId())) {
						troopStatus(arg, tempTroop, status, result);
					}
				}
			}
			break;
		}

		return result;
	}

	private ArrayList<TroopCard> troopStatusRemove(SkillArg arg, String status) {

		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		SkillModel model = arg.getModel();
		
		ArrayList<TroopCard> result = new ArrayList<>();
		switch (model.Target) {
		case SELF:
			TroopCard troop = (TroopCard) selfCard;
			troopStatusRemove(room, troop, status);
			break;
		case C_TROOP:
		case C_TROOP_MINE:
		case C_TROOP_ENEMY:
			ArrayList<Object> target = room.getTarget(arg);
			if (target.size() == 0) {
				return result;
			}
			troop = (TroopCard) target.get(0);
			if (TrapTriggerManager.getInstance().spellBlock(room, troop, selfCard)) {
				break;
			}
			if (isSpellBlock(room, selfCard, troop)) {
				break;
			}
			troopStatusRemove(room, troop, status);
			break;
		case C_AREA:
		case C_AREA_MINE:
		case C_AREA_ENEMY:
			target = room.getTarget(arg);
			if (target.size() == 0) {
				return result;
			}
			Area area = (Area) target.get(0);
			if (TrapTriggerManager.getInstance().spellBlock(room, area, selfCard)) {
				break;
			}
			for (TroopCard tempTroop : area.getTroops()) {
				if (isSpellBlock(room, selfCard, tempTroop)) {
					continue;
				}
				troopStatusRemove(room, tempTroop, status);
			}
			break;
		case OPP_AREA_TROOP:
			ArrayList<TroopCard> oppAreaTroop = SkillTargetSelect.getInstance().getOppAreaTroop(room, selfCard);
			for (TroopCard t : oppAreaTroop) {
				if (isSpellBlock(room, selfCard, t)) {
					continue;
				}
				troopStatusRemove(room, t, status);
			}
			break;
		case ENEMY_ALL_TROOP:
			ArrayList<TroopCard> enemyTroops = room.getTroopsByPlayerId(room.getEnemyId(selfCard.getPlayerId()));
			if (enemyTroops.size() == 0) {
				return result;
			}
			for (TroopCard tempTroop : enemyTroops) {
				if (isSpellBlock(room, selfCard, tempTroop)) {
					continue;
				}
				troopStatusRemove(room, tempTroop, status);
			}
			break;
		}
		return result;
	}
	
	private void copyShuffle(SkillArg arg) {

		GameRoom room = arg.getRoom();
		SkillModel model = arg.getModel();
		
		ArrayList<CardBase> result = new ArrayList<>();
		switch (model.Target) {
		case C_HAND_CARD_MINE:
		case C_HAND_CARD_OR_FRIEND:
			ArrayList<Object> target = room.getTarget(arg);
			if (target.size() == 0) {
				return;
			}
			CardBase cardBase = (CardBase) target.get(0);
			BattleRole role = room.getBattleRole(cardBase.getPlayerId());
			int intValue = expression(model.Value);
			ArrayList<CardBase> decks = role.getDecks();
			for (int i = 0; i < intValue; i++) {
				CardBase card = room.createCard(role.getPlayerId(), cardBase.getRealId());
				if (card == null) {
					continue;
				}
				if (decks.size() == 0) {
					decks.add(card);
				} else {
					int index = Tools.random(0, decks.size() - 1);
					decks.add(index, card);
				}
				result.add(card);
			}
			room.deckCardNumberSync(role);
			return;
		}
	}

	private void shuffle(SkillArg arg) {

		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		SkillModel model = arg.getModel();
		
		switch (model.Target) {
		case SELF:
			BattleRole role = room.getBattleRole(selfCard.getPlayerId());
			ArrayList<CardBase> decks = role.getDecks();
			if (decks.size() == 0) {
				decks.add(selfCard);
			} else {
				int index = Tools.random(0, decks.size() - 1);
				decks.add(index, selfCard);
			}
			role.getDiscards().remove(selfCard);
			room.deckCardNumberSync(role);
			break;
		case C_HAND_CARD_MINE:
			ArrayList<Object> target = room.getTarget(arg);
			if (target.size() == 0) {
				room.sendTargetSelect(selfCard, arg);
				return;
			}
			CardBase cardBase = (CardBase) target.get(0);
			role = room.getBattleRole(cardBase.getPlayerId());
			room.removeHandCard(role, selfCard);
			decks = role.getDecks();
			if (decks.size() == 0) {
				decks.add(cardBase);
			} else {
				int index = Tools.random(0, decks.size() - 1);
				decks.add(index, cardBase);
			}
			
			room.handCardToDeck(cardBase);
			room.deckCardNumberSync(role);
			break;
		default:
			int valueInt = expression(model.Value);
			for (int i = 0; i < valueInt; i++) {
				String cardId = getRandomCardIdBySubType(model.Target);
				if (ConfigData.cardModels.get(cardId) == null) {
					return;
				}
				role = room.getBattleRole(selfCard.getPlayerId());
				cardBase = room.createCard(role.getPlayerId(), cardId);
				if (cardBase == null) {
					continue;
				}
				decks = role.getDecks();
				if (decks.size() == 0) {
					decks.add(cardBase);
				} else {
					int index = Tools.random(0, decks.size() - 1);
					decks.add(index, cardBase);
				}
				room.deckCardNumberSync(role);
			}
			break;
		}
	}

	private ArrayList<CardBase> cost(SkillArg arg) {

		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		BattleRole self = arg.getFighter();
		SkillModel model = arg.getModel();
		
		ArrayList<CardBase> result = new ArrayList<>();
		BattleRole fighter = self;
		if (model.Target.indexOf(ENEMY) != -1) {
			fighter = room.getBattleRole(room.getEnemyId(self.getPlayerId()));
		}
		int valueInt = expression(model.Value, arg);
		if (valueInt == 0 && !model.Value.equals("0")) {
			return result;
		}
		if (model.Cancel == 1) {
			String target = model.Target;
			if (SELF.equals(model.Target) || SELF_ANYWHERE.equals(model.Target)) {
				target = String.valueOf(selfCard.getUid());
			} else if (target.indexOf(AREA_COST) != -1) {
				target = AREA_COST;
			}
			Effect effect = fighter.getEffects(selfCard.getUid(), model.Index);
			if (effect == null) {
				fighter.setEffects(selfCard.getUid(), model.Index, getEffect(selfCard, model, target, valueInt));
				fighter.addCostArg(target, valueInt);
			} else {
				if (!effect.isRepeat()) {
					if (effect.getValue() != valueInt) {
						fighter.addCostArg(effect.getEffectType(), -effect.getValue());
						effect.setValue(valueInt);
						fighter.addCostArg(target, valueInt);
					} else {
						return result;
					}
				} else {
					effect.setValue(effect.getValue() + valueInt);
					fighter.addCostArg(target, valueInt);
				}
			}
		}

		switch (model.Target) {
		case TALE:
			ArrayList<CardBase> handCards = fighter.getHandCards();
			for (CardBase card : handCards) {
				if (card.getUid() == selfCard.getUid()) {
					continue;
				}
				if (TALE.equals(card.getSubType())) {
					result.add(card);
				}
			}
			return result;
		case DECK_COYOTLE_TROOP_1:
			CardBase next = getDeckNext(room, selfCard, CardModel.TROOP, null, CardModel.COYOTLE);
			if (next == null) {
				return result;
			}
			next.addCost(valueInt);
			break;
		case DECK_TROOP_1:
			next = getDeckNext(room, selfCard, CardModel.TROOP, null, null);
			next.addCost(valueInt);
			break;
		case DECK_TROOP:
			BattleRole role = room.getBattleRole(selfCard.getPlayerId());
			ArrayList<CardBase> decks = role.getDecks();
			for (CardBase temp : decks) {
				if (temp.getType() != CardModel.TROOP) {
					continue;
				}
				temp.addCost(valueInt);
			}
			deckCardModity(room, role);
			break;
		case DECK_TOP:
			next = getDeckNext(room, selfCard, 0, null, null);
			next.addCost(valueInt);
			break;
		case HAND_CARDS_RANDOM:
			handCards = fighter.getHandCards();
			if (handCards.size() == 0) {
				return result;
			}
			int index = Tools.random(0, handCards.size() - 1);
			CardBase card = handCards.get(index);
			card.addCost(valueInt);
			result.add(card);
			return result;
		case HAND_CARDS_RANDOM_HUMAN:
			ArrayList<CardBase> tempList = new ArrayList<>();
			tempList.addAll(fighter.getHandCards());
			if (tempList.size() == 0) {
				return result;
			}
			Collections.shuffle(tempList);
			for (CardBase tempCard : tempList) {
				if (CardModel.HUMAN.equals(tempCard.getSubType())) {
					tempCard.addCost(valueInt);
					result.add(tempCard);
					return result;
				}
			}
			break;
		case HAND_CARD_COST_MAX:
			card = fighter.getCostMaxInHandCard();
			card.addCost(valueInt);
			result.add(card);
			break;
		case HAND_CARDS:
			handCards = fighter.getHandCards();
			for (CardBase temp : handCards) {
				if (model.Repeat == 1) {
					temp.addCost(valueInt);
				}
				if (temp.getUid() == selfCard.getUid()) {
					continue;
				}
				result.add(temp);
			}
			return result;
		case C_HAND_CARD_MINE:
			ArrayList<Object> target = room.getTarget(arg);
			if (target.size() == 0) {
				return result;
			}
			CardBase cardBase = (CardBase) target.get(0);
			cardBase.addCost(valueInt);
			result.add(cardBase);
			return result;
		case ENEMY_SPELL_CARD:
			handCards = fighter.getHandCards();
			for (CardBase temp : handCards) {
				if (temp.getUid() == selfCard.getUid()) {
					continue;
				}
				if (temp.getType() == CardModel.SPELL) {
					result.add(temp);
				}
			}
			return result;
		case MINE_TROOP_CARD:
		case ENEMY_TROOP_CARD:
			handCards = fighter.getHandCards();
			for (CardBase temp : handCards) {
				if (temp.getUid() == selfCard.getUid()) {
					continue;
				}
				if (temp.getType() == CardModel.TROOP) {
					result.add(temp);
				}
			}
			return result;
		case DRAW:
			selfCard.setStatusTrun(DRAW, valueInt);
			return result;

		case MINE_AREA_COST:
		case ENEMY_AREA_COST:
			room.areaLvUpNeedResource(fighter);
			return result;

		case SELF:
		case SELF_ANYWHERE:
			if (model.Cancel == 0) {
				selfCard.addCost(valueInt);
			}
			handCards = fighter.getHandCards();
			if (handCards.size() == 0) {
				return result;
			}
			if (arg.getSendType() == 0) {
				return result;
			}
			if (handCards.indexOf(selfCard) != -1) {
				result.add(selfCard);
			} else if (selfCard.getAreaIndex() != -1) {
				result.add(selfCard);
			}
			return result;
		case RETURN:
			selfCard.setStatusTrun(RETURN, valueInt);
			return result;
		case C_TROOP:
		case C_TROOP_MINE:
		case C_TROOP_ENEMY:
			ArrayList<Object> targets = room.getTarget(arg);
			if (targets.size() != 0) {
				card = (TroopCard) targets.get(0);
				card.addCost(valueInt);
				result.add(card);
			}
			break;
		case C_AREA:
		case C_AREA_MINE:
		case C_AREA_ENEMY:
			target = room.getTarget(arg);
			if (target.size() == 0) {
				return result;
			}
			Area targetArea = (Area) target.get(0);
			for (TroopCard tempCard : targetArea.getTroops()) {
				tempCard.addCost(valueInt);
				result.add(tempCard);
			}
			for (CardBase tempCard : targetArea.getArtiTraps()) {
				tempCard.addCost(valueInt);
				result.add(tempCard);
			}
			break;
		case EFFECT_CARD:
			ArrayList<CardBase> drawCards = selfCard.getEffectCards();
			if (drawCards == null || drawCards.size() == 0) {
				return result;
			}
			for (CardBase temp : drawCards) {
				if (model.Cancel == 0) {
					temp.addCost(valueInt);
					role = room.getBattleRole(temp.getPlayerId());
					handCards = role.getHandCards();
					if (handCards.indexOf(temp) != -1) {
						result.add(temp);
					} else if (temp.getAreaIndex() != -1) {
						result.add(temp);
					}
				}
			}
			break;
		}

		if (model.Target.indexOf(DRAW) != -1) {
			selfCard.setStatus(DRAW_COST_BY_SUBTYPE, true);
			selfCard.setStatusTrun(model.Target, valueInt);
		}
		return result;
	}

	/**
	 * 部队牌属性效果
	 * 
	 * @param selfCard
	 * @param card
	 * @param value
	 * @param area
	 * @param self
	 * @param model
	 * @param sendType
	 */
	private void addAttr(SkillArg arg, TroopCard card, String value) {

		CardBase selfCard = arg.getSelfCard();
		BattleRole self = arg.getFighter();
		SkillModel model = arg.getModel();

		int valueInt = expression(value, arg, card);
		if (SPELL.equals(model.Genius) || selfCard.getType() == CardModel.SPELL) {
			addAttr(card, model.Type, valueInt, self);
			return;
		}

		Effect effect = card.getEffects(selfCard.getUid(), model.Index);
		if (effect == null) {
			addAttr(card, model.Type, valueInt, self);
			setEffects(card, selfCard, model, valueInt);
			return;
		}

		if (!effect.isRepeat()) {
			if (effect.getValue() != valueInt) {
				delAttr(card, effect.getEffectType(), effect.getValue(), model.Index, self);
				addAttr(card, model.Type, valueInt, self);
				effect.setValue(valueInt);
			}
		} else {
			addAttr(card, model.Type, valueInt, self);
			effect.setValue(effect.getValue() + valueInt);
		}
		return;
	}

	private Effect getEffect(CardBase card, SkillModel model, int value) {
		return new Effect(model.Genius, model.Type, value, model.Cancel == 1, model.Repeat == 1);
	}

	private Effect getEffectByTarget(CardBase card, SkillModel model, int value) {
		return new Effect(model.Genius, model.Target, value, model.Cancel == 1, model.Repeat == 1);
	}

	private Effect getEffect(CardBase card, SkillModel model, String effectType, int value) {
		return new Effect(model.Genius, effectType, value, model.Cancel == 1, model.Repeat == 1);
	}
	
	public Effect getEffect(Effect effect) {
		return new Effect(effect.getTriggerEvent(), effect.getEffectType(), effect.getValue(), effect.isTemp(), effect.isRepeat());
	}

	@SuppressWarnings("unused")
	private boolean getEffectIsTemp(CardBase card, SkillModel model) {
		if (card.getType() == CardModel.ARTIFACT) {
			return true;
		}
		if (card.getType() == CardModel.SPELL) {
			return false;
		}
		if (card.getType() == CardModel.TROOP) {
			if (Tools.isEmptyString(model.Genius)) {
				return false;
			}
			switch (model.Genius) {
			case LEADER:
			case MY_TURN:
			case PATHFINDER:
			case AIR_TEMPLE:
			case EARTH_TEMPLE:
			case FIRE_TEMPLE:
			case WATER_TEMPLE:
				return true;
			}
		}
		return false;
	}

	private boolean isTargetSubType(String subType) {
		return ConfigData.cardModelBySubType.get(subType) != null;
	}

	private boolean isCardId(String cardId) {
		return ConfigData.cardModels.get(cardId) != null;
	}

	private void addAttr(TroopCard card, String type, int value, BattleRole self) {
		switch (type) {
		case ADD_ATK:
		case SET_ATK_BY_HP:
			card.addAttack(value);
			break;
		case ADD_ATK_HP:
			card.addAttack(value);
		case ADD_HP:
			card.addHp(value);
			break;
		case SWITCH_ATK_HP:
			int hp = card.getHp();
			int atk = card.getRealAttack();
			card.setAttack(hp);
			card.setHp(atk);
			break;
		case SET_ATK:
			card.setAttack(value);
			break;
		}
	}

	private void reduceHp(SkillArg arg, TroopCard troop, String value) {
		int valueInt = getAmplify(arg, value, null);
		reduceHp(arg, troop, valueInt, true);
	}

	private void reduceHp(SkillArg arg, TroopCard troop, int valueInt, boolean isSend) {
		boolean alive = troop.isAlive();
		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		BattleRole fighter = arg.getFighter();

		if (troop.isSpellBlock()) {
			troop.setStatus(TroopCard.SPELL_BLOCK, false);
			room.reduceHpSync(fighter.getPlayerId(), selfCard.getUid(), troop.getUid(), 0);
			room.troopStatusSync(troop, TroopCard.SPELL_BLOCK);
			if (selfCard.getLogInfo() != null) {
				selfCard.getLogInfo().addTarget(LogDetailInfo.ATTACK, troop.getRealId(), 0);
			}
		} else {
			if (troop.isInvincible()) {
				valueInt = 0;
			}
			selfCard.setAttackTarget(troop);
			troop.addHp(-valueInt);
			if (arg.getPlayerId() != troop.getPlayerId()) {
				BattleRole role = room.getBattleRole(arg.getPlayerId());
				role.getQuestManager().addCount(QuestManager.DAMAGE, QuestManager.TROOP, valueInt);
			}
			if (selfCard.getLogInfo() != null) {
				selfCard.getLogInfo().addTarget(LogDetailInfo.ATTACK, troop.getRealId(), -valueInt);
			}
			if (isSend && valueInt != 0) {
				room.reduceHpSync(fighter.getPlayerId(), selfCard.getUid(), troop.getUid(), valueInt);
			}
			if (alive) {
				troop.setStatusTrun(TroopCard.LAST_DAMAGE, valueInt);
				if (troop.isAlive()) {
					if (valueInt > 0) {
						room.troopDamagedSync(troop);
						room.triggerEffect(TriggerManager.DAMAGE_AFTER, selfCard.getPlayerId(), selfCard, 1);
						room.triggerEffect(TriggerManager.DAMAGE_ALIVE, troop.getPlayerId(), troop, 1);
					}
				} else {
					room.cardDeath(troop);
					room.deathcrySync(troop);
					if (!troop.isUndead()) {
						room.destoryCard(troop.getPlayerId(), troop);
					}
					room.deathcry(troop);
				}
			}
		}
	}

	public boolean isAutoTrigger(SkillModel model) {
		switch (model.Trigger) {
		case TriggerManager.START:
		case TriggerManager.END:
		case TriggerManager.ENCHANT:
		case TriggerManager.WARCRY:
		case TriggerManager.BREACH:
			return true;
		}
		return false;
	}

	private void delAttr(TroopCard card, String type, int value, int subIndex, BattleRole self) {
		switch (type) {
		case ADD_ATK:
			card.addAttack(-value);
			break;
		case ADD_ATK_HP:
			card.addAttack(-value);
			card.addHp(-value);
			break;
		case ADD_HP:
			card.addHp(-value);
			break;
		case LEADER:
			card.removeLeaderSkillTriggers();
			if (card.getLeaderTriggers().size() == 0) {
				card.setStatus(type, false);
			}
			break;
		case FLIGHT:
		case GUARDIAN:
		case LIFEDRAIN:
		case SPELL_BLOCK:
		case FORCE_SHIELD:
		case DOUBLE_DAMAGE:
		case EXCESS_DAMAGE:
		case OPP_AREA_ATTACK:
		case AVOID_ATTACKED:
		case AVOID_OPP_ATTACKED:
		case ENEMY_CANT_CURE:
		case INVINCIBLE:
		case ATTACK_INVINCIBLE:
		case ALWAYS_ATTACK_HERO:
		case HERO_CANT_BE_ATTACK:
		case AREA_LV_UP_CHANGE_DRAW:
		case CANT_ATTACK:
			card.setStatus(type, false);
			break;
		case BattleRole.AMPLIFY:
			if (!card.isStun()) {
				self.addStatusCount(type, -value);
				self.removeEffects(card.getUid(), subIndex);
			}
			card.addStatusCount(type, -value);
			break;
		case ADD_RES_STOP:
			self.setStatus(ADD_RES_STOP, false);
			break;
		}
	}

	private void heal(SkillArg arg) {
		GameRoom room = arg.getRoom();
		SkillModel model = arg.getModel();
		String target = model.Target;
		switch (target) {
		case SELF_HERO:
			BattleRole role = room.getBattleRole(arg.getPlayerId());
			addHpByHero(arg, role);
			break;
		case ENEMY_HERO:
			BattleRole enemy = room.getBattleRole(room.getEnemyId(arg.getPlayerId()));
			addHpByHero(arg, enemy);
			break;
		}
	}
	
	/**
	 * 英雄增加血量
	 * 
	 * @param fighter
	 * @param value
	 * @param self
	 */
	private void addHpByHero(SkillArg arg, BattleRole fighter) {
		GameRoom room = arg.getRoom();
		int valueInt = getAmplify(arg, arg.getModel().Value, null);
		if (cureHeroHp(arg, fighter, valueInt)) {
			room.triggerEffect(TriggerManager.MINE_HERO_LIFE, fighter.getPlayerId(), 1);
			room.triggerEffect(TriggerManager.CURE_HERO, fighter.getPlayerId(), 1);
			room.heroHpSync(fighter);
		}
	}

	private void reduceHpByHero(SkillArg arg, BattleRole self) {
		GameRoom room = arg.getRoom();
		SkillModel model = arg.getModel();
		CardBase selfCard = arg.getSelfCard();

		BattleRole role = room.getBattleRole(selfCard.getPlayerId());
		arg.setFighter(role);
		int valueInt = getAmplify(arg, model.Value, null);
		if (self.getStatus(TroopCard.DOUBLE_DAMAGE)) {
			valueInt *= 2;
		}
		reduceHeroHp(room, selfCard, self, valueInt);
		room.settlement(self.getPlayerId());
	}

	private int getAmplify(SkillArg arg, String value, CardBase target) {
		GameRoom room = arg.getRoom();
		BattleRole fighter = room.getBattleRole(arg.getTriggerOwner().getPlayerId());
		int valueInt = expression(value, arg, target);
		if (valueInt > 0 && fighter.getStatus(BattleRole.AMPLIFY)) {
			valueInt += fighter.getStatus().get(BattleRole.AMPLIFY);
			if (fighter.getStatus(DOUBLE_AMPLIFY)) {
				valueInt += fighter.getStatus().get(BattleRole.AMPLIFY);
			}
		}
		return valueInt;
	}

	private void reduceHeroHp(GameRoom room, CardBase selfCard, BattleRole self, int valueInt) {
		selfCard.setAttackTarget(self);
		self.addHp(-valueInt);
		room.reduceHpSync(self.getPlayerId(), selfCard.getUid(), self.getUid(), valueInt);
		if (valueInt > 0) {
			room.triggerEffect(TriggerManager.DAMAGE_AFTER, selfCard.getPlayerId(), selfCard, 1);
		}
		room.triggerEffect(TriggerManager.MINE_HERO_LIFE, self.getPlayerId(), 1);
	}

	public void lifeDrain(GameRoom room, TroopCard selfCard, BattleRole self, int valueInt) {
		if (!selfCard.isLifedrain()) {
			return;
		}
		SkillArg arg = new SkillArg(room, selfCard.getPlayerId(), selfCard, null, self, null, 0);
		if (cureHeroHp(arg, self, valueInt)) {
			room.triggerEffect(TriggerManager.MINE_HERO_LIFE, self.getPlayerId(), 1);
			room.lifedrainSync(selfCard);
			logger.info("玩家：{}，房间Id：{}。CardId：{}。吸血{}点。", self.getPlayerId(), room.getRoomId(), selfCard.getRealId(),
					selfCard.getAttack());
		}
	}

	public boolean cureHeroHp(SkillArg arg, BattleRole fighter, int valueInt) {
		GameRoom room = arg.getRoom();
		CardBase selfCard = arg.getSelfCard();
		BattleRole enemy = room.getBattleRole(room.getEnemyId(fighter.getPlayerId()));
		ArrayList<TroopCard> troops = room.getTroopsByPlayerId(enemy.getPlayerId());
		for (TroopCard troop : troops) {
			if (troop.isEnemyCantCure()) {
				return false;
			}
		}
		
		if (room.getTriggerManager().isCureToReduce(fighter)) {
			enemy.addHp(-valueInt);
			if (arg.getPlayerId() != enemy.getPlayerId()) {
				BattleRole role = room.getBattleRole(arg.getPlayerId());
				role.getQuestManager().addCount(QuestManager.DAMAGE, QuestManager.HERO, valueInt);
			}
			if (valueInt != 0) {
				room.reduceHpSync(enemy.getPlayerId(), selfCard.getUid(), enemy.getUid(), valueInt);
			}
			room.settlement(enemy.getPlayerId());
		} else {
			fighter.addHp(valueInt);
			if (arg.getPlayerId() == fighter.getPlayerId()) {
				fighter.getQuestManager().addCount(QuestManager.CURE, QuestManager.HERO, valueInt);
			}
		}
		return true;
	}

	private void setTroopSyncResult(ArrayList<TroopCard> result, CardBase selfCard, TroopCard card, int sendType) {
		if (card == null) {
			return;
		}
		switch (sendType) {
		case 0:
			return;
		case 1:
			if (card.isAttrChange() || card.isChange()) {
				result.add(card);
			}
			break;
		case 2:
			if ((card.isAttrChange() || card.isChange()) && selfCard.getUid() != card.getUid()) {
				result.add(card);
			}
		}
	}

	public int expression(String expression, SkillArg arg) {
		return expression(expression, arg, null);
	}

	public int expression(String expression) {
		return expression(expression, null, null);
	}
			
	public int expression(String expression, SkillArg arg, CardBase card) {
		
		try {
			int value = Integer.parseInt(expression);
			if (arg != null && arg.getSelfCard().getStatus(DOUBLE_DAMAGE)) {
				return value * 2;
			}
			return value;
		} catch (NumberFormatException e) {
		}

		try {
			if (expression.indexOf(RANDOM) != -1) {
				expression = expression.replace(RANDOM, "");
				String[] split = expression.split("-");
				if (split.length < 2) {
					return 0;
				}
				int min = Integer.parseInt(split[0]);
				int max = Integer.parseInt(split[1]);
				int value = Tools.random(min, max);
				return value;
			}
		} catch (NumberFormatException e) {
			ErrorPrint.print(e);
			return 0;
		}

		if (arg != null) {
			CardBase selfCard = arg.getSelfCard();
			BattleRole self = arg.getFighter();
			GameRoom room = arg.getRoom();
			CardBase trigger = arg.getTrigger();
			String valueArg = ConfigData.skillValueArgModels.get(expression);
			if (Tools.isEmptyString(valueArg)) {
				logger.error("公式转换出错！公式替换参数为空，公式：{}", expression);
				return -1;
			}
			expression = expression.replaceAll("\\[", "");
			expression = expression.replaceAll("\\]", "");
			switch (valueArg) {
			case BattleRole.NEABY_MAX_HP:
				Area area = self.getAreas().get(selfCard.getAreaIndex());
				int maxHp = 0;
				for (TroopCard troop : area.getTroops()) {
					if (troop.getUid() == selfCard.getUid()) {
						continue;
					}
					maxHp = troop.getHp() > maxHp ? troop.getHp() : maxHp;
				}
				expression = expression.replaceAll(BattleRole.NEABY_MAX_HP, String.valueOf(maxHp));
				break;

			case BattleRole.NEABY_MAX_ATK:
				area = self.getAreas().get(selfCard.getAreaIndex());
				int maxAtk = 0;
				for (TroopCard troop : area.getTroops()) {
					if (troop.getUid() == selfCard.getUid()) {
						continue;
					}
					maxAtk = troop.getRealAttack() > maxAtk ? troop.getRealAttack() : maxAtk;
				}
				expression = expression.replaceAll(BattleRole.NEABY_MAX_ATK, String.valueOf(maxAtk));
				break;
				
			case COST_UP_7_DEMIGOD:
				expression = expression.replaceAll(COST_UP_7_DEMIGOD, String.valueOf(self.getCost7Demigod()));
				break;
				
			case COST_TALE:
				expression = expression.replaceAll(COST_TALE, String.valueOf(self.getCostArg().get(COST_TALE)));
				break;
				
			case BattleRole.LAST_DRAW_COST:
				expression = expression.replaceAll(BattleRole.LAST_DRAW_COST, String.valueOf(self.getStatusCount(BattleRole.LAST_DRAW_COST)));
				break;
				
			case BattleRole.ENEMY_HANDCARD_COUNT:
				int enemyId = room.getEnemyId(self.getPlayerId());
				BattleRole enemy = room.getBattleRole(enemyId);
				expression = expression.replaceAll(BattleRole.ENEMY_HANDCARD_COUNT, String.valueOf(enemy.getHandCards().size()));
				break;
				
			case BattleRole.HANDCARD_COUNT:
				expression = expression.replaceAll(BattleRole.HANDCARD_COUNT, String.valueOf(self.getHandCards().size()));
				break;
				
			case BattleRole.TROOP_TEMPLE_COUNT:
				expression = expression.replaceAll(BattleRole.TROOP_TEMPLE_COUNT, String.valueOf(self.getTroopTempleCount()));
				break;
				
			case BattleRole.AWAKE_TROOP_COUNT:
				expression = expression.replaceAll(BattleRole.AWAKE_TROOP_COUNT, String.valueOf(self.getAwakeTroopCount()));
				break;
				
			case BattleRole.ABERRATION_COUNT:
				expression = expression.replaceAll(BattleRole.ABERRATION_COUNT, String.valueOf(self.getAberrationCount()));
				break;
				
			case CardModel.ABERRATION:
				expression = expression.replaceAll(CardModel.ABERRATION, String.valueOf(self.getStatusCount(CardModel.ABERRATION)));
				break;
				
			case PATH_OR_BREACH_AREA:
				expression = expression.replaceAll(PATH_OR_BREACH_AREA, String.valueOf(self.getPathOrBreachArea()));
				break;
				
			case DEMIGOD_TEMPLE:
				expression = expression.replaceAll(DEMIGOD_TEMPLE, String.valueOf(self.getDemigodTemple()));
				break;
				
			case BattleRole.PLAY_CARD_COUNT:
				expression = expression.replaceAll(BattleRole.PLAY_CARD_COUNT, String.valueOf(self.getStatus().get(BattleRole.PLAY_CARD_COUNT)));
				break;
				
			case BattleRole.WARCRY_COUNT:
				expression = expression.replaceAll(BattleRole.WARCRY_COUNT, String.valueOf(self.getWarcryCount()));
				break;
				
			case CardModel.HUMAN:
				expression = expression.replaceAll(CardModel.HUMAN, String.valueOf(room.getTroopCountBySubType(self.getPlayerId(), CardModel.HUMAN)));
				break;
				
			case BattleRole.TURN_DROWER:
				expression = expression.replaceAll(BattleRole.TURN_DROWER, String.valueOf(self.getTurnPlayCardBySubType(CardModel.DROWER)));
				break;
				
			case SELF_AREA_TRAP_COUNT:
				BattleRole role = room.getBattleRole(selfCard.getPlayerId());
				area = role.getArea(selfCard.getAreaIndex());
				if (area != null) {
					ArrayList<TrapCard> trap = area.getTrap();
					expression = expression.replaceAll(SELF_AREA_TRAP_COUNT, String.valueOf(trap.size()));
				}
				break;
				
			case BattleRole.TRAP_COUNT:
				expression = expression.replaceAll(BattleRole.TRAP_COUNT, String.valueOf(self.getTrapCount()));
				break;
				
			case BattleRole.FLIGHT_COUNT:
				expression = expression.replaceAll(BattleRole.FLIGHT_COUNT, String.valueOf(self.getFlightCount()));
				break;
				
			case BattleRole.SPIDER_COUNT:
				expression = expression.replaceAll(BattleRole.SPIDER_COUNT, String.valueOf(self.getSpiderCount()));
				break;
				
			case FIREPACT:
				expression = expression.replaceAll(FIREPACT, String.valueOf(self.getStatusCount(FIREPACT)));
				break;
				
			case WATERPACT:
				expression = expression.replaceAll(WATERPACT, String.valueOf(self.getStatusCount(WATERPACT)));
				break;
				
			case EARTHPACT:
				expression = expression.replaceAll(EARTHPACT, String.valueOf(self.getStatusCount(EARTHPACT)));
				break;
				
			case AIRPACT:
				expression = expression.replaceAll(AIRPACT, String.valueOf(self.getStatusCount(AIRPACT)));
				break;
				
			case BattleRole.AREA_COUNT:
				expression = expression.replaceAll(BattleRole.AREA_COUNT, String.valueOf(self.getAreaCount()));
				break;
				
			case BattleRole.TEMPLE_COUNT:
				expression = expression.replaceAll(BattleRole.TEMPLE_COUNT, String.valueOf(self.getTempleCount()));
				break;
				
			case BattleRole.AIR:
				expression = expression.replaceAll(BattleRole.AIR, String.valueOf(self.getAirTemple()));
				break;
				
			case BattleRole.EARTH:
				expression = expression.replaceAll(BattleRole.EARTH, String.valueOf(self.getEarthTemple()));
				break;
				
			case BattleRole.FIRE:
				expression = expression.replaceAll(BattleRole.FIRE, String.valueOf(self.getFireTemple()));
				break;
				
			case BattleRole.WATER:
				expression = expression.replaceAll(BattleRole.WATER, String.valueOf(self.getWaterTemple()));
				break;
				
			case CardModel.TALE:
				expression = expression.replaceAll(CardModel.TALE, String.valueOf(self.getStatusCount(CardModel.TALE)));
				break;
				
			case CardModel.PLANT:
				expression = expression.replaceAll(CardModel.PLANT, String.valueOf(self.getStatusCount(CardModel.PLANT)));
				break;
				
			case COST_UP_7:
				expression = expression.replaceAll(COST_UP_7, String.valueOf(self.getCardNumberByCost(7)));
				break;
				
			case BattleRole.AMPLIFY:
				if (card != null) {
					expression = expression.replaceAll(BattleRole.AMPLIFY, String.valueOf(card.getAmplify()));
				}
				break;
				
			case BattleRole.TARGET_HP:
				if (card != null) {
					expression = expression.replaceAll(BattleRole.TARGET_HP, String.valueOf(card.getHp()));
				}
				break;

			case BattleRole.TARGET_ATK:
				if (card != null) {
					if (card.getType() == CardModel.TROOP) {
						TroopCard temp = (TroopCard) card;
						expression = expression.replaceAll(BattleRole.TARGET_ATK, String.valueOf(temp.getRealAttack()));
					}
				}
				break;

			case BattleRole.TARGET_COST:
				if (card != null) {
					role = room.getBattleRole(card.getPlayerId());
					expression = expression.replaceAll(BattleRole.TARGET_ATK, String.valueOf(card.getCost(role)));
				}
				break;

			case ATTACKED:
				if (card != null) {
					if (card.getType() == CardModel.TROOP) {
						expression = expression.replaceAll(ATTACKED, String.valueOf(((TroopCard) card).getRealAttack()));
					}
				}
				break;
				
			case TRIGGER_ATK:
				if (card.getType() == CardModel.TROOP) {
					TroopCard temp = (TroopCard) card;
					expression = expression.replaceAll(TRIGGER_ATK, String.valueOf(temp.getRealAttack()));
				}
				break;
				
			case TRIGGER_HP:
				expression = expression.replaceAll(TRIGGER_HP, String.valueOf(trigger.getHp()));
				break;
				
			case TRIGGER_COST:
				BattleRole fighter = room.getBattleRole(trigger.getPlayerId());
				expression = expression.replaceAll(TRIGGER_COST, String.valueOf(trigger.getCost(fighter)));
				break;
				
			case BattleRole.HP:
				if (selfCard.getType() == CardModel.TROOP && self.getHp() > 0) {
					expression = expression.replaceAll(BattleRole.HP, String.valueOf(selfCard.getHp()));
				}
				break;
				
			case DISCARD_COST:
				Integer integer = selfCard.getStatus().get(DISCARD_COST);
				expression = expression.replaceAll(DISCARD_COST, String.valueOf(integer == null ? 0 : integer));
				break;
				
			case COST:
				expression = expression.replaceAll(COST, String.valueOf(selfCard.getCost(self)));
				break;
				
			case KILL_TROOP_COUNT:
				expression = expression.replaceAll(KILL_TROOP_COUNT, String.valueOf(selfCard.getStatusCount(KILL_TROOP_COUNT)));
				break;
				
			case ATTACK:
				if (selfCard.getType() == CardModel.TROOP) {
					expression = expression.replaceAll(ATTACK, String.valueOf(((TroopCard) selfCard).getRealAttack()));
				}
				break;
				
			case LAST_DAMAGE:
				expression = expression.replaceAll(LAST_DAMAGE, String.valueOf(selfCard.getStatusCount(LAST_DAMAGE)));
				break;
				
			case OPP_AREA_TROOP:
				ArrayList<TroopCard> oppAreaTroop = SkillTargetSelect.getInstance().getOppAreaTroop(room, selfCard);
				expression = expression.replaceAll(OPP_AREA_TROOP, String.valueOf(oppAreaTroop.size()));
				break;
				
			case ENEMY_TROOP_COUNT:
				enemyId = room.getEnemyId(selfCard.getPlayerId());
				int size = room.getTroopsByPlayerId(enemyId).size();
				expression = expression.replaceAll(ENEMY_TROOP_COUNT, String.valueOf(size));
				break;
				
			case BattleRole.TROOP_COUNT:
				size = room.getTroopsByPlayerId(selfCard.getPlayerId()).size();
				expression = expression.replaceAll(BattleRole.TROOP_COUNT, String.valueOf(size));
				break;
				
			case ENEMY_STUN_COUNT:
				enemyId = room.getEnemyId(selfCard.getPlayerId());
				enemy = room.getBattleRole(enemyId);
				expression = expression.replaceAll(ENEMY_STUN_COUNT, String.valueOf(enemy.getStunCount()));
				break;
			}
		}

		try {
			int value = Integer.parseInt(expression);
			if (arg != null && arg.getSelfCard().getStatus(DOUBLE_DAMAGE)) {
				return value * 2;
			}
			return value;
		} catch (NumberFormatException e) {
		}

		ScriptEngine jse = new ScriptEngineManager().getEngineByName("JavaScript");
		double eval;
		try {
			eval = Double.parseDouble(jse.eval(expression).toString());
			eval = Math.floor(eval);
			int value = (int) eval;
			if (arg != null && arg.getSelfCard().getStatus(DOUBLE_DAMAGE)) {
				return value * 2;
			}
			return value;
		} catch (ScriptException e) {
			logger.error("公式转换出错！公式为：" + expression);
			ErrorPrint.print(e);
			return -1;
		}
	}

	private boolean isSpellBlock(GameRoom room, CardBase selfCard, TroopCard card) {
		if (selfCard.getStatus(IMMUNE_SPELLBLOCK)) {
			return false;
		}
		if (card.getPlayerId() != selfCard.getPlayerId() && card.isSpellBlock()) {
			card.setStatus(TroopCard.SPELL_BLOCK, false);
			room.troopStatusSync(card, TroopCard.SPELL_BLOCK);
			return true;
		}
		return false;
	}

	private void setEffects(CardBase card, CardBase selfCard, SkillModel model, int value) {
		card.setEffects(selfCard.getUid(), model.Index, getEffect(selfCard, model, value));
	}

	private CardBase getDeckNext(GameRoom room, CardBase card, int type, String state, String subType) {
		BattleRole role = room.getBattleRole(card.getPlayerId());
		ArrayList<CardBase> decks = role.getDecks();
		for (CardBase temp : decks) {
			if (type != 0 && temp.getType() != type) {
				continue;
			}
			if (!Tools.isEmptyString(state) && temp.getStatus(state)) {
				continue;
			}
			if (!Tools.isEmptyString(subType) && !subType.equals(temp.getSubType())) {
				continue;
			}
			deckCardModity(room, role);
			return temp;
		}
		return null;
	}

	private ArrayList<CardBase> getDeckNext(GameRoom room, CardBase card, int count, int type, String state,
			String subType) {
		BattleRole role = room.getBattleRole(card.getPlayerId());
		ArrayList<CardBase> decks = role.getDecks();
		ArrayList<CardBase> result = new ArrayList<>();
		for (CardBase temp : decks) {
			if (type != 0 && temp.getType() != type) {
				continue;
			}
			if (!Tools.isEmptyString(state) && temp.getStatus(state)) {
				continue;
			}
			if (!Tools.isEmptyString(subType) && !subType.equals(temp.getSubType())) {
				continue;
			}
			result.add(temp);
			if (result.size() >= count) {
				break;
			}
		}
		if (result.size() > 0) {
			deckCardModity(room, role);
		}
		return result;
	}

	public boolean initSkillEffect(CardBase card) {
		HashMap<Integer, SkillModel> skill = ConfigData.skillModels.get(card.getRealId());
		if (skill == null || skill.size() == 0) {
			return false;
		}
		boolean isEffect = false;

		for (SkillModel model : skill.values()) {
			if (!Tools.isEmptyString(model.Genius)) {
				if (SkillManager.IN_HAND.equals(model.Genius)) {
					card.setStatus(model.Genius, true);
					card.setStatus(model.Trigger, true);
				} else if (SkillManager.NEED_3_TEMPLES.equals(model.Genius)) {
					card.setStatus(model.Genius, true);
				}
				continue;
			}
			if (!Tools.isEmptyString(model.Trigger)) {
				continue;
			}
			if (model.Cancel == 1) {
				continue;
			}
			if (Tools.isEmptyString(model.Target) || SELF.equals(model.Target)) {
				switch (model.Type) {
				case GUARDIAN:
				case SPEED:
				case FORCE_SHIELD:
				case EXCESS_DAMAGE:
				case SPELL_BLOCK:
					card.setStatus(model.Type, true);
					setEffects(card, card, model, 1);
					isEffect = true;
					break;
				}
			}
		}
		return isEffect;
	}
	
	public boolean haveTarget(SkillArg arg) {
		GameRoom room = arg.getRoom();
		SkillModel model = arg.getModel();
		CardBase selfCard = arg.getSelfCard();
		CardBase triggerOwner = arg.getTriggerOwner();
		String target = model.Target;
		switch (target) {
		case C_HAND_CARD_MINE:
			BattleRole role = room.getBattleRole(triggerOwner.getPlayerId());
			return role.getHandCards().size() > 0;
			
		case C_HAND_CARD_ENEMY:
			BattleRole enemy = room.getBattleRole(room.getEnemyId(triggerOwner.getPlayerId()));
			return enemy.getHandCards().size() > 0;
			
		case C_AREA_MINE:
			role = room.getBattleRole(triggerOwner.getPlayerId());
			return role.getAreaCount() > 0;

		case C_TROOP_MINE:
			ArrayList<TroopCard> troops= room.getTroopsByPlayerId(triggerOwner.getPlayerId(), selfCard, model);
			return troops.size() > 0;
			
		case C_TROOP_ENEMY:
			int enemyId = room.getEnemyId(triggerOwner.getPlayerId());
			troops= room.getTroopsByPlayerId(enemyId);
			return troops.size() > 0;
			
		case C_HERO_OR_TROOP_ENEMY:
			return true;
			
		case C_HAND_CARD_TROOP:
			role = room.getBattleRole(triggerOwner.getPlayerId());
			for (CardBase card : role.getHandCards()) {
				if (card.getType() == CardModel.TROOP) {
					return true;
				}
			}
			break;
			
		case C_TROOP_AREA_SELF:
			Area area = triggerOwner.getArea();
			if (area == null) {
				return false;
			}
			return area.getTroops().size() > 0;
		}
		return false;
	}
	
	public void randomTarget(SkillArg arg) {
		GameRoom room = arg.getRoom();
		SkillModel model = arg.getModel();
		CardBase selfCard = arg.getSelfCard();
		CardBase triggerOwner = arg.getTriggerOwner();
		String type = model.Type;
		String target = model.Target;
		if (selfCard.getTarget().size() != 0) {
			return;
		}
		switch (type) {
		case MOVE_SELF:
			selfCard.getTarget().add(selfCard);
			break;
		}
		switch (target) {
		case C_HAND_CARD_MINE:
			BattleRole role = room.getBattleRole(triggerOwner.getPlayerId());
//			ArrayList<CardBase> handCards = role.getHandCards();
//			if (handCards.size() > 0) {
//				int index = Tools.random(0, handCards.size() - 1);
//				CardBase cardBase = handCards.get(index);
//				selfCard.getTarget().add(cardBase);
//			}
			break;

		case C_AREA_MINE:
			role = room.getBattleRole(triggerOwner.getPlayerId());
			Area area = randomArea(role, null);
			if (area != null) {
				selfCard.getTarget().add(area);
			}
			break;

		case C_TROOP_MINE:
			ArrayList<TroopCard> troops= room.getTroopsByPlayerId(triggerOwner.getPlayerId(), selfCard, model);
			if (troops.size() == 0) {
				return;
			}
			int index = Tools.random(0, troops.size() - 1);
			TroopCard troop = troops.get(index);
			selfCard.getTarget().add(troop);
			moveTarget(arg, troop);
			break;
			
		case C_TROOP_ENEMY:
			int enemyId = room.getEnemyId(triggerOwner.getPlayerId());
			troops= room.getTroopsByPlayerId(enemyId);
			if (troops.size() == 0) {
				return;
			}
			index = Tools.random(0, troops.size() - 1);
			troop = troops.get(index);
			selfCard.getTarget().add(troop);
			moveTarget(arg, troop);
			break;
			
		case C_HERO_OR_TROOP_ENEMY:
			enemyId = room.getEnemyId(triggerOwner.getPlayerId());
			BattleRole enemy = room.getBattleRole(enemyId);
			ArrayList<TroopCard> enemyTroops = room.getTroopsByPlayerId(enemyId);
			IBattleObject obj = room.random(enemyTroops, enemy);
			if (obj != null) {
				selfCard.getTarget().add(obj);
			}
			break;
			
		case C_HAND_CARD_TROOP:
//			role = room.getBattleRole(triggerOwner.getPlayerId());
//			ArrayList<CardBase> list = new ArrayList<>();
//			for (CardBase tempCard : role.getHandCards()) {
//				if (tempCard.getType() == CardModel.TROOP) {
//					list.add(tempCard);
//				}
//			}
//			index = Tools.random(0, list.size() - 1);
//			CardBase card = list.get(index);
//			selfCard.getTarget().add(card);
			break;
			
		case C_TROOP_AREA_SELF:
			area = triggerOwner.getArea();
			if (area == null) {
				return;
			}
			troops = area.getTroops();
			if (area.getTroops().size() > 0) {
				index = Tools.random(0, troops.size() - 1);
				troop = troops.get(index);
				selfCard.getTarget().add(troop);
				moveTarget(arg, troop);
			}
			break;
		}
	}
	
	private void moveTarget(SkillArg arg, CardBase card) {
		if (!MOVE.equals(arg.getModel().Type)) {
			return;
		}
		CardBase selfCard = arg.getSelfCard();
		GameRoom room = arg.getRoom();
		BattleRole role = room.getBattleRole(card.getPlayerId());
		Area area = randomArea(role, card.getArea());
		selfCard.getTarget().add(area);
	}
	
	private Area randomArea(BattleRole role, Area exArea) {
		if (role.getAreaCount() == 0) {
			return null;
		}
		ArrayList<Area> areas = new ArrayList<>();
		for (Area area : role.getAreas()) {
			if (area.getLevel() > 0) {
				if (exArea != null && area.equals(exArea)) {
					continue;
				}
				areas.add(area);
			}
		}
		if (areas.size() == 0) {
			return null;
		}
		if (areas.size() == 1) {
			return areas.get(0);
		}
		int index = Tools.random(0, areas.size() - 1);
		Area area = areas.get(index);
		return area;
	}
	
	private void deckCardModity(GameRoom room, BattleRole role) {
		room.triggerEffect(TriggerManager.DECK_CARD_MODIFY, role.getPlayerId(), 1);
		role.addStatusCount(BattleRole.DECK_CARD_MODIFY_COUNT, 1);
	}
}