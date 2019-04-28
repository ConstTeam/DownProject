package config.model.game;

public class NNGameTypeModel {
	/** 斗牛 */
	public static final int TYPE = 1001;
	
	/** 抢庄类型 */
	public static final String BANKER_TYPE = "0";
	/** 最大游戏人数 */
	public static final String MAX_NUM = "1";
	/** 下注倍数 */
	public static final String BET_MULTI = "2";
	/** 总局数 */
	public static final String MAX_ROUND = "3";
	/** 抢庄倍数 */
	public static final String BANKER_MULTI = "4";
	/** 推注 */
	public static final String PUSH_BET = "5";
	/** 自动开始 */
	public static final String AUTO_START = "6";
	/** 高级选项 */
	public static final String OTHER_OPTION = "7";
	/** 房费 */
	public static final String ROOM_CONSUME = "8";
	
	/** 金币场-高级选项 */
	public static final String COIN_OTHER_OPTION = "2";
	/** 金币场-底注 */
	public static final String BASE_SCORE = "3";

	/** 禁止搓牌 */
	public static final String DONT_TWIST = "1";
	/** 开始后禁入加入 */
	public static final String START_DONT_JOIN = "2";
	
	/** Id */
	public String id;
	/** 抢庄类型 */
	public int[] bankerType;
	/** 最大游戏人数 */
	public int[] maxNum;
	/** 下注倍数 */
	public String[] betMulti;
	/** 总局数 */
	public int[] maxRound;
	/** 抢庄倍数 */
	public int[] bankerMulti;
	/** 推注 */
	public int[] pushBet;
	/** 自动开始 */
	public int[] autoStart;
	/** 高级选项 */
	public int[] option;
}
