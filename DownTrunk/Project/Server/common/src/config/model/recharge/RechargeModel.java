package config.model.recharge;

import util.GameData;

public class RechargeModel extends GameData {

	/** Id */
	public String ID;
	public String DisplayName;
	/** 单价 */
	public int Price;
	/** 货币类型 1:金币  2:钻石 */
	public int Type;
	/** 充值获得的货币数量 */
	public int Num;
}
