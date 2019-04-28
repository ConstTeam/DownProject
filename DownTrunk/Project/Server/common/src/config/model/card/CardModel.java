package config.model.card;

public class CardModel {

	/** 部队 */
	public static final int TROOP = 1;
	/** 法术 */
	public static final int SPELL = 2;
	/** 神器 */
	public static final int ARTIFACT = 3;
	/** 陷阱 */
	public static final int TRAP = 4;
	
	public static final String TALE = "Tale";
	public static final String HUMAN = "Human";
	public static final String DRIDER = "Drider";
	public static final String LOREMASTER = "Loremaster";
	public static final String BLESSING = "Blessing";
	public static final String ABERRATION = "Aberration";
	public static final String COYOTLE = "Coyotle";
	public static final String PLANT = "Plant";
	public static final String DEMIGOD = "Demigod";
	public static final String ORC = "Orc";
	public static final String TRAP_TYPE = "Trap";
	public static final String DROWER = "Drower";
	public static final String DWARF = "Dwarf";
	public static final String FIREPACT = "Firepact";
	public static final String FAE = "Fae";
	public static final String WATERPACT = "Waterpact";
	public static final String EARTHPACT = "Earthpact";
	public static final String AVARIEL = "Avariel";
	public static final String AIRPACT = "Airpact";
	
	public static final String TRAP_INPUT = "TRAP_INPUT";
	public static final String TRAP_OUTPUT = "TRAP_OUTPUT";

	/** 卡牌类型 */
	public int type;
	/** 卡牌id */
	public String ID;
	/** 攻击力 */
	public int Attack;
	/** 血量 */
	public int Hp;
	/** 品质 */
	public int Quality;
	/** 技能id */
	public int[] Skills;
	/** 快速法术 */
	public int MagicId;
	/** 符文 */
	public int[] Rune;
	/** 符文 */
	public String RuneStr;
	/** 资源 */
	public int Cost;
	/** 天赋 */
	public String Genius;
	/** 子类型 */
	public String SubType;
	/** 购买价 */
	public int BuyGold;
	/** 卖出价 */
	public int SellGold;
	/** 限量 */
	public int Limit;
	/** 是否可见 */
	public int IsShow;
	/** 是否开放 */
	public int Open;

}
