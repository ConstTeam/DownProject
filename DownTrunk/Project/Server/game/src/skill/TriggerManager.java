package skill;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.ConfigData;
import config.model.card.CardModel;
import config.model.skill.SkillModel;
import message.game.fight.FightMsgSend;
import module.area.Area;
import module.card.ArtifactCard;
import module.card.CardBase;
import module.card.TrapCard;
import module.card.TroopCard;
import module.fight.BattleRole;
import module.scene.GameRoom;
import module.scene.RoomConst;
import util.Tools;

public class TriggerManager implements ITriggerConst {
	
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(TriggerManager.class);
	/** 触发事件<事件类型, <事件所有者Uid, <技能SubId, 事件>>>  */
	private HashMap<String, HashMap<Integer, HashMap<Integer, TriggerEvent>>> triggerEvents;
	
	public TriggerManager() {
		triggerEvents = new HashMap<>();
	}
	
	public ArrayList<TriggerEvent> getTriggerEvent(String triggerEvent) {
		ArrayList<TriggerEvent> list = new ArrayList<>();
		if (triggerEvents.get(triggerEvent) == null) {
			return list;
		}
		Iterator<Entry<Integer, HashMap<Integer, TriggerEvent>>> iterator = triggerEvents.get(triggerEvent).entrySet().iterator();
		while (iterator.hasNext()) {
			HashMap<Integer,TriggerEvent> events = iterator.next().getValue();
			list.addAll(events.values());
		}
		
		Collections.sort(list, new EventComparator());
		return list;
	}

	public boolean addTriggerEvent(String triggerEvent, SkillModel model, CardBase card) {
		return addTriggerEvent(triggerEvent, model, card, card);
	}
			
	public boolean addTriggerEvent(String triggerEvent, SkillModel model, CardBase triggerCard, CardBase card) {
		if (triggerCard.getType() != CardModel.SPELL && SkillManager.getInstance().isAutoTrigger(model)) {
			return false;
		}
		if (this.triggerEvents.get(triggerEvent) == null) {
			this.triggerEvents.put(triggerEvent, new HashMap<>());
		}
		if (this.triggerEvents.get(triggerEvent).get(triggerCard.getUid()) == null) {
			this.triggerEvents.get(triggerEvent).put(triggerCard.getUid(), new HashMap<>());
		}
		if (this.triggerEvents.get(triggerEvent).get(triggerCard.getUid()).get(model.SubID) == null) {
			TriggerEvent event = new TriggerEvent(triggerCard, card, model);
			this.triggerEvents.get(triggerEvent).get(triggerCard.getUid()).put(model.SubID, event);
			return true;
		}
		return false;
	}
	
	public void delTriggerEvent(String triggerEvent, int cardUid) {
		if (this.triggerEvents.get(triggerEvent) == null) {
			return;
		}
		if (this.triggerEvents.get(triggerEvent).get(cardUid) == null) {
			return;
		}
		this.triggerEvents.get(triggerEvent).remove(cardUid);
		if (this.triggerEvents.get(triggerEvent).size() == 0) {
			this.triggerEvents.remove(triggerEvent);
		}
	}

	public boolean addTriggerEvent(String triggerEvent, CardBase card) {
		HashMap<Integer, SkillModel> skill = ConfigData.skillModels.get(card.getRealId());
		if (skill == null || skill.size() == 0) {
			return false;
		}
		for (SkillModel model : skill.values()) {
			if (triggerEvent.equals(model.Trigger)) {
				return addTriggerEvent(triggerEvent, model, card, card);
			}
		}
		return false;
	}
	
	public void addTriggerEvent(CardBase card) {
		String triggerEvent = card.getGenius();
		if (Tools.isEmptyString(triggerEvent)) {
			return;
		}
		switch (triggerEvent) {
		case PLAY_CARD:
		case PLANT:
		case TRIGGER_TRAP:
		case DRAW:
		case DECK_CARD_MODIFY:
		case HAND_CARD_CHANGE:
		case DEATH:
		case MOVE:
			break;
		case SkillManager.COST_HERO_HP:
			triggerEvent = MINE_HERO_LIFE;
			break;
		default:
			return;
		}
		HashMap<Integer, SkillModel> skill = ConfigData.skillModels.get(card.getRealId());
		if (skill == null || skill.size() == 0) {
			return;
		}
		for (SkillModel model : skill.values()) {
			if (triggerEvent.equals(model.Trigger)) {
				addTriggerEvent(triggerEvent, model, card, card);
			}
		}
		return;
	}
	
	public void addTriggerEvent(SkillModel model, String triggerEvent, CardBase card) {
		if (card.getType() == CardModel.SPELL) {
			return;
		}
		int i = model.Value.indexOf("AreaCount");
		if (i != -1) {
			addTriggerEvent(triggerEvent, model, card);
		}
	}	
	
	public void delTriggerEvent(CardBase card) {
		Iterator<Entry<String, HashMap<Integer, HashMap<Integer, TriggerEvent>>>> iterator = triggerEvents.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, HashMap<Integer, HashMap<Integer, TriggerEvent>>> next = iterator.next();
			HashMap<Integer, HashMap<Integer, TriggerEvent>> map = next.getValue();
			map.remove(card.getUid());
			if (map.size() == 0) {
				iterator.remove();
			}
		}
	}
	
	public void triggerEffect(GameRoom room, String eventName, int playerId, Object triggerObj, int sendType) {
		CardBase trigger = null;
		Area triggerArea = null;
		if (triggerObj != null) {
			if (triggerObj instanceof CardBase) {
				trigger = (CardBase)triggerObj;
			} else if (triggerObj instanceof Area) {
				triggerArea = (Area) triggerObj;
			}
		}
		sendType = 1;
		ArrayList<TriggerEvent> triggerEvent = this.getTriggerEvent(eventName);
		if (triggerEvent != null && triggerEvent.size() > 0) {
			CardBase lastTriggerCard = null;
			Iterator<TriggerEvent> iterator = triggerEvent.iterator();
			while (iterator.hasNext()) {
				TriggerEvent event = iterator.next();
				CardBase triggerCard = event.getTriggerCard();
				if (lastTriggerCard != null && lastTriggerCard.getUid() != triggerCard.getUid()) {
					eventEnd(room, lastTriggerCard, trigger);
					lastTriggerCard = null;
				}
				if (triggerCard.isStun()) {
					continue;
				}
				if (triggerCard.getType() == CardModel.TRAP) {
					if (triggerCard.getStatus(TrapCard.TRIGGERING)) {
						continue;
					}
					if (room.checkPlayer(triggerCard.getPlayerId())) {
						continue;
					}
					if (triggerCard.getPlayerId() != playerId) {
						continue;
					}
				} else {
					if (!TARGET.equals(eventName) && triggerCard.getPlayerId() != playerId) {
						continue;
					}
				}
				switch (eventName) {
				case DRAW:
					if (!room.checkPlayer(triggerCard.getPlayerId())) {
						continue;
					}
					break;
				case PLAY_CARD:
					if (trigger == null) {
						continue;
					}
					if (trigger.getUid() == triggerCard.getUid()) {
						if (!SkillManager.SELF_ANYWHERE.equals(event.getModel().Target)) {
							continue;
						}
					}
					String genius = event.getModel().Genius;
					if (!Tools.isEmptyString(genius)) {
						if (genius.indexOf("SubType-") != -1) {
							if (Tools.isEmptyString(trigger.getSubType())) {
								continue;
							}
							if (genius.indexOf(trigger.getSubType()) == -1 ) {
								continue;
							}
						}
						if (genius.indexOf("Id-") != -1) {
							if (genius.indexOf(trigger.getId()) == -1) {
								continue;
							}
						}
						if (genius.indexOf(TROOP) != -1) {
							if (trigger.getType() != CardModel.TROOP) {
								continue;
							}
						}
						if (genius.indexOf(COST_UP_7) != -1) {
							BattleRole role = room.getBattleRole(trigger.getPlayerId());
							if (trigger.getCost(role) < 7) {
								continue;
							}
						}
					}
					break;
				case PLANT:
				case TRAP:
				case TALE:
					if (!eventName.equals(trigger.getSubType())) {
						continue;
					}
					break;
				case FIND_TALE:
					if (!TALE.equals(trigger.getSubType())) {
						continue;
					}
					break;
				case SPELL:
				case SPELL_AFTER:
					if (trigger.getType() != CardModel.SPELL && trigger.getType() != CardModel.TRAP) {
						continue;
					}
					if (trigger.getPlayerId() != playerId && triggerCard.getType() != CardModel.TRAP) {
						continue;
					}
					break;
				case AREA_TROOP:
					if (trigger.getUid() == triggerCard.getUid()) {
						continue;
					}
					if (trigger.getPlayerId() != triggerCard.getPlayerId()) {
						continue;
					}
					if (trigger.getAreaIndex() != triggerCard.getAreaIndex()) {
						continue;
					}
					break;
				case TARGET:
					if (trigger.getUid() != triggerCard.getUid()) {
						continue;
					}
					break;
				case DAMAGE_ALIVE:
					if (trigger.getUid() != triggerCard.getUid()) {
						continue;
					}
					break;
				case SELF_AREA_LV_UP:
					if (triggerCard.getPlayerId() != triggerArea.getPlayerId()) {
						continue;
					}
					break;
				case HAND_CARD_CHANGE:
					if (triggerCard.getUid() == trigger.getUid()) {
						sendType = 0;
					}
					break;
				}
				
				if (!Tools.isEmptyString(event.getModel().Trigger)) {
					switch (event.getModel().Trigger) {
					case PLAY_CARD:
						if (trigger == null) {
							continue;
						}
						if (trigger.getUid() == triggerCard.getUid()) {
							if (!SkillManager.SELF_ANYWHERE.equals(event.getModel().Target)) {
								continue;
							}
						}
						if (Tools.isNumber(event.getModel().Genius) && !event.getModel().Genius.equals(trigger.getId())) {
							continue;
						}
						break;
					case PLANT:
					case TRAP:
					case TALE:
						if (trigger == null) {
							continue;
						}
						if (!event.getModel().Trigger.equals(trigger.getSubType())) {
							continue;
						}
						break;
					case FIND_TALE:
						if (trigger == null) {
							continue;
						}
						if (!TALE.equals(trigger.getSubType())) {
							continue;
						}
						break;
					case SPELL:
						if (trigger == null) {
							continue;
						}
						if (trigger.getType() != CardModel.SPELL && trigger.getType() != CardModel.TRAP) {
							continue;
						}
						break;
					case AREA_TROOP_DEATH:
						if (trigger.getUid() == triggerCard.getUid()) {
							continue;
						}
						if (trigger.getPlayerId() != triggerCard.getPlayerId()) {
							continue;
						}
						if (trigger.getAreaIndex() != triggerCard.getAreaIndex()) {
							continue;
						}
						break;
					case FIRST:
						BattleRole role = room.getBattleRole(trigger.getPlayerId());
						if (!role.isFirst()) {
							continue;
						}
						if (triggerCard.getType() == CardModel.TRAP) {
							if (trigger.getPlayerId() == triggerCard.getPlayerId()) {
								continue;
							} 
						} else {
							if (trigger.getPlayerId() != triggerCard.getPlayerId()) {
								continue;
							}
						}
						break;
					case SECOND:
						role = room.getBattleRole(playerId);
						if (triggerCard.getType() == CardModel.TRAP) {
							role = room.getBattleRole(room.getEnemyId(triggerCard.getPlayerId()));
						}
						if (!role.isSecond()) {
							continue;
						}
						break;
					case FIRST_SPELL:
						role = room.getBattleRole(trigger.getPlayerId());
						if (!role.isFirstSpellCard()) {
							continue;
						}
						if (trigger.getType() != CardModel.SPELL && trigger.getType() != CardModel.TRAP) {
							continue;
						}
						if (triggerCard.getType() == CardModel.TRAP) {
							if (trigger.getPlayerId() == triggerCard.getPlayerId()) {
								continue;
							} 
						} else {
							if (trigger.getPlayerId() != triggerCard.getPlayerId()) {
								continue;
							}
						}
						break;
					case FIRST_TROOP:
						role = room.getBattleRole(trigger.getPlayerId());
						if (!role.isFirstTroopCard()) {
							continue;
						}
						if (trigger.getType() != CardModel.TROOP) {
							continue;
						}
						if (triggerCard.getType() == CardModel.TRAP) {
							if (trigger.getPlayerId() == triggerCard.getPlayerId()) {
								continue;
							} 
						} else {
							if (trigger.getPlayerId() != triggerCard.getPlayerId()) {
								continue;
							}
						}
						break;
					case SkillManager.OPP_AREA_TROOP:
						if (trigger.getPlayerId() == triggerCard.getPlayerId()) {
							continue;
						}
						Area tempArea = triggerCard.getArea() == null ? triggerCard.getOldArea() : triggerCard.getArea();
						if (trigger.getAreaIndex() != SkillManager.getInstance().getOppoAreaIndex(tempArea.getIndex())) {
							continue;
						}
						break;
					case SkillManager.SPIDER_IN_AREA:
						if (trigger.getUid() == triggerCard.getUid()) {
							continue;
						}
						if (trigger.getPlayerId() != triggerCard.getPlayerId()) {
							continue;
						}
						if (trigger.getAreaIndex() != triggerCard.getAreaIndex()) {
							continue;
						}
						if (!trigger.getId().equals("1001")) {
							continue;
						}
						break;
					case GUARDIAN_IN_AREA:
						if (!trigger.getStatus(TroopCard.GUARDIAN)) {
							continue;
						}
						break;
					case WARCRY_IN_AREA:
						if (!trigger.getStatus(WARCRY)) {
							continue;
						}
						break;
					case DEATH:
						if (trigger.getPlayerId() != triggerCard.getPlayerId()) {
							continue;
						}
						String genius = event.getModel().Genius;
						if (Tools.isNumber(genius)) {
							if (!genius.equals(trigger.getId())) {
								continue;
							}
						}
						break;
					}
				}
				
				if (!Tools.isEmptyString(event.getModel().Genius)) {
					String genius = event.getModel().Genius;
					switch (genius) {
					case SECOND:
						BattleRole role = room.getBattleRole(playerId);
						if (!role.isSecond()) {
							room.getTriggerManager().addTriggerEvent(TriggerManager.SECOND, event.getModel(), event.getTriggerCard());
							continue;
						}
						break;
					case SkillManager.SELF:
						if (trigger.getUid() != triggerCard.getUid()) {
							continue;
						}
						break;
					case SkillManager.SELF_ATK_LE_3:
						if (trigger.getUid() != triggerCard.getUid()) {
							continue;
						}
						if (trigger.getType() != CardModel.TROOP) {
							continue;
						}
						TroopCard tempTroop = (TroopCard) trigger;
						if (tempTroop.getRealAttack() > 3) {
							continue;
						}
						break;
					case SkillManager.SELF_ATK_LE_4:
						if (trigger.getUid() != triggerCard.getUid()) {
							continue;
						}
						if (trigger.getType() != CardModel.TROOP) {
							continue;
						}
						tempTroop = (TroopCard) trigger;
						if (tempTroop.getRealAttack() > 4) {
							continue;
						}
						break;
					}
				}
				SkillModel model = event.getModel();
				
				CardBase card = event.getCard();
				if (card == null) {
					if (triggerCard.getType() == CardModel.ARTIFACT) {
						card = triggerCard;
					} else {
						card = trigger;
					}
					if (model.Target.equals(SkillManager.SELF)) {
						card = triggerCard;
					}
				}
				if (TriggerManager.AREA_TROOP.equals(eventName)) {
					if (trigger.getAreaIndex() != card.getAreaIndex()) {
						continue;
					}
				}
				switch (model.Type) {
				case SkillManager.SPEED:
					if (!room.checkPlayer(card.getPlayerId())) {
						continue;
					}
					break;
				}
						
				BattleRole fighter = room.getBattleRole(card.getPlayerId());
				if (triggerCard.getType() == CardModel.TRAP) {
					if (model.Target.indexOf(SkillManager.SELF) != -1 || model.Target.equals(SkillManager.SELF_HERO)) {
						fighter = room.getBattleRole(triggerCard.getPlayerId());
					} else {
						fighter = room.getBattleRole(room.getEnemyId(triggerCard.getPlayerId()));
					}
				}
				Area area = null;
				if (card.getArea() != null) {
					area = card.getArea();
				} else if (triggerCard.getArea() != null) {
					area = triggerCard.getArea();
				}
				if (triggerCard.getStatus(ArtifactCard.TRUN)) {
					if (!triggerCard.getStatus(ArtifactCard.TRUN_COUNT)) {
						continue;
					}
				}
				if (triggerCard.getType() == CardModel.TRAP) {
					switch (model.Type) {
					case SkillManager.SUMMON:
					case SkillManager.FILL_ONE:
					case SkillManager.FILL_FULL:
						if (model.Target.equals(SkillManager.TRIGGER_AREA)) {
							area = trigger.getArea() == null ? trigger.getOldArea() : trigger.getArea();
						} else {
							area = triggerCard.getArea() == null ? triggerCard.getOldArea() : triggerCard.getArea();
						}
						break;
					}
					if (model.Target.indexOf(SkillManager.SELF_AREA) != -1) {
						area = triggerCard.getArea() == null ? triggerCard.getOldArea() : triggerCard.getArea();
					}
				} else if (eventName.equals(TRIGGER_TRAP) && (model.Type.indexOf(SkillManager.SUMMON) != -1 || model.Type.indexOf(SkillManager.FILL) != -1)) {
					fighter = room.getBattleRole(trigger.getPlayerId());
					area = trigger.getArea() == null ? trigger.getOldArea() : trigger.getArea();
					card = trigger;
				}
				try {
					if (triggerCard.getType() == CardModel.TRAP) {
						triggerCard.setStatus(TrapCard.TRIGGERING, true);
					}
					SkillArg arg = new SkillArg(room, triggerCard.getPlayerId(), card, area, fighter, model, sendType);
					arg.setTriggerOwner(triggerCard);
					arg.setTrigger(true);
					arg.setAttCard(trigger);
					arg.setDefCard(trigger == null? null : trigger.getAttackTarget());
					arg.setTrigger(trigger);
					arg.setEventName(eventName);
					arg.setSendType(sendType);
					boolean result = false;
					switch (eventName) {
					case DISCARDS:
						if (triggerCard.getType() == CardModel.SPELL) {
							this.delTriggerEvent(eventName, triggerCard.getUid());
						}
						arg.setSelfCard(triggerCard);
						arg.setTriggerOwner(trigger);
						result = SkillManager.getInstance().triggerEffect(arg);
						break;
					case TALE:
					case AREA_TROOP:
						if (triggerCard.getType() != CardModel.ARTIFACT && model.Target.indexOf(SkillManager.SELF) != -1 || model.Target.indexOf(SkillManager.OPP_AREA_RANDOM_TROOP) != -1) {
							arg.setSelfCard(triggerCard);
							arg.setTriggerOwner(triggerCard);
							result = SkillManager.getInstance().triggerEffect(arg);
						} else {
							arg.setSelfCard(trigger);
							arg.setTriggerOwner(triggerCard);
							result = SkillManager.getInstance().triggerEffect(arg);
						}
						break;
					case SPELL: 
						if (triggerCard.getType() == CardModel.TRAP) {
							arg.setSelfCard(triggerCard);
							arg.setTriggerOwner(triggerCard);
							result = SkillManager.getInstance().triggerEffect(arg);
						} else {
							result = SkillManager.getInstance().triggerEffect(arg);
						}
						break;
					default:
						result = SkillManager.getInstance().triggerEffect(arg);
						break;
					}
					if (result) {
						// 移除触发事件
						if (triggerCard.getStatus(ArtifactCard.TRUN)) {
							triggerCard.setStatus(ArtifactCard.TRUN_COUNT, false);
						}
						lastTriggerCard = triggerCard;
					}
				} finally {
					triggerCard.setStatus(TrapCard.TRIGGERING, false);
				}
			}
			if (lastTriggerCard != null) {
				eventEnd(room, lastTriggerCard, trigger);
				lastTriggerCard = null;
			}
		}
	}
	
	private void eventEnd(GameRoom room, CardBase triggerCard, CardBase trigger) {
		if (triggerCard.getType() == CardModel.TRAP) {
			TrapCard trap = (TrapCard) triggerCard;
			if (!trap.isDead()) {
				TrapTriggerManager.getInstance().triggerTrap(room, trap, trigger);
			}
		} else if (triggerCard.getType() == CardModel.ARTIFACT) {
			room.artiTriggerSync(triggerCard);
		}
	}
	
	public boolean isNeedTargetSelect(CardBase card, SkillModel model) {
		if (model.Target.indexOf("C_") == -1) {
			return false;
		}
		if (card != null && card.getTarget().size() != 0) {
			return false;
		}
		if (model.Trigger.equals(WARCRY) && model.SubID == 1) {
			return false;
		}
		return true;
	}

	private void triggerEffect(String type, GameRoom room, CardBase card, BattleRole fighter, Area area) {
		if (fighter.isEffectCard(card.getUid())) {
			return;
		}
		switch (type) {
		case START:
			startEffect(room, card, fighter, area, 1);
			break;
		case END:
			endEffect(room, card, fighter, area);
			break;
		case ENCHANT:
			enchantEffect(room, card, fighter, area);
			break;
		}
	}

	public void startEffect(GameRoom room, CardBase card, BattleRole fighter, Area area, int type) {
		fighter.setEffectCard(card.getUid());
		boolean send = false;
		if (card.getStatus(START)) {
			HashMap<Integer, SkillModel> skill = ConfigData.skillModels.get(card.getRealId());
			for (SkillModel model : skill.values()) {
				if (START.equals(model.Trigger)) {
					SkillArg arg = new SkillArg(room, card.getPlayerId(), card, area, fighter, model, 1);
					if (SkillManager.getInstance().trigger(arg)) {
						if (isNeedTargetSelect(card, model)) {
							if (room.getPlayState() != RoomConst.PLAY_STATE_AUTO) {
								if (SkillManager.getInstance().haveTarget(arg)) {
									fighter.setStatus(BattleRole.TARGET_SELECT, true);
									fighter.setInterruptSkillArg(arg);
									FightMsgSend.targetSelectRequest(room.getSession(card.getPlayerId()), card, model.Type, model.Target);
								}
								return;
							}
						}
						if (type == 1 || (isNeedTargetSelect(null, model) || model.Genius.equals(SkillManager.EFFECT_SUCC))) {
							if (send == false && card.getType() == CardModel.ARTIFACT && card.getArea() != null) {
								room.artiTriggerSync(card);
							}
							send = SkillManager.getInstance().effect(arg);
						}
					}
				}
			}
			card.getTarget().clear();
		}
		if (send) {
			if (card.getType() == CardModel.TROOP) {
				TroopCard troop = (TroopCard) card;
				if (troop.getArea() != null) {
					room.startSync(troop);
				}
			}
		}
	}
	
	public void endEffect(GameRoom room, CardBase card, BattleRole fighter, Area area) {
		if (card.getStatus(END)) {
			HashMap<Integer, SkillModel> skill = ConfigData.skillModels.get(card.getRealId());
			for (SkillModel model : skill.values()) {
				if (END.equals(model.Trigger)) {
					SkillArg arg = new SkillArg(room, card.getPlayerId(), card, area, fighter, model, 1);
					SkillManager.getInstance().triggerEffect(arg);
				}
			}
		}
	}
	
	private void enchantEffect(GameRoom room, CardBase card, BattleRole fighter, Area area) {
		TroopCard troop = (TroopCard) card;
		boolean isEnchant = false;
		if (troop.getStatus(ENCHANT) && (troop.isEnchant())) {
			HashMap<Integer, SkillModel> skill = ConfigData.skillModels.get(troop.getRealId());
			for (SkillModel model : skill.values()) {
				if (ENCHANT.equals(model.Trigger)) {
					SkillArg arg = new SkillArg(room, troop.getPlayerId(), troop, area, fighter, model, 1);
					if (SkillManager.getInstance().triggerEffect(arg)) {
						isEnchant = true;
					}
				}
			}
		}
		if (isEnchant) {
			room.troopEnchantSync(troop);
		}
	}
	
	public void breachEffect(GameRoom room, CardBase card, BattleRole fighter, Area area) {
		room.setRoleState(fighter.getPlayerId(), BattleRole.BREACH);
		boolean isBreak = false;
		if (card.getStatus(BREACH)) {
			HashMap<Integer, SkillModel> skill = ConfigData.skillModels.get(card.getRealId());
			for (SkillModel model : skill.values()) {
				if (BREACH.equals(model.Trigger)) {
					SkillArg arg = new SkillArg(room, card.getPlayerId(), card, area, fighter, model, 1);
					if (SkillManager.getInstance().trigger(arg)) {
						if (isNeedTargetSelect(card, model)) {
							if (SkillManager.getInstance().haveTarget(arg)) {
								fighter.setInterruptSkillArg(arg);
								FightMsgSend.targetSelectRequest(room.getSession(card.getPlayerId()), card, model.Type, model.Target);
							}
							return;
						}
						isBreak = SkillManager.getInstance().effect(arg);
					}
				}
			}
		}
		if (isBreak) {
			card.setStatus(BREACH, false);
			// TODO 同步破坏触发效果
		}
		room.clearRoleState(fighter.getPlayerId());
	}
	
	public void deathcryEffectSync(GameRoom room, CardBase card, BattleRole fighter, Area area) {
		if (card.getStatus(DEATHCRY)) {
			HashMap<Integer, SkillModel> skill = ConfigData.skillModels.get(card.getRealId());
			for (SkillModel model : skill.values()) {
					if (DEATHCRY.equals(model.Trigger)) {
						SkillArg arg = new SkillArg(room, card.getPlayerId(), card, area, fighter, model, 1);
						if (SkillManager.getInstance().trigger(arg)) {
							room.troopDeathcrySync(card);
							return;
						}
					}
			}
		}
	}

	public void deathcryEffect(GameRoom room, CardBase card, BattleRole fighter, Area area) {
		room.setRoleState(fighter.getPlayerId(), BattleRole.DEATHCRY);
		if (card.getStatus(DEATHCRY)) {
			HashMap<Integer, SkillModel> skill = ConfigData.skillModels.get(card.getRealId());
			for (SkillModel model : skill.values()) {
					if (DEATHCRY.equals(model.Trigger)) {
						SkillArg arg = new SkillArg(room, card.getPlayerId(), card, area, fighter, model, 1);
						SkillManager.getInstance().triggerEffect(arg);
					}
			}
		}
		room.clearRoleState(fighter.getPlayerId());
	}
	
	public void start(GameRoom room, BattleRole fighter) {
		
		for (Area area : fighter.getAreas()) {

			for (CardBase card : fighter.getHandCards()) {
				if (card.getStatus(SkillManager.IN_HAND)) {
					triggerEffect(START, room, card, fighter, area);
				}
			}
			
			for (TroopCard troop : area.getTroops()) {
				triggerEffect(START, room, troop, fighter, area);
				if (fighter.getInterruptSkillArg() != null) {
					return;
				}
			}
			
			for (ArtifactCard artifact : area.getArtifact()) {
				triggerEffect(START, room, artifact, fighter, area);
				if (fighter.getInterruptSkillArg() != null) {
					return;
				}
			}
		}
	}
	
	public void end(GameRoom room, BattleRole fighter) {
		
		for (Area area : fighter.getAreas()) {
			for (TroopCard troop : area.getTroops()) {
				triggerEffect(END, room, troop, fighter, area);
				triggerEffect(ENCHANT, room, troop, fighter, area);
				fighter.setEffectCard(troop.getUid());
				room.findCardSync(fighter);
				if (fighter.getInterruptSkillArg() != null) {
					return;
				}
			}
			
			for (ArtifactCard artifact : area.getArtifact()) {
				triggerEffect(END, room, artifact, fighter, area);
				room.findCardSync(fighter);
				if (fighter.getInterruptSkillArg() != null) {
					return;
				}
			}
			
			for (CardBase card : fighter.getHandCards()) {
				if (card.getStatus(SkillManager.IN_HAND)) {
					triggerEffect(END, room, card, fighter, area);
				}
			}
		}
		ArrayList<TriggerEvent> triggerEvent = this.getTriggerEvent(END);
		if (triggerEvent != null && triggerEvent.size() > 0) {
			Iterator<TriggerEvent> iterator = triggerEvent.iterator();
			while (iterator.hasNext()) {
				TriggerEvent event = iterator.next();
				CardBase triggerCard = event.getTriggerCard();
				if (triggerCard.getPlayerId() != fighter.getPlayerId()) {
					continue;
				}
				triggerEffect(END, room, triggerCard, fighter, null);
				iterator.remove();
			}
		}
		BattleRole enemy = room.getBattleRole(room.getEnemyId(fighter.getPlayerId()));
		for (Area area : enemy.getAreas()) {
			for (TrapCard trap : area.getTrap()) {
				triggerEffect(END, room, trap, enemy, area);
			}
		}
	}
	
	public void warcry(GameRoom room, CardBase card, BattleRole fighter, Area area) {
		boolean send = true;
		String type = null;
		String target = null;
		SkillArg arg = null;
		if (card.getStatus(WARCRY)) {
			HashMap<Integer, SkillModel> skill = ConfigData.skillModels.get(card.getRealId());
			for (SkillModel model : skill.values()) {
				if (WARCRY.equals(model.Trigger)) {
					arg = new SkillArg(room, card.getPlayerId(), card, area, fighter, model, 1);
					if (SkillManager.getInstance().trigger(arg)) {
						if (isNeedTargetSelect(card, model)) {
							if (SkillManager.getInstance().haveTarget(arg)) {
								target = model.Target;
								type = model.Type;
							}
							continue;
						}
						if (send) {
							room.warcrySync(card);
							fighter.addStatusCount(BattleRole.WARCRY_COUNT, 1);
							send = false;
						}
						SkillManager.getInstance().effect(arg);
					}
				}
			}
			if (!Tools.isEmptyString(target)) {
				fighter.setStatus(BattleRole.TARGET_SELECT, true);
				fighter.setStatus(BattleRole.WARCRY_INTERRUPT, true);
				fighter.setInterruptSkillArg(arg);
				FightMsgSend.targetSelectRequest(room.getSession(card.getPlayerId()), card, type, target);
			}
		}
	}
	
	public void autoWarcry(GameRoom room, CardBase card, BattleRole fighter, Area area) {
		if (card.getStatus(WARCRY)) {
			HashMap<Integer, SkillModel> skill = ConfigData.skillModels.get(card.getRealId());
			for (SkillModel model : skill.values()) {
				if (WARCRY.equals(model.Trigger)) {
					SkillArg arg = new SkillArg(room, card.getPlayerId(), card, area, fighter, model, 1);
					if (SkillManager.getInstance().trigger(arg)) {
						if (isNeedTargetSelect(null, model)) {
							SkillManager.getInstance().effect(arg);
						}
					}
				}
			}
		}
	}
	
	public void autoPlayCard(SkillArg arg, CardBase card) {
		HashMap<Integer, SkillModel> skill = ConfigData.skillModels.get(card.getRealId());
		for (SkillModel model : skill.values()) {
			if (SkillManager.getInstance().isAutoTrigger(model)) {
				continue;
			}
			if (SkillManager.getInstance().trigger(arg)) {
				if (isNeedTargetSelect(null, model)) {
					SkillManager.getInstance().effect(arg);
				}
			}
		}
	}
	
	public String drawChange(GameRoom room, BattleRole fighter) {
		for (Area area : fighter.getAreas()) {
			for (ArtifactCard artifact : area.getArtifact()) {
				String drawChange = drawChange(room, artifact, fighter);
				if (drawChange != null) {
					return drawChange;
				}
			}
		}
		return null;
	}
	
	public String drawChange(GameRoom room, CardBase card, BattleRole fighter) {
		if (card.getStatus(ArtifactCard.DRAW_CHANGE)) {
			HashMap<Integer, SkillModel> skill = ConfigData.skillModels.get(card.getRealId());
			for (SkillModel model : skill.values()) {
				if (ArtifactCard.DRAW_CHANGE.equals(model.Type)) {
					room.artiTriggerSync(card);
					return model.Target;
				}
			}
		}
		return null;
	}
	
	public boolean isCureToReduce(BattleRole fighter) {
		for (Area area : fighter.getAreas()) {
			for (ArtifactCard artifact : area.getArtifact()) {
				if (artifact.getStatus(ArtifactCard.CURE_TO_REDUCE)) {
					return true;
				}
			}
		}
		return false;
	}
}
