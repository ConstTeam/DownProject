package message.hall.login;

/**
 * 登录模块消息号
 *
 */
public class LoginMessageConst {
	
	/* ------------------ 接收 ------------------*/
	/** 平台登录 */
	public static final int LOGIN = 1;
	/** 分配游戏服 */
	public static final int ASSIGN_INSTANCE_SERVER = 2;
	/** 取消分配 */
	public static final int CANCEL_ASSIGN = 3;
	/** 断线重连 */
	public static final int RE_LOGIN = 4;
	/** 新手分配游戏服 */
	public static final int ASSIGN_GUIDE_INSTANCE_SERVER = 5;
	
	/* ------------------ 发送 ------------------*/
	
	public static final int LOGIN_RES = 1;
	
	public static final int ASSIGN_INSTANCE_SERVER_RES = 2;
	
	public static final int MESSAGE_BOX = 3;
	
	public static final int RE_LOGIN_SERVER = 4;
	/** 取消分配 */
	public static final int CANCEL_ASSIGN_SYNC = 5;
	/** 分配成功 */
	public static final int ASSIGN_SUCCESS_SYNC = 6;
	
	/* ------------------ 客户端推送提示  ------------------*/
	/** 客户端弹出提示 */
	public static final int MESSAGE_BOX_ID = 2;
	/*
	 * 待实现
	 */
	/** 防沉迷弹出提示 */
	public static final int ANTI_MESSAGE_BOX = 0;
	
}
