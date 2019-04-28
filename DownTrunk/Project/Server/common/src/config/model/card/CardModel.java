package config.model.card;

public class CardModel {

	/** ���� */
	public static final int TROOP = 1;
	/** ���� */
	public static final int SPELL = 2;
	/** ���� */
	public static final int ARTIFACT = 3;
	/** ���� */
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

	/** �������� */
	public int type;
	/** ����id */
	public String ID;
	/** ������ */
	public int Attack;
	/** Ѫ�� */
	public int Hp;
	/** Ʒ�� */
	public int Quality;
	/** ����id */
	public int[] Skills;
	/** ���ٷ��� */
	public int MagicId;
	/** ���� */
	public int[] Rune;
	/** ���� */
	public String RuneStr;
	/** ��Դ */
	public int Cost;
	/** �츳 */
	public String Genius;
	/** ������ */
	public String SubType;
	/** ����� */
	public int BuyGold;
	/** ������ */
	public int SellGold;
	/** ���� */
	public int Limit;
	/** �Ƿ�ɼ� */
	public int IsShow;
	/** �Ƿ񿪷� */
	public int Open;

}
