package skill;

import java.util.ArrayList;

import config.model.skill.SkillModel;
import module.area.Area;
import module.card.CardBase;
import module.fight.BattleRole;
import module.fight.IBattleObject;
import module.scene.GameRoom;

public class SkillArg {
	
	private GameRoom room;
	
	private int playerId;
	
	private CardBase selfCard;
	
	private CardBase triggerOwner;
	
	private CardBase trigger;
	
	private CardBase attCard;
	
	private IBattleObject defCard;
	
	private Area area;
	
	private BattleRole fighter;
	
	private SkillModel model;
	
	private int sendType;

	private boolean isTrigger = false;
	
	private String eventName;
	
	private ArrayList<CardBase> destoryCards;
	
	public SkillArg() {
	}
	
	public SkillArg(GameRoom room, int playerId, CardBase selfCard, Area area, BattleRole fighter, SkillModel model, int sendType) {
		this.room = room;
		this.playerId = playerId;
		this.selfCard = selfCard;
		this.triggerOwner = selfCard;
		this.area = area;
		this.fighter = fighter;
		this.model = model;
		this.sendType = sendType;
		this.destoryCards = new ArrayList<>();
	}
	
	public GameRoom getRoom() {
		return room;
	}

	public void setRoom(GameRoom room) {
		this.room = room;
	}

	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	public CardBase getSelfCard() {
		return selfCard;
	}

	public void setSelfCard(CardBase selfCard) {
		this.selfCard = selfCard;
	}

	public CardBase getTriggerOwner() {
		return triggerOwner;
	}

	public void setTriggerOwner(CardBase triggerCard) {
		this.triggerOwner = triggerCard;
	}

	public Area getArea() {
		return area;
	}

	public void setArea(Area area) {
		this.area = area;
	}

	public BattleRole getFighter() {
		return fighter;
	}

	public void setFighter(BattleRole fighter) {
		this.fighter = fighter;
	}

	public SkillModel getModel() {
		return model;
	}

	public void setModel(SkillModel model) {
		this.model = model;
	}

	public int getSendType() {
		return sendType;
	}

	public void setSendType(int sendType) {
		this.sendType = sendType;
	}

	public boolean isTrigger() {
		return isTrigger;
	}

	public void setTrigger(boolean isTrigger) {
		this.isTrigger = isTrigger;
	}

	public String getEventName() {
		return eventName;
	}

	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public CardBase getAttCard() {
		return attCard;
	}

	public void setAttCard(CardBase attCard) {
		this.attCard = attCard;
	}

	public ArrayList<CardBase> getDestoryCards() {
		return destoryCards;
	}

	public void setDestoryCards(ArrayList<CardBase> destoryCards) {
		this.destoryCards = destoryCards;
	}
	
	public void addDestoryCard(CardBase card) {
		if (this.destoryCards == null) {
			return;
		}
		if (this.destoryCards.indexOf(card) != -1) {
			return;
		}
		this.destoryCards.add(card);
	}

	public IBattleObject getDefCard() {
		return defCard;
	}

	public void setDefCard(IBattleObject defCard) {
		this.defCard = defCard;
	}

	public CardBase getTrigger() {
		return trigger;
	}

	public void setTrigger(CardBase trigger) {
		this.trigger = trigger;
	}
}
