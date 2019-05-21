package module.card;

import java.util.ArrayList;
import java.util.HashMap;

import config.model.card.CardModel;
import module.area.Area;
import module.fight.BattleRole;
import module.fight.IBattleObject;
import module.log.LogInfo;
import skill.Effect;
import skill.TriggerManager;

public abstract class CardBase implements IBattleObject, ICardAreaAction {

	public boolean change = false;
	
	public boolean attrChange = false;
	
	public static final int HP_MAX = 99;
	
	public static final int ATK_MAX = 99;
	
	/** 本局唯一id */
	private int uid;
	/** 玩家id */
	private int playerId;
	/** 旧玩家id */
	private int oldOwnerId;
	/** 卡牌id */
	private String id;
	/** 卡牌id */
	private String realId;
	/** 卡牌Subid */
	private String subType;
	/** genius */
	private String genius;
	/** 血量 */
	private int hp;
	/** 天赋 */
	private int pathfinder = 3;
	/** 卡牌所在区域 */
	private Area area;
	/** 卡牌原所在区域 */
	private Area oldArea;
	/** 消耗 */
	private int cost = 0;
	/** 死亡 */
	private boolean dead;
	/** 攻击者 */
	private IBattleObject attackTarget;
	/** log */
	private LogInfo logInfo;
	/** 目标 */
	private ArrayList<Object> target = new ArrayList<>();
	/** 目标 */
	private ArrayList<CardBase> effectCards = new ArrayList<>();
	/** 状态 */
	private HashMap<String, Integer> status;
	/** 当前可撤消的效果（属性变化） HashMap<施法者uid, HashMap<技能SubID, Effect>> */
	private HashMap<Integer, HashMap<Integer, Effect>> effects;
	
	protected void init(CardModel cardModel) {
		this.setId(cardModel.ID);
		this.setRealId(cardModel.ID);
		this.setCost(cardModel.Cost);
		this.setSubType(cardModel.SubType);
		this.setChange(true);
		this.setAttrChange(true);
		setEffects(new HashMap<>());
		setStatus(new HashMap<>());
	}
	
	protected void setGenius(String genius) {
	}
	
	public String getGenius() {
		return this.genius;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRealId() {
		return realId;
	}
	
	public void setRealId(String realId) {
		this.realId = realId;
	}

	@Override
	public boolean isForceShield() {
		return false;
	}

	@Override
	public int getHp() {
		return hp < 0 ? 0 : hp;
	}
	
	public int getRealHp() {
		return hp;
	}
	
	public void setHp(int hp) {
		this.setAttrChange(true);
		hp = hp > HP_MAX ? HP_MAX : hp;
		this.hp = hp;
	}

	public abstract int getType();

	public int getPathfinder() {
		return pathfinder;
	}

	public void setPathfinder(int pathfinder) {
		this.pathfinder = pathfinder;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public boolean isDead() {
		return dead;
	}

	public boolean isAlive() {
		return !dead && hp > 0;
	}

	public void setDead(boolean dead) {
		this.dead = dead;
	}

	@Override
	public boolean isChange() {
		return change;
	}

	@Override
	public void setChange(boolean change) {
		this.change = change;
	}

	public boolean isAttrChange() {
		return attrChange;
	}

	public void setAttrChange(boolean attrChange) {
		this.attrChange = attrChange;
	}

	public ArrayList<Object> getTarget() {
		return target;
	}

	public void setTarget(ArrayList<Object> target) {
		this.target = target;
	}

	@Override
	public int getPlayerId() {
		return playerId;
	}

	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}

	public boolean getStatus(String type) {
		switch (type) {
		case TroopCard.STUN:
		case TroopCard.ATTACKED:
		case TroopCard.SLEEP:
			return getStatus().get(type) == null ? false : getStatus().get(type) > 0;
		default:
			if (isStun()) {
				return false;
			}
			return getStatus().get(type) == null ? false : getStatus().get(type) > 0;
		}
	}
	
	public int getStatusCount(String type) {
		if (getStatus().get(type) == null) {
			return 0;
		}
		return getStatus().get(type);
	}
	
	public boolean isStun() {
		return getStatus().get(TroopCard.STUN) == null ? false : getStatus().get(TroopCard.STUN) > 0;
	}
	
	public void setStatus(String type, boolean status) {
		switch (type) {
		case TroopCard.AVOID_ATTACKED:
		case TroopCard.AVOID_OPP_ATTACKED:
		case TroopCard.ALWAYS_ATTACK_HERO:
		case TroopCard.DOUBLE_DAMAGE:
		case TroopCard.FLIGHT:
		case TroopCard.GUARDIAN:
		case TroopCard.LIFEDRAIN:
		case TroopCard.CANT_ATTACK:
			addStatusCount(type, status ? 1 : -1);
			break;
		default:
			getStatus().put(type, status ? 1 : 0);
			break;
		}
		if (!TroopCard.ATTACKED.equals(type)) {
			this.setChange(true);
		}
	}

	public boolean isDeathcry() {
		return getStatus(TriggerManager.DEATHCRY);
	}
	
	public void setDeathcry(boolean state) {
		setStatus(TriggerManager.DEATHCRY, state);
	}

	public void setStatusTrun(String type, int trunCount) {
		getStatus().put(type, trunCount);
		this.setChange(true);
	}

	public void addStatusCount(String type, int count) {
		if (getStatus().get(type) == null) {
			getStatus().put(type, count);
		} else {
			getStatus().put(type, getStatus().get(type) + count);
		}
		if (getStatus().get(type) < 0) {
			getStatus().put(type, 0);
		}
		this.setChange(true);
	}

	public HashMap<String, Integer> getStatus() {
		return status;
	}

	public void setStatus(HashMap<String, Integer> status) {
		this.status = status;
	}
	
	public int getAmplify() {
		if (!getStatus(BattleRole.AMPLIFY)) {
			return 0;
		}
		return getStatus().get(BattleRole.AMPLIFY);
	}

	public int getDefaultCost() {
		return this.cost;
	}

	public void setCost(int cost) {
		this.cost = cost;
	}
	
	public void addCost(int cost) {
		if (cost == 0) {
			this.cost = 0;
		} else {
			this.cost = this.cost + cost;
			if (this.cost < 0) {
				this.cost = 0;
			}
		}
	}

	public String getSubType() {
		return subType;
	}

	public void setSubType(String subType) {
		this.subType = subType;
	}

	public int getOldOwnerId() {
		return oldOwnerId;
	}

	public void setOldOwnerId(int oldOwnerId) {
		this.oldOwnerId = oldOwnerId;
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

	public LogInfo getLogInfo() {
		return logInfo;
	}

	public void setLogInfo(LogInfo logInfo) {
		this.logInfo = logInfo;
	}

	public Area getArea() {
		return area;
	}

	public void setArea(Area area) {
		this.area = area;
	}
	
	public abstract void copy(CardBase card);

	public IBattleObject getAttackTarget() {
		return attackTarget;
	}

	public void setAttackTarget(IBattleObject attackTarget) {
		this.attackTarget = attackTarget;
	}

	public Area getOldArea() {
		return oldArea;
	}

	public void setOldArea(Area oldArea) {
		this.oldArea = oldArea;
	}

	public ArrayList<CardBase> getEffectCards() {
		return effectCards;
	}

	public void setEffectCards(ArrayList<CardBase> effectCards) {
		this.effectCards = effectCards;
	}
}
