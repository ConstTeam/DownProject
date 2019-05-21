package module.fight;

import java.util.ArrayList;
import java.util.HashMap;

import config.model.card.CardModel;
import module.area.Area;
import module.card.ArtifactCard;
import module.card.CardBase;
import module.card.FindCard;
import module.card.TrapCard;
import module.card.TroopCard;
import quest.QuestManager;
import skill.Effect;
import skill.SkillArg;
import skill.SkillManager;
import skill.TriggerManager;

public class BattleRole extends BattleRoleBase implements IBattleRoleStatus {

	/** 主符文 */
	private int mainRune;
	/** 升级所需消耗 */
	private int lvUpResource;
	/** 本局唯一id */
	private int uid;
	/** 玩家昵称 */
	private String nickname;
	/** 头像 */
	private String icon;
	/** 天梯排名 */
	private int rank;
	/** 当前状态 */
	private int state;
	/** 所在房间Id */
	private int roomId;
	/** 是否为机器人 */
	private boolean robot = false;
	
	private SkillArg interruptSkillArg;
	
	private CardBase inputCard;
	
	/** 状态 */
	private HashMap<String, Integer> status = new HashMap<>();
	/** 技能数值用参数 */
	private HashMap<String, Integer> arg = new HashMap<>();
	/** 当前可撤消的效果（属性变化） HashMap<施法者uid, HashMap<技能SubID, Effect>> */
	private HashMap<Integer, HashMap<Integer, Effect>> effects;
	/** Cost */
	private HashMap<String, Integer> costArg = new HashMap<>();
	/** playCards */
	private HashMap<String, Boolean> playCards = new HashMap<>();
	/** firstFindCardId */
	private HashMap<Integer, String> firstFind = new HashMap<>();
	/** startCards */
	private HashMap<Integer, Integer> effectCards = new HashMap<>();
	/** 替换卡牌 */
	private HashMap<String, String> replaceCard = new HashMap<>();
	
	/** 手牌 ArrayList<Card> */
	private ArrayList<CardBase> handCards;
	/** 区域 ArrayList<Area> */
	private ArrayList<Area> areas;
	/** 牌堆 ArrayList<Card> */
	private ArrayList<CardBase> decks;
	/** 坟场 ArrayList<Card> */
	private ArrayList<CardBase> discards;
	
	private ArrayList<FindCard> findCards;
	
	private ArrayList<CardBase> attackLimitSpell;
	
	private String dealCardId;
	
	private QuestManager questManager;

	public BattleRole(int playerId, String nickname, int hp, int uid) {
		this.setPlayerId(playerId);
		handCards = new ArrayList<>();
		areas = new ArrayList<>(AREA_MAX_INDEX + 1);
		for (int i = 0; i <= AREA_MAX_INDEX; i++) {
			areas.add(new Area(i, playerId));
		}
		decks = new ArrayList<>();
		discards = new ArrayList<>();
		findCards = new ArrayList<>();
		attackLimitSpell = new ArrayList<>();
		effects = new HashMap<>();
		this.setUid(uid);
		this.setHp(hp);
		this.setNickname(nickname);
		setConstructTrapSelect(INIT_CONSTRUCT_TRAP_SELECT);
		setQuestManager(QuestManager.init(playerId));
	}
	
	public void addHandCard(CardBase card) {
		this.getHandCards().add(card);
	}
	
	public void addHandCard(CardBase card, int index) {
		this.getHandCards().add(index, card);
	}
	
	public void removeHandCard(CardBase card) {
		if (card == null) {
			this.getHandCards().clear();
			return;
		}
		if (this.getHandCards().indexOf(card) == -1) {
			return;
		}
		this.getHandCards().remove(card);
	}

	public void addHp(int defence) {
		if (defence < 0) {
			addStatusCount(DAMAGE, defence);
		}
		defence += this.getRealHp();
		this.setHp(defence);
	}

	public void replenishedResource() {
		this.setResource(this.getReplResource());
	}

	public void addReplResource(int resource) {
		this.setReplResource(resource + this.getReplResource());
		resource += this.getResource();
		resource = resource > this.getReplResource() ? this.getReplResource() : resource;
		this.setResource(resource);
	}

	public void addReplResourceOnly(int resource) {
		this.setReplResource(resource + this.getReplResource());
		if (this.getReplResource() < 0) {
			this.setReplResource(0);
		}
	}
	
	public void addResource() {
		this.setResource(this.getResource() + 1);
	}

	public void addResource(int resource) {
		if (resource > 0) {
			resource += this.getResource();
			resource = resource > this.getReplResource() ? this.getReplResource() : resource;
		} else {
			resource += this.getResource();
		}
		this.setResource(resource);
	}

	public Area getArea(int index) {
		if (index < 0) {
			return null;
		}
		if (index >= areas.size()) {
			return null;
		}
		return areas.get(index);
	}

	public ArrayList<Area> getAreas() {
		return areas;
	}

	public void setAreas(ArrayList<Area> areas) {
		this.areas = areas;
	}

	public ArrayList<CardBase> getHandCards() {
		return handCards;
	}

	public ArrayList<CardBase> getDecks() {
		return decks;
	}

	public void setDecks(ArrayList<CardBase> decks) {
		this.decks = decks;
	}

	public ArrayList<CardBase> getDiscards() {
		return discards;
	}

	public void setDiscards(ArrayList<CardBase> discards) {
		this.discards = discards;
	}

	public int getMainRune() {
		return mainRune;
	}

	public void setMainRune(int mainRune) {
		this.mainRune = mainRune;
	}

	public int getLvUpResource() {
		int lvUpResource = this.lvUpResource;
		if (getCostArg().get(SkillManager.AREA_COST) != null) {
			lvUpResource += getCostArg().get(SkillManager.AREA_COST);
		}
		return lvUpResource >= 0 ? lvUpResource : 0;
	}

	public void setLvUpResource(int lvUpResource) {
		this.lvUpResource = lvUpResource;
	}

	public void addLvUpResource() {
		if (lvUpResource >= Area.MAX_COST) {
			return;
		}
		lvUpResource++;
	}

	public ArrayList<FindCard> getFindCards() {
		return findCards;
	}

	public void setFindCards(ArrayList<FindCard> findCards) {
		this.findCards = findCards;
	}

	public boolean getStatus(String type) {
		return getStatus().get(type) == null ? false : getStatus().get(type) > 0;
	}

	public int getStatusCount(String type) {
		int value = getStatus().get(type) == null ? 0 : getStatus().get(type);
		if (AMPLIFY.equals(type) && getStatus(SkillManager.DOUBLE_AMPLIFY)) {
			return value * 2;
		}
		return value;
	}

	public void setStatus(String type, boolean status) {
		getStatus().put(type, status ? 1 : 0);
	}

	public void addStatusCount(String type, int count) {
		if (getStatus().get(type) == null) {
			getStatus().put(type, count);
		} else {
			getStatus().put(type, getStatus().get(type) + count);
		}
	}

	public void setStatusTrun(String type, int trunCount) {
		getStatus().put(type, trunCount);
	}

	public HashMap<String, Integer> getStatus() {
		return status;
	}

	public void setStatus(HashMap<String, Integer> status) {
		this.status = status;
	}
	
	public int getWarcryCount() {
		return getStatusCount(WARCRY_COUNT);
	}
	
	public int getConstructTrapSelect() {
		return getStatusCount(CONSTRUCT_TRAP_SELECT);
	}
	
	public void setConstructTrapSelect(int value) {
		if (value <= getStatusCount(CONSTRUCT_TRAP_SELECT)) {
			return;
		}
		setStatusTrun(CONSTRUCT_TRAP_SELECT, value);
	}
	
	public boolean removeConstructTrapSelect(CardBase card) {
		setStatusTrun(CONSTRUCT_TRAP_SELECT, INIT_CONSTRUCT_TRAP_SELECT);
		for (Area area : this.areas) {
			for (ArtifactCard arti : area.getArtifact()) {
				if (arti.getUid() == card.getUid()) {
					continue;
				}
				setConstructTrapSelect(arti.getStatusCount(CONSTRUCT_TRAP_SELECT));
			}
		}
		return false;
	}
	
	public boolean isNotFirst() {
		return getStatus().get(PLAY_CARD_COUNT) > 1;
	}
	
	public boolean isBlessing() {
		return getStatus(CardModel.BLESSING);
	}

	public boolean isFirst() {
		if (getStatus().get(PLAY_CARD_COUNT) == null) {
			return false;
		}
		return getStatus().get(PLAY_CARD_COUNT) == 1;
	}
	
	public boolean isSecond() {
		if (getStatus().get(PLAY_CARD_COUNT) == null) {
			return false;
		}
		return getStatus().get(PLAY_CARD_COUNT) == 2;
	}
	
	public boolean isSecondAfter() {
		Integer count = getStatus().get(PLAY_CARD_COUNT);
		return count == null ? false : count >= 2;
	}
	
	public boolean isPlaySpellCard() {
		return getStatusCount(PLAY_SPELL_COUNT) >= 1;
	}
	
	public boolean isFirstSpellCard() {
		if (getStatus().get(PLAY_SPELL_COUNT) == null) {
			return false;
		}
		return getStatus().get(PLAY_SPELL_COUNT) == 1;
	}
	
	public boolean isFirstTroopCard() {
		if (getStatus().get(PLAY_TROOP_COUNT) == null) {
			return false;
		}
		return getStatus().get(PLAY_TROOP_COUNT) == 1;
	}
	
	public void playCard(CardBase card) {
		addStatusCount(PLAY_CARD_COUNT, 1);
		
		if (card.getType() == CardModel.SPELL || card.getType() == CardModel.TRAP) {
			addStatusCount(PLAY_SPELL_COUNT, 1);
			
		} else if (card.getType() == CardModel.TROOP) {
			addStatusCount(PLAY_TROOP_COUNT, 1);
		}
		
		if (CardModel.BLESSING.equals(card.getSubType())) {
			setStatus(CardModel.BLESSING, true);
		}
		
		if (CardModel.ABERRATION.equals(card.getSubType())) {
			addStatusCount(CardModel.ABERRATION, 1);
		}
		
		if (CardModel.DROWER.equals(card.getSubType())) {
			addTurnPlayCardBySubType(CardModel.DROWER);
		}
	}
	
	public void addTurnPlayCardBySubType(String subtype) {
		addStatusCount(getTurnPlayCardStrBySubType(subtype), 1);
	}
	
	public int getTurnPlayCardBySubType(String subtype) {
		return getStatusCount(getTurnPlayCardStrBySubType(subtype));
	}
	
	public static String getTurnPlayCardStrBySubType(String subtype) {
		return SkillManager.TURN + "-" + subtype;
	}
	
	public void startTrun() {
		setStatus(CardModel.BLESSING, false);
		setStatusTrun(PLAY_SPELL_COUNT, 0);
		setStatusTrun(PLAY_TROOP_COUNT, 0);
		setStatusTrun(PLAY_CARD_COUNT, 0);
		setStatusTrun(getTurnPlayCardStrBySubType(CardModel.DROWER), 0);
		setStatusTrun(TriggerManager.HERO_BE_ATTACK_COUNT, 0);
		setStatusTrun(DAMAGE, 0);
		// 区域升级重置为可操作
		this.setAreaLvUp(false);
		this.setDrawCard(false);
	}
	
	public CardBase deal() {
		if (this.getDecks().size() == 0) {
			return null;
		}
		CardBase card = this.getDecks().remove(0);
		return card;
	}
	
	public HashMap<String, Integer> getArg() {
		return arg;
	}
	
	public int getArg(String key) {
		return this.arg.get(key) == null ? 0 : this.arg.get(key);
	}
	
	public void setArg(String key, int value) {
		if (arg.get(key) == null) {
			arg.put(key, value);
		} else {
			arg.put(key, arg.get(key) + value);
		}
	}

	public int getDamage() {
		return getStatusCount(DAMAGE);
	}

	public int getAreaCount() {
		return getArg(AREA_COUNT);
	}

	public int getAirTemple() {
		return getArg(AIR);
	}

	public int getWaterTemple() {
		return getArg(WATER);
	}

	public int getFireTemple() {
		return getArg(FIRE);
	}

	public int getEarthTemple() {
		return getArg(EARTH);
	}
	
	public int getTempleCount() {
		return getArg(TEMPLE_COUNT);
	}
	
	public void setAreaCount() {
		HashMap<String, Integer> arg = getArg();
		arg.clear();
		for (Area area : areas) {
			if (area.getLevel() > 0) {
				setArg(AREA_COUNT, 1);
				if (area.getLevel() == Area.MAX_LEVEL) {
					setArg(TEMPLE_COUNT, 1);
					switch (area.getRune()) {
					case SkillManager.AIR:
						setArg(AIR, 1);
						break;
					case SkillManager.EARTH:
						setArg(EARTH, 1);
						break;
					case SkillManager.FIRE:
						setArg(FIRE, 1);
						break;
					case SkillManager.WATER:
						setArg(WATER, 1);
						break;
					}
				}
			}
		}
	}
	
	public int getTroopTempleCount() {
		int count = 0;
		for (Area area : this.areas) {
			if (area.getLevel() == Area.MAX_LEVEL && area.getTroops().size() != 0) {
				count++;
			}
		}
		return count;
	}

	public int getTrapCount() {
		int count = 0;
		for (Area area : this.areas) {
			ArrayList<TrapCard> trap = area.getTrap();
			count += trap.size();
		}
		return count;
	}

	public int getCardCount() {
		int count = 0;
		for (Area area : this.areas) {
			ArrayList<TroopCard> troops = area.getTroops();
			ArrayList<CardBase> artiTraps = area.getArtiTraps();
			count += troops.size();
			count += artiTraps.size();
		}
		return count;
	}
	
	public int getCardNumberByCost(int cost) {
		int count = 0;
		for (CardBase card : this.handCards) {
			if (card.getCost(this) >= cost) {
				count++;
			}
		}
		return count;
	}
	
	public boolean isSameHandCard() {
		for (CardBase card1 : this.handCards) {
			for (CardBase card2 : this.handCards) {
				if (card1.getUid() != card2.getUid() && card1.getId() == card2.getId()) {
					return true;
				}
			}
		}
		return false;
	}
	
	public CardBase removeCostMaxInDeck() {
		if (this.decks.size() == 0) {
			return null;
		}
		CardBase result = null;
		for (CardBase card : this.decks) {
			if (result == null || result.getCost(this) < card.getCost(this)) {
				result = card;
			}
		}
		this.decks.remove(result);
		return result;
	}
	
	public CardBase getCostMaxInHandCard() {
		if (this.handCards.size() == 0) {
			return null;
		}
		CardBase result = null;
		for (CardBase card : this.handCards) {
			if (result == null || result.getCost(this) < card.getCost(this)) {
				result = card;
			}
		}
		return result;
	}
	
	public int getPathOrBreachArea() {
		int count = 0;
		for (Area area : this.areas) {
			for (TroopCard troop : area.getTroops()) {
				if (troop.getPathfinder() < 3) {
					count++;
					break;
				} else if (troop.getStatus().get(TriggerManager.BREACH) != null) {
					count++;
					break;
				}
			}
		}
		return count;
	}
	
	public int getDemigodTemple() {
		int count = 0;
		for (Area area : this.areas) {
			for (TroopCard troop : area.getTroops()) {
				if (CardModel.DEMIGOD.equals(troop.getSubType())) {
					count++;
					break;
				}
			}
		}
		return count;
	}
	
	public int getAberrationCount() {
		int count = 0;
		for (Area area : this.areas) {
			for (TroopCard troop : area.getTroops()) {
				if (CardModel.ABERRATION.equals(troop.getSubType())) {
					count++;
				}
			}
		}
		return count;
	}
	
	public int getCost7Demigod() {
		int count = 0;
		for (Area area : this.areas) {
			for (TroopCard troop : area.getTroops()) {
				if (troop.getCost(this) >= 7 && CardModel.DEMIGOD.equals(troop.getSubType())) {
					count++;
				}
			}
		}
		return count;
	}
	
	public int getStunCount() {
		int count = 0;
		for (Area area : this.areas) {
			for (TroopCard troop : area.getTroops()) {
				if (troop.isStun()) {
					count++;
				}
			}
		}
		return count;
	}
	
	public int getFlightCount() {
		int count = 0;
		for (Area area : this.areas) {
			for (TroopCard troop : area.getTroops()) {
				if (troop.isFlight()) {
					count++;
				}
			}
		}
		return count;
	}
	
	public int getSpiderCount() {
		int count = 0;
		for (Area area : this.areas) {
			for (TroopCard troop : area.getTroops()) {
				if (troop.getId().equals("1001")) {
					count++;
				}
			}
		}
		return count;
	}
	
	public int getAwakeTroopCount() {
		int count = 0;
		for (Area area : this.areas) {
			for (TroopCard troop : area.getTroops()) {
				if (troop.isEnchant()) {
					count++;
				}
			}
		}
		return count;
	}
	
	public boolean isCanBeAttack() {
		for (Area area : this.areas) {
			for (TroopCard troop : area.getTroops()) {
				if (troop.getStatus(TroopCard.HERO_CANT_BE_ATTACK)) {
					return false;
				}
			}
		}
		return true;
	}
	
	public int isAreaLvUpChangeDraw(boolean isTurnPlayer) {
		if (getTempleCount() < 3) {
			return 0;
		}
		for (Area area : this.areas) {
			for (TroopCard troop : area.getTroops()) {
				if (troop.getStatus(TroopCard.AREA_LV_UP_CHANGE_DRAW)) {
					if (isDrawCard() || isAreaLvUp() || !isTurnPlayer) {
						return 2;
					} else {
						return 1;
					}
				}
			}
		}
		return 0;
	}

	public ArrayList<CardBase> getAttackLimitSpell() {
		return attackLimitSpell;
	}

	public void setAttackLimitSpell(ArrayList<CardBase> attackLimitSpell) {
		this.attackLimitSpell = attackLimitSpell;
	}

	public HashMap<Integer, HashMap<Integer, Effect>> getEffects() {
		return effects;
	}

	public void setEffects(HashMap<Integer, HashMap<Integer, Effect>> effects) {
		this.effects = effects;
	}

	public Effect getEffects(int cardUid, int subId) {
		if (this.effects.get(cardUid) == null) {
			return null;
		}
		return this.effects.get(cardUid).get(subId);
	}

	public void setEffects(int cardUid, int subId, Effect effect) {
		if (this.effects.get(cardUid) == null) {
			this.effects.put(cardUid, new HashMap<>());
		}
		if (this.effects.get(cardUid).get(subId) != null) {
			return;
		}
		this.effects.get(cardUid).put(subId, effect);
	}

	public void removeEffects(int cardUid, int subId) {
		if (this.effects.get(cardUid) == null) {
			return;
		}
		if (this.effects.get(cardUid).get(subId) == null) {
			return;
		}
		this.effects.get(cardUid).remove(subId);
		if (this.effects.get(cardUid).size() == 0) {
			this.effects.remove(cardUid);
		}
	}

	public HashMap<String, Integer> getCostArg() {
		return costArg;
	}

	public void setCostArg(HashMap<String, Integer> costArg) {
		this.costArg = costArg;
	}
	
	public void addCostArg(String key, int value) {
		if (costArg.get(key) == null) {
			costArg.put(key, value);
			return;
		}
		costArg.put(key, costArg.get(key) + value);
	}

	public String getDealCardId() {
		return dealCardId;
	}

	public void setDealCardId(String dealCardId) {
		this.dealCardId = dealCardId;
	}

	public HashMap<String, Boolean> getPlayCards() {
		return playCards;
	}

	public boolean setPlayCards(String cardId) {
		if (playCards.get(cardId) != null) {
			return false;
		}
		playCards.put(cardId, true);
		return true;
	}

	public void setEffectCard(int cardUid) {
		effectCards.put(cardUid, cardUid);
	}
	
	public boolean isEffectCard(int cardUid) {
		return effectCards.get(cardUid) != null;
	}
	
	public void clearEffectCard() {
		this.effectCards.clear();
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public HashMap<Integer, String> getFirstFind() {
		return firstFind;
	}
	
	public String getFirstFind(int cardUid) {
		return firstFind.get(cardUid);
	}

	public void setFirstFind(int cardUid, String cardId) {
		if (this.firstFind.get(cardUid) == null) {
			this.firstFind.put(cardUid, cardId);
		}
	}

	public QuestManager getQuestManager() {
		return questManager;
	}

	public void setQuestManager(QuestManager questManager) {
		this.questManager = questManager;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public CardBase getInputCard() {
		return inputCard;
	}

	public void setInputCard(CardBase inputCard) {
		this.inputCard = inputCard;
	}

	public int getState() {
		return state;
	}

	public boolean isState() {
		return state != 0;
	}
	
	public void setState(int state) {
		this.state = state;
	}

	public void clearState() {
		this.state = 0;
	}
	
	public boolean isMessaging() {
		return getStatus(MESSAGING);
	}
	
	public void msgBegin() {
		setStatus(MESSAGING, true);
	}
	
	public void msgEnd() {
		setStatus(MESSAGING, false);
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public int getRoomId() {
		return roomId;
	}

	public void setRoomId(int roomId) {
		this.roomId = roomId;
	}

	public boolean isRobot() {
		return robot;
	}

	public void setRobot(boolean robot) {
		this.robot = robot;
	}

	public SkillArg getInterruptSkillArg() {
		return interruptSkillArg;
	}

	public void setInterruptSkillArg(SkillArg interruptSkillArg) {
		this.interruptSkillArg = interruptSkillArg;
	}
	
	public boolean isExtraTurn() {
		if(getStatus(EXTRA_TURN)) {
			setStatus(EXTRA_TURN, false);
			return true;
		}
		return false;
	}

	public HashMap<String, String> getReplaceCard() {
		return replaceCard;
	}

	public void setReplaceCard(String key, String value) {
		this.replaceCard.put(key, value);
	}
	
	public static int getStatusType(String type) {
		switch (type) {
		case SkillManager.FIREPACT:
			return 1;
		case SkillManager.EARTHPACT:
			return 2;
		case SkillManager.WATERPACT:
			return 3;
		case SkillManager.AIRPACT:
			return 4;
		case AMPLIFY:
			return 5;
		}
		return -1;
	}
}
