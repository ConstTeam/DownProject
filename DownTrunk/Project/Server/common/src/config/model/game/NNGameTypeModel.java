package config.model.game;

public class NNGameTypeModel {
	/** ��ţ */
	public static final int TYPE = 1001;
	
	/** ��ׯ���� */
	public static final String BANKER_TYPE = "0";
	/** �����Ϸ���� */
	public static final String MAX_NUM = "1";
	/** ��ע���� */
	public static final String BET_MULTI = "2";
	/** �ܾ��� */
	public static final String MAX_ROUND = "3";
	/** ��ׯ���� */
	public static final String BANKER_MULTI = "4";
	/** ��ע */
	public static final String PUSH_BET = "5";
	/** �Զ���ʼ */
	public static final String AUTO_START = "6";
	/** �߼�ѡ�� */
	public static final String OTHER_OPTION = "7";
	/** ���� */
	public static final String ROOM_CONSUME = "8";
	
	/** ��ҳ�-�߼�ѡ�� */
	public static final String COIN_OTHER_OPTION = "2";
	/** ��ҳ�-��ע */
	public static final String BASE_SCORE = "3";

	/** ��ֹ���� */
	public static final String DONT_TWIST = "1";
	/** ��ʼ�������� */
	public static final String START_DONT_JOIN = "2";
	
	/** Id */
	public String id;
	/** ��ׯ���� */
	public int[] bankerType;
	/** �����Ϸ���� */
	public int[] maxNum;
	/** ��ע���� */
	public String[] betMulti;
	/** �ܾ��� */
	public int[] maxRound;
	/** ��ׯ���� */
	public int[] bankerMulti;
	/** ��ע */
	public int[] pushBet;
	/** �Զ���ʼ */
	public int[] autoStart;
	/** �߼�ѡ�� */
	public int[] option;
}
