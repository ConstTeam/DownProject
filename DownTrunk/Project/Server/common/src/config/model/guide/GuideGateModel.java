package config.model.guide;

public class GuideGateModel {
	/** 关卡id */
	public int ID;
	/** 关卡敌方ICON */
	public String RobotIcon;
	/** 关卡敌方显示名 */
	public String RobotName;
	/** 敌方卡组Id */
	public int RobotCardGroupId;
	/** 敌方初始血量 */
	public int RobotHP;
	/** 关卡我方ICON */
	public String Icon;
	/** 关卡我方显示名 */
	public String Name;
	/** 我方卡组Id */
	public int CardGroupId;
	/** 我方初始血量 */
	public int HP;
	/** 初始手牌数 */
	public int HandCardCount;
	/** 先手(1Robot|0玩家) */
	public int First;
}
