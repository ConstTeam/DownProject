package redis.subscribe;

import app.ServerStaticInfo;

public class SubPubConst {
	
	/* ������ */
	
	/*
	 * ���ֲ� 
	 */
	/** ������Ϣ */
	public static final String CLUB_ROOM_INFO = "1";
	/** ���ֲ����ơ����� */
	public static final String CLUB_INFO = "2";
	/** ��Ա��Ϣ */
	public static final String MEMBER_INFO = "3";
	/** ���ֲ���ʯ */
	public static final String CLUB_DIAMOND = "4";
	/** ���ֲ���ɢ */
	public static final String CLUB_DISBAND = "5";
	/** ���ֲ������� */
	public static final String CLUB_RED_POINT = "6";
	

	/*
	 * ���
	 */
	/** ���ǿ���ߺ� */
	public static final String PLAYER_LOGOUT = "1";
	/** ��ҳ�ֵ֪ͨ */
	public static final String PLAYER_RECHARGE = "2";
	/** GMǿ���ߺ� */
	public static final String GM_PLAYER_LOGOUT = "3";
	/** ������Ϸ�� */
	public static final String CONN_GAME_SERVER = "4";
	
	public static final String PLAYER_NOTICE = ServerStaticInfo.getServerId() + ".player.*";
	
	/* ��Ϸ�� */
	
	/*
	 * ����
	 */
	/** ���ٷ��� */
	public static final String GM_DESTORY_ROOM = "1";
	
}
