package db;

import config.ConfigData;

public class DBModuleConst {

	public static final int FIGHT_RECORD_DESTORY_TIME = ConfigData.arguments.get("FIGHT_RECORD_DESTORY_TIME");
	/** 开服时间 */
	public static final String SERVER_START_TIME = "SERVER_START_TIME";
	
	public static final String FORCE_LOGOUT = "FrozenPlayer";
	/** 玩家上限 */
	public static final int PLAYER_LIMIT = 900000;
    /** 货币类型 金币 */
	public static final int GOLD = 1;
    /** 货币类型 钻石 */
	public static final int DIAMOND = 2;
    /** 货币类型 预扣钻石 */
	public static final int DIAMOND_PRE = 3;
    /** 货币类型 红包 */
	public static final int RED_PACKET = 3;
    /** 货币类型 金币 */
	public static final String GOLD_STR = "gold";
    /** 货币类型 钻石 */
	public static final String DIAMOND_STR = "diamond";
}
