package message.game.fight;

/**
 * ս��ģ����Ϣ��
 *
 */
public class FightMsgConst {

	/* ------------------ ���� ------------------ */
	/** ѡ�����ֻ���� */
	public static final int FIRST_CHOOSE = 1;
	/** ���ֻ������ */
	public static final int READY = 2;
	/** �����غ� */
	public static final int END_TURN = 3;
	/** ��������� */
	public static final int TROOP_CARD_PLAY = 4;
	/** ���� */
	public static final int ATTACT = 5;
	/** �������� */
	public static final int AREA_LV_UP = 6;
	/** ��������� */
	public static final int SPELL_CARD_PLAY = 7;
	/** ��������� */
	public static final int ARTI_CARD_PLAY = 8;
	/** ���ֻ��� */
	public static final int REPLACE_FIRST_DEAL = 9;
	/** �������-���忨 */
	public static final int SPELL_TRAP_CARD_PLAY = 10;
	/** ����-ѡ��Ŀ��� */
	public static final int FIND_SELECT = 11;
	/** �鿴-ѡ������ƿ�׻��ƿⶥ */
	public static final int CHECK_CARD_SELECT = 12;
	/** Ͷ�� */
	public static final int GIVE_UP = 13;
	/** ��װ���� */
	public static final int CONSTRUCT_TRAP = 14;
	/** �ٻ��滻������ */
	public static final int SUMMON_TROOP = 15;
	/** ���� */
	public static final int TAP_CARD = 16;
	/** ѡ��Ŀ�� */
	public static final int TARGET_SELECT = 17;
	/** ���� */
	public static final int DRAW_CARD = 18;
	/**  */
	public static final int OTHER = 2;

	/* ------------------ ���� ------------------ */
	/** ���� */
	public static final int START_GAME = 1;
	/** ��ʼ�� */
	public static final int FIRST_DEAL = 2;
	/** ���ֻ��ƽ�� */
	public static final int REPLACE_FIRST_DEAL_RESULT = 3;
	/** �ѷ�׼���ɹ� */
	public static final int SELF_READY_SYNC = 4;
	/** ��׼������ʼ�Ծ� */
	public static final int GAME_START_SYNC = 5;
	/** ׼������ʱ */
	public static final int NOTICE = 6;
	/** ����ͬ�� */
	public static final int TAP_CARD_SYNC = 7;
	/** ׼��ͬ�� */
	public static final int READY_SYNC = 8;
	/** ����ͬ�� */
	public static final int PVPSETTLEMENT_SYNC = 9;
	/** �鿴-ѡ�������Ϣ */
	public static final int CHECK_CARD_SELECT_RESULT = 13;
	/** ��ʾ��Ϣ */
	public static final int MSG_BOX = 14;
	/** ͬ������ʣ�࿨������ */
	public static final int DECK_CARD_NUMER_SYNC = 15;
	/** ����������Ҫ�����ĵ��� */
	public static final int AREA_LV_UP_NEED_RES = 16;
	/** LOGͬ�� */
	public static final int LOG_SYNC = 18;
	/** �غϵ���ʱ */
	public static final int TURN_NOTICE = 20;
	/** ָ��������ͬ�� */
	public static final int GUIDESETTLEMENT_SYNC = 21;

	/** Ӣ��Ѫ��ͬ�� */
	public static final int HERO_HP_SYNC = 51;
	/** ������ͬ�� */
	public static final int TROOP_ATTR_SYNC = 52;
	/** ��������ͬ�� */
	public static final int HANDCARD_ATTR_SYNC = 53;
	/** ��������ͬ�� */
	public static final int CARD_COST_SYNC = 54; // TODO �Ƿ�������ʾ
	/** �˺�Ч��ͬ�� */
	public static final int REDUCE_HP_SYNC = 55;
	/** ���ٳ��Ͽ��� */
	public static final int DESTORY_CARD = 56;
	/** ��Դͬ�� */
	public static final int RESOURCE_SYNC = 57;
	/** ͬ��ƣ�� */
	public static final int TIRET_SYNC = 58;
	/** ��Ͽ��� */
	public static final int INTERRUPT_CARD = 59;
	/** Ӣ���Ƿ�ɱ����� */
	public static final int ROLE_CAN_BE_ATTACK = 60;
	/** Ӣ��״ֵ̬ͬ�� */
	public static final int ROLE_STATUS_SYNC = 61;
	/** ����ť������ */
	public static final int AREA_LV_UP_CHANGE_DRAW = 62;
	/** ��ѹ���� */
	public static final int EXCESS_SYNC = 63;

	/** ת������ */
	public static final int COPY_AND_PLAY = 120;
	/** ת������ */
	public static final int CHANGE_HANDCARDS = 121;
	/** �ѷ���ȡ���� */
	public static final int DEAL_CARD_SYNC = 122;
	/** �з�����ͬ�� */
	public static final int ENEMY_CARD_SYNC = 123;
	/** �ٻ��� */
	public static final int DEAL_CARD = 124; // TODO �Ƿ�������ʾ
	/** ���Ƶ����� */
	public static final int HAND_CARD_TO_DECK = 125;
	/** ���Ƶ����� */
	public static final int HAND_CARD_TO_PLAY = 126;
	/** ���� */
	public static final int DISCARD_SYNC = 127;
	/** ��ʾ */
	public static final int REVEAL_CARD_SYNC = 128;
	/** �鿴���� */
	public static final int CHECK_CARD_SYNC = 129;
	/** �������� */
	public static final int RETURN_HAND_CARDS = 130;
	/** �ٻ������� */
	public static final int SUMMON_SYNC = 131;
	/** �ٻ��滻 */
	public static final int SUMMON_SELECT = 132;
	/** ת�� */
	public static final int TRANSFORM_SYNC = 133;
	/** �ƶ�ͬ�� */
	public static final int MOVE_SYNC = 134;
	/** ���ٳ��ϲ������� */
	public static final int DESTORY_TROOP_CARD = 135;
	/** ��ײ�˺�Ч��ͬ�� */
	public static final int ATTACK_SYNC = 136;
	/** �������� */
	public static final int AREA_LV_UP_END = 137;
	/** �������� */
	public static final int DESTORY_AREA = 138;
	/** ���ֿ��� */
	public static final int FIND_CARD_SYNC = 139;
	/** ���ֽ�� */
	public static final int FIND_RESULT = 140;
	/** ��װ���� */
	public static final int CONSTRUCT_TRAP_SYNC = 141;
	/** ��װ������Ϣ��� */
	public static final int CONSTRUCT_TRAP_RES = 142;
	/** ����ѡ��Ŀ��-���� */
	public static final int CARD_TARGET_SELECT_REQUEST = 143;
	/** ����ѡ��Ŀ�� */
	public static final int TARGET_SELECT_REQUEST = 144;
	/** ����ѡ��Ŀ��-�ƶ� */
	public static final int MOVE_TARGET_SELECT_REQUEST = 145;
	/** ��������� */
	public static final int PLAY_TROOP_SYNC = 146;
	/** ��������� */
	public static final int PLAY_SPELL_SYNC = 147;
	/** �������-������ */
	public static final int PLAY_TRAP_SYNC = 148;
	/** ��������ƽ��� */
	public static final int PLAY_ARTI_SYNC = 149;
	/** ���ƽ��� */
	public static final int PLAY_CARD_END = 150;
	/** ��������-���� */
	public static final int TROOP_FLIGHT_SYNC = 151;
	/** ��������-���� */
	public static final int TROOP_GUARDIAN_SYNC = 152;
	/** ��������-˯�� */
	public static final int TROOP_SLEEP_SYNC = 153;
	/** ��������-�ɹ��� */
	public static final int TROOP_ATTACK_SYNC = 154;
	/** ��������-ʥ�� */
	public static final int TROOP_FORCE_SHIELD_SYNC = 155;
	/** ��������-������ */
	public static final int TROOP_SPELL_BLOCK_SYNC = 156;
	/** ��������-���� */
	public static final int TROOP_CONTROL_SYNC = 157;
	/** ��������-�ȷ� */
	public static final int TROOP_SPEED_SYNC = 158;
	/** ��������-��Ѫ */
	public static final int TROOP_LIFEDRAIN_SYNC = 159;
	/** �Ƿ��ܱ����� */
	public static final int CAN_BE_ATTACK = 160;
	/** �Ƿ��ܱ��������򹥻� */
	public static final int CAN_BE_OPP_AREA_ATTACK = 161;
	/** ��������-�޵� */
	public static final int TROOP_INVINCIBLE_SYNC = 162;
	/** ��������-��ѣ */
	public static final int TROOP_STUN_SYNC = 163;
	/** ��������-ͬ���Ŵ� */
	public static final int AMPLIFY_SYNC = 164;
	/** ��������-���赲Ҳ�ɹ���Ӣ�� */
	public static final int ALWAYS_ATTACK_HERO = 165;
	/** ��������-��˯���Ƿ��ѹ������� */
	public static final int TROOP_ATTACKED_SYNC = 166;
	/** ��������-�������� */
	public static final int TROOP_ATTACK_TYPE = 167;
	/** ��������-���غ��ܵ��˺� */
	public static final int TROOP_DAMAGED_SYNC = 168;

	/** Ч��-���ﴥ�� */
	public static final int TROOP_DEATHCRY_SYNC = 194;
	/** Ч��-�ƻ󴥷� */
	public static final int TROOP_ENCHANT_SYNC = 195;
	/** Ч��-��Ѫ���� */
	public static final int LIFEDRAIN_SYNC = 196;
	/** Ч��-���崥�� */
	public static final int TRAP_TRIGGER_SYNC = 197;
	/** Ч��-�������� */
	public static final int ARTI_TRIGGER_SYNC = 198;
	/** Ч��-��ʼ���� */
	public static final int START_SYNC = 199;
	/** Ч��-ս�𴥷�*/
	public static final int WARCRY_SYNC = 200;

	/** ��ʼ */
	public static final int SYNC_START = 201;
	/** ���� */
	public static final int SYNC_END = 202;
	
	/** �غϿ�ʼ */
	public static final int MY_TURN = 211;
	/** �غϿ�ʼ */
	public static final int MY_TURN_END = 212;

	/** �غϽ��� */
	public static final int TRUN_FINISH = 213;
	/** �غϽ��� */
	public static final int TRUN_FINISH_END = 214;
	
	/** �غϽ��� */
	public static final int OTHER_SYNC = 2;

}
