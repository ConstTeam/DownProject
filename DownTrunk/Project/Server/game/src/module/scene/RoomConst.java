package module.scene;

public class RoomConst {
	

	/*
	 * ��Ϸ����
	 */
	
	/*
	 * �������
	 */
	/** �ɹ� */
	public static final int SUCCESS = 1;
	/** ʧ�� */
	public static final int FAILED = 0;

	/*
	 * ��������
	 */
	public static final int ROOM_TYPE_PVP = 1;
	
	public static final int ROOM_TYPE_GUIDE = 2;
	
	public static final int ROOM_TYPE_ROBOT = 3;
	
	/*
	 * ����״̬
	 */
	/** ����״̬-��Ϸ�� */
	public static final int ROOM_STATE_PLAYING = 1;
	/** ����״̬-��Ϸ���� */
	public static final int ROOM_STATE_END = 2;
	/** ����״̬-���������� */
	public static final int ROOM_DESTORY = -1;
	
	/** ����״̬-��Ϸ������ */
	public static final int GAME_STARTING = 1;
	
	/*
	 * ����״̬
	 */
	/** ����״̬-�ȴ����� */
	public static final int PLAY_STATE_READY = 0;
	/** ����״̬-��Ϸ���� */
	public static final int PLAY_STATE_START = 1;
	/** ����״̬-�ȴ� */
	public static final int PLAY_STATE_WAIT = 2;
	/** ����״̬-�Զ������غ� */
	public static final int PLAY_STATE_AUTO = 3;
	
	/** �غ����� */
	public static final int TRUN_CARD = 0;
	/** ���� */
	public static final int FIND_CARD = 1;
	
	public static final int TURN_COUNT_DOWN_TIME = 85 * 1000;
	
}
