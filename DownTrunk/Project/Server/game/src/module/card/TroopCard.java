package module.card;

import java.util.ArrayList;
import java.util.HashMap;

import config.ConfigData;
import config.model.card.CardModel;
import config.model.skill.SkillModel;
import module.area.Area;
import module.fight.BattleRole;

public class TroopCard extends CardBase implements ITroopStatus {

	private final int type = CardModel.TROOP;

	/** ¹¥»÷Á¦ */
	private int attack;
	
	private Area oldArea;
	
	private int attackType = 0;

	private ArrayList<String> leaderSkillTriggers = new ArrayList<>();

	public TroopCard(CardModel cardModel) {
		this.init(cardModel);
		this.setHp(cardModel.Hp);
		this.setAttack(cardModel.Attack);
		this.setGenius(cardModel.Genius);
		setSleep(true);
		setStatus(ATTACKED, false);
	}
	
	public void init() {
		CardModel cardModel = ConfigData.cardModels.get(this.getRealId());
		this.init(cardModel);
		this.setHp(cardModel.Hp);
		this.setAttack(cardModel.Attack);
		setSleep(true);
		setStatus(ATTACKED, false);
	}

	public int getAttack() {
		if (isDoubleDamage()) {
			return getRealAttack() * 2;
		}
		return getRealAttack();
	}

	public int getRealAttack() {
		return attack < 0 ? 0 : attack;
	}

	public void setAttack(int attack) {
		this.setAttrChange(true);
		this.attack = attack;
		if (this.attack > ATK_MAX) {
			this.attack = ATK_MAX;
		}
	}

	public void addAttack(int attack) {
		setAttack(this.attack + attack);
	}

	public void addHp(int hp) {
		this.setHp(this.getHp() + hp);
		if (this.getHp() <= 0) {
			this.setDead(true);
		}
	}

	public int getType() {
		return type;
	}

	public boolean isSleep() {
		return getStatus(SLEEP);
	}
	
	public boolean isUndead() {
		return getStatus(UNDEAD);
	}

	public void setSleep(boolean status) {
		if (isSleep() == status) {
			return;
		}
		if (status && isSpeed()) {
			return;
		}
		setStatus(SLEEP, status);
	}

	public void attack() {
		setStatus(ATTACKED, true);
	}

	public boolean isGuardian() {
		if (getStatus(STUN)) {
			return false;
		}
		return getStatus(GUARDIAN);
	}

	public boolean isFlight() {
		if (getStatus(STUN)) {
			return false;
		}
		return getStatus(FLIGHT);
	}

	public boolean isLifedrain() {
		return getStatus(LIFEDRAIN);
	}

	public boolean canBeAttack() {
		return !getStatus(AVOID_ATTACKED);
	}

	public boolean canBeOppAttack() {
		return !getStatus(AVOID_OPP_ATTACKED);
	}

	@Override
	public boolean isForceShield() {
		return getStatus(FORCE_SHIELD);
	}

	public void setForceShield(boolean status) {
		setStatus(FORCE_SHIELD, status);
	}

	public boolean isSpellBlock() {
		return getStatus(SPELL_BLOCK);
	}

	public void setControl(boolean status) {
		setStatus(CONTROL, status);
	}

	public boolean isControl() {
		return getStatus(CONTROL);
	}

	public boolean isSpeed() {
		return getStatus(SPEED);
	}
	
	public boolean isEnemyCantCure() {
		return getStatus(ENEMY_CANT_CURE);
	}

	public void endTurn() {
		setAttrChange(false);
		setChange(false);
		Integer turnCount = getStatus().get(STUN);
		if (turnCount != null && turnCount >= 1) {
			setStatusTrun(STUN, turnCount - 1);
		}
		setStatus(ATTACKED, true);
		setStatus(AWAKE_ONLY, false);
		setStatus(SPEED, false);

		Integer trunCount = getStatus().get(ArtifactCard.TRUN_COUNT);
		if (trunCount != null) {
			setStatus(ArtifactCard.TRUN_COUNT, true);
		}
	}

	public void startTurn() {
		setAttrChange(false);
		setChange(false);
		setSleep(false);
		setStatus(ATTACKED, false);
	}

	public boolean isEnchant() {
		if (getStatus(AWAKE_ONLY)) {
			return true;
		}
		if (getStatus(ATTACKED)) {
			return false;
		}
		if (isSleep()) {
			return false;
		}
		return true;
	}

	public boolean isAttack(BattleRole fighter) {
		if (getStatus(ATTACKED)) {
			return false;
		}
		if (isSleep()) {
			return false;
		}
		if (getStatus(STUN)) {
			return false;
		}
		if (getStatus(CANT_ATTACK)) {
			return false;
		}
		if (isAttackLimitTroop()) {
			Area area = fighter.getArea(this.getAreaIndex());
			if (!area.troopIsFull()) {
				return false;
			}
		}
		return true;
	}

	public int getStunCount() {
		if (getStatus().get(STUN) == null) {
			return 0;
		}
		return getStatus().get(STUN);
	}

	public boolean isLeader() {
		return getStatus(LEADER);
	}

	public boolean isOppAreaAttack() {
		return getStatus(OPP_AREA_ATTACK);
	}

	public boolean isMermaidLover() {
		return getStatus(MERMAID_LOVER);
	}

	public boolean isSplash() {
		return getStatus(SPLASH);
	}

	public boolean isAttackLimitTroop() {
		return getStatus(ATTACK_LIMIT_TROOP);
	}

	public boolean isAttackLimitSpell() {
		return getStatus(ATTACK_LIMIT_SPELL);
	}

	public boolean isHeroAttacker() {
		return getStatus(HERO_ATTACKER);
	}

	public boolean isRandomTarget() {
		return getStatus(RANDOM_TARGET);
	}

	public boolean isExcessDamage() {
		return getStatus(EXCESS_DAMAGE);
	}

	public boolean isDoubleDamage() {
		return getStatus(DOUBLE_DAMAGE);
	}

	public boolean isInvincible() {
		return getStatus(INVINCIBLE);
	}

	public boolean isAttackInvincible() {
		return getStatus(ATTACK_INVINCIBLE);
	}

	public boolean isDefenderInvincible() {
		return getStatus(DEFENDER_INVINCIBLE);
	}

	public boolean isAlwaysAttackHero() {
		return getStatus(ALWAYS_ATTACK_HERO);
	}

	public ArrayList<String> getLeaderTriggers() {
		return leaderSkillTriggers;
	}

	public void addLeaderSkillTriggers(SkillModel model) {
		if (model.Cancel == 0) {
			return;
		}
		String cardId = model.ID;
		if (cardId.equals(this.getId()) && this.leaderSkillTriggers.indexOf(cardId) != -1) {
			return;
		}
		this.leaderSkillTriggers.add(cardId);
	}

	public void removeLeaderSkillTriggers() {
		this.leaderSkillTriggers.remove(this.getId());
	}

	public Area getOldArea() {
		return oldArea;
	}

	public void setOldArea(Area oldArea) {
		this.oldArea = oldArea;
	}

	@Override
	public int getMainRowIndex() {
		if (this.getArea() == null) {
			return -1;
		}
		return this.getArea().getMainRowIndex(this);
	}

	@Override
	public int getAreaIndex() {
		if (this.getArea() == null) {
			return -1;
		}
		return this.getArea().getIndex();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void copy(CardBase card) {
		if (card.getType() != CardModel.TROOP) {
			return;
		}
		TroopCard troop = (TroopCard) card;
		this.setAttack(troop.getAttack());
		this.setHp(troop.getHp());
		Object clone = troop.getStatus().clone();
		this.setStatus((HashMap<String, Integer>) clone);
		this.setStatus(ATTACKED, false);
		this.setSleep(true);
	}

	public int getAttackType() {
		return attackType;
	}

	public void setAttackType(int attackType) {
		this.attackType = attackType;
	}
}
