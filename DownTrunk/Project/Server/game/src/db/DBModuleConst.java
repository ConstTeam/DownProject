package db;

import config.ConfigData;

public class DBModuleConst {

	public static final int FIGHT_RECORD_DESTORY_TIME = ConfigData.arguments.get("FIGHT_RECORD_DESTORY_TIME");
	/** ����ʱ�� */
	public static final String SERVER_START_TIME = "SERVER_START_TIME";
	
	public static final String FORCE_LOGOUT = "FrozenPlayer";
	/** ������� */
	public static final int PLAYER_LIMIT = 900000;
    /** �������� ��� */
	public static final int GOLD = 1;
    /** �������� ��ʯ */
	public static final int DIAMOND = 2;
    /** �������� Ԥ����ʯ */
	public static final int DIAMOND_PRE = 3;
    /** �������� ��� */
	public static final int RED_PACKET = 3;
    /** �������� ��� */
	public static final String GOLD_STR = "gold";
    /** �������� ��ʯ */
	public static final String DIAMOND_STR = "diamond";
}
