package message.game;

public class GameMsgModuleConst {

	/* ------------------ 接收客户端请求的模块号 ------------------*/
	/** 游戏服-游戏房间服务 */
	public static final int ROOM_SERVICE = 101;
	/** 游戏服-战斗服务 */
	public static final int FIGHT_SERVICE = 102;
	/** 游戏服-战斗服务 */
	public static final int OTHER_SERVICE = 103;
	/** 游戏服-GM服务 */
	public static final int GM_SERVICE = 4;
	/* ------------------ 发送给客户端的模块号 ------------------*/
	/** 游戏服-游戏房间服务 */
	public static final int ROOM_RESPONSE = 101;
	/** 游戏服-战斗服务 */
	public static final int FIGHT_RESPONSE = 102;
	/** 游戏服-战斗服务 */
	public static final int OTHER_RESPONSE = 103;
	/** 游戏服-GM服务 */
	public static final int GM_RESPONSE = 4;
}
