package config.model.game;

public class GameCoinRuleModel {
	
	/** 游戏类型 */
	public int GameType;
	/** 入场金币（乘以底分的倍数） */
	public int EnterCoin;
	/** 离场金币（乘以底分的倍数） */
	public int ExitCoin;
	/** 房费（每局固定扣除的金额，同时也是返利计算用金额） */
	public double RoomCoin;
}
