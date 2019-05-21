package skill;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.ConfigData;
import config.model.skill.SkillModel;
import module.card.CardBase;
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
	
}
