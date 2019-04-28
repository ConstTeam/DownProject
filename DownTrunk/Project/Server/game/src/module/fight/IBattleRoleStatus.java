package module.fight;

public interface IBattleRoleStatus {

	/** 区域最多3个。Index最大值2 */
	public static final int AREA_MAX_INDEX = 2;
	/** 玩家Index值10 */
	public static final int FIGHTER_INDEX = 10;
	/** 手牌最多10张。 */
	public static final int HAND_CARD_MAX_COUNT = 10;
	
	public static final int INIT_CONSTRUCT_TRAP_SELECT = 2;
	
	public static final int START = 1;
	
	public static final int END = 2;
	
	public static final int PLAY_CARD = 3;
	
	public static final int ATTACK = 4;
	
	public static final int OTHER = 5;
	
	public static final int BREACH = 6;
	
	public static final int DEATHCRY = 7;

	
	public static final String MESSAGING = "Messaging";
	
	public static final String PLAY_CARD_COUNT = "PlayCardCount";
	
	public static final String WARCRY_COUNT = "WarcryCount";

	public static final String PLAY_SPELL_COUNT = "Spell";

	public static final String PLAY_TROOP_COUNT = "Troop";

	
	public static final String AREA_COUNT = "AreaCount";
	
	public static final String TEMPLE_COUNT = "TempleCount";

	public static final String TROOP_TEMPLE_COUNT = "TroopTempleCount";
	
	public static final String AIR = "Air";
	
	public static final String WATER = "Water";
	
	public static final String FIRE = "Fire";
	
	public static final String EARTH = "Earth";

	
	public static final String HP = "Hp";
	
	public static final String TARGET_HP = "TargetHp";
	
	public static final String TARGET_ATK = "TargetAtk";
	
	public static final String TARGET_COST = "TargetCost";
	
	public static final String NEABY_MAX_HP = "NeabyMaxHp";

	public static final String NEABY_MAX_ATK = "NeabyMaxAtk";
	
	public static final String CHECK = "Check";

	public static final String HANDCARD_COUNT = "HandCardCount";

	public static final String ENEMY_HANDCARD_COUNT = "EnemyHandCardCount";
	
	public static final String TARGET_SELECT = "TargetSelect";
	
	public static final String WARCRY_INTERRUPT = "WarcryInterrupt";
	
	public static final String SCHEHERAZADE = "Scheherazade";

	
	public static final String LAST_TROOP = "LastTroop";
	
	public static final String LAST_SPELL = "LastSpell";

	
	public static final String LAST_DRAW_COST = "LastDrawCost";
	
	
	public static final String AMPLIFY = "Amplify";
	
	public static final String DAMAGE = "Damage";
	
	public static final String TIRET = "Tiret";
	
	public static final String DECK_CARD_MODIFY_COUNT = "DeckCardModifyCount";
	
	public static final String TRAP_COUNT = "TrapCount";
	
	public static final String TROOP_COUNT = "TroopCount";
	
	public static final String FLIGHT_COUNT = "FlightCount";
	
	public static final String SPIDER_COUNT = "SpiderCount";
	
	public static final String ABERRATION_COUNT = "AberrationCount";

	public static final String AWAKE_TROOP_COUNT = "AwakeTroopCount";
	
	public static final String CONSTRUCT_TRAP_SELECT = "ConstructTrapSelect";

	public static final String EXTRA_TURN = "ExtraTurn";

	public static final String TURN_DROWER = "Turn-Drower";
	
}
