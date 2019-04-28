package module.templet;

public abstract class TempletBase {

	/** 起手牌数 */
	public int firstCardNum = 3;
	/** 起手后可调度 */
	public boolean canChange = true;
	/** 起手后可调度次数 */
	public int canChangeCount = 2;
	/** 起始Hp */
	public int initHp = 25; // 英雄初始血量
	/** 加速模式开始回合数 */
	public int turboModeRound = 11;
	/** 附加参数1 */
	public int arg1;
	/** 房间类型 */
	public int type;
}
