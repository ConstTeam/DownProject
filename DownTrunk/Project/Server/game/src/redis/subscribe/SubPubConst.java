package redis.subscribe;

import app.ServerStaticInfo;

public class SubPubConst {
	
	/* 大厅服 */
	
	/*
	 * 俱乐部 
	 */
	/** 牌桌信息 */
	public static final String CLUB_ROOM_INFO = "1";
	/** 俱乐部名称、公告 */
	public static final String CLUB_INFO = "2";
	/** 成员信息 */
	public static final String MEMBER_INFO = "3";
	/** 俱乐部钻石 */
	public static final String CLUB_DIAMOND = "4";
	/** 俱乐部解散 */
	public static final String CLUB_DISBAND = "5";
	/** 俱乐部申请红点 */
	public static final String CLUB_RED_POINT = "6";
	

	/*
	 * 玩家
	 */
	/** 玩家强制踢号 */
	public static final String PLAYER_LOGOUT = "1";
	/** 玩家充值通知 */
	public static final String PLAYER_RECHARGE = "2";
	/** GM强制踢号 */
	public static final String GM_PLAYER_LOGOUT = "3";
	/** 加入游戏服 */
	public static final String CONN_GAME_SERVER = "4";
	
	public static final String PLAYER_NOTICE = ServerStaticInfo.getServerId() + ".player.*";
	
	/* 游戏服 */
	
	/*
	 * 房间
	 */
	/** 销毁房间 */
	public static final String GM_DESTORY_ROOM = "1";
	
}
