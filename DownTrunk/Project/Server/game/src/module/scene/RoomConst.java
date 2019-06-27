package module.scene;

public class RoomConst {
	

	/*
	 * 游戏类型
	 */
	
	/*
	 * 错误代码
	 */
	/** 成功 */
	public static final int SUCCESS = 1;
	/** 失败 */
	public static final int FAILED = 0;

	/*
	 * 房间类型
	 */
	public static final int ROOM_TYPE_PVP = 1;
	
	public static final int ROOM_TYPE_GUIDE = 2;
	
	public static final int ROOM_TYPE_ROBOT = 3;
	
	/*
	 * 房间状态
	 */
	/** 房间状态-等待进入 */
	public static final int ROOM_STATE_JOIN = 0;
	/** 房间状态-等待准备 */
	public static final int ROOM_STATE_READY = 1;
	/** 房间状态-游戏中 */
	public static final int ROOM_STATE_PLAYING = 2;
	/** 房间状态-游戏结束 */
	public static final int ROOM_STATE_END = 3;
	/** 房间状态-房间已销毁 */
	public static final int ROOM_DESTORY = -1;
	
	/** 房间状态-游戏开局中 */
	public static final int GAME_STARTING = 1;
	
	/*
	 * 房间状态
	 */
	/** 房间状态-等待开局 */
	public static final int PLAY_STATE_READY = 0;
	/** 房间状态-游戏开局 */
	public static final int PLAY_STATE_START = 1;
	
}
