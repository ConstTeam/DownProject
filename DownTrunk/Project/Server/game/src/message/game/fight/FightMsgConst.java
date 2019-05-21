package message.game.fight;

/**
 * 战斗模块消息号
 *
 */
public class FightMsgConst {

	/* ------------------ 接收 ------------------ */
	/** 准备 */
	public static final int READY = 1;
	/** 角色血量 */
	public static final int ROLE_HP = 2;
	/** 角色获得道具 */
	public static final int GET_ITEM = 3;

	/* ------------------ 发送 ------------------ */
	/** 进入房间 */
	public static final int INTO_ROOM = 1;
	/** 开始游戏 */
	public static final int START_GAME = 2;
	/** 同步血量 */
	public static final int HP_SYNC = 3;
	/** 同步角色获得道具 */
	public static final int GET_ITEM_SYNC = 4;

	/** 提示消息 */
	public static final int MSG_BOX = 14;
}
