package message.game.fight;

/**
 * 战斗模块消息号
 *
 */
public class FightMsgConst {

	/* ------------------ 接收 ------------------ */
	/** 选择先手或后手 */
	public static final int FIRST_CHOOSE = 1;
	/** 开局换牌完成 */
	public static final int READY = 2;
	/** 结束回合 */
	public static final int END_TURN = 3;
	/** 打出部队牌 */
	public static final int TROOP_CARD_PLAY = 4;
	/** 攻击 */
	public static final int ATTACT = 5;
	/** 升级区域 */
	public static final int AREA_LV_UP = 6;
	/** 打出法术卡 */
	public static final int SPELL_CARD_PLAY = 7;
	/** 打出神器卡 */
	public static final int ARTI_CARD_PLAY = 8;
	/** 开局换牌 */
	public static final int REPLACE_FIRST_DEAL = 9;
	/** 打出法术-陷阱卡 */
	public static final int SPELL_TRAP_CARD_PLAY = 10;
	/** 发现-选择的卡牌 */
	public static final int FIND_SELECT = 11;
	/** 查看-选择放入牌库底或牌库顶 */
	public static final int CHECK_CARD_SELECT = 12;
	/** 投降 */
	public static final int GIVE_UP = 13;
	/** 组装陷阱 */
	public static final int CONSTRUCT_TRAP = 14;
	/** 召唤替换部队牌 */
	public static final int SUMMON_TROOP = 15;
	/** 滑牌 */
	public static final int TAP_CARD = 16;
	/** 选择目标 */
	public static final int TARGET_SELECT = 17;
	/** 摸牌 */
	public static final int DRAW_CARD = 18;
	/**  */
	public static final int OTHER = 2;

	/* ------------------ 发送 ------------------ */
	/** 开局 */
	public static final int START_GAME = 1;
	/** 初始牌 */
	public static final int FIRST_DEAL = 2;
	/** 开局换牌结果 */
	public static final int REPLACE_FIRST_DEAL_RESULT = 3;
	/** 已方准备成功 */
	public static final int SELF_READY_SYNC = 4;
	/** 已准备，开始对局 */
	public static final int GAME_START_SYNC = 5;
	/** 准备倒计时 */
	public static final int NOTICE = 6;
	/** 滑牌同步 */
	public static final int TAP_CARD_SYNC = 7;
	/** 准备同步 */
	public static final int READY_SYNC = 8;
	/** 结算同步 */
	public static final int PVPSETTLEMENT_SYNC = 9;
	/** 查看-选择结束消息 */
	public static final int CHECK_CARD_SELECT_RESULT = 13;
	/** 提示消息 */
	public static final int MSG_BOX = 14;
	/** 同步卡组剩余卡牌数量 */
	public static final int DECK_CARD_NUMER_SYNC = 15;
	/** 升级区域需要的消耗点数 */
	public static final int AREA_LV_UP_NEED_RES = 16;
	/** LOG同步 */
	public static final int LOG_SYNC = 18;
	/** 回合倒计时 */
	public static final int TURN_NOTICE = 20;
	/** 指引结算结果同步 */
	public static final int GUIDESETTLEMENT_SYNC = 21;

	/** 英雄血量同步 */
	public static final int HERO_HP_SYNC = 51;
	/** 部队牌同步 */
	public static final int TROOP_ATTR_SYNC = 52;
	/** 手牌属性同步 */
	public static final int HANDCARD_ATTR_SYNC = 53;
	/** 卡牌消耗同步 */
	public static final int CARD_COST_SYNC = 54; // TODO 是否立即显示
	/** 伤害效果同步 */
	public static final int REDUCE_HP_SYNC = 55;
	/** 销毁场上卡牌 */
	public static final int DESTORY_CARD = 56;
	/** 资源同步 */
	public static final int RESOURCE_SYNC = 57;
	/** 同步疲劳 */
	public static final int TIRET_SYNC = 58;
	/** 打断卡牌 */
	public static final int INTERRUPT_CARD = 59;
	/** 英雄是否可被攻击 */
	public static final int ROLE_CAN_BE_ATTACK = 60;
	/** 英雄状态值同步 */
	public static final int ROLE_STATUS_SYNC = 61;
	/** 区域按钮改摸牌 */
	public static final int AREA_LV_UP_CHANGE_DRAW = 62;
	/** 碾压触发 */
	public static final int EXCESS_SYNC = 63;

	/** 转换手牌 */
	public static final int COPY_AND_PLAY = 120;
	/** 转换手牌 */
	public static final int CHANGE_HANDCARDS = 121;
	/** 已方抽取卡牌 */
	public static final int DEAL_CARD_SYNC = 122;
	/** 敌方卡牌同步 */
	public static final int ENEMY_CARD_SYNC = 123;
	/** 召唤牌 */
	public static final int DEAL_CARD = 124; // TODO 是否立即显示
	/** 手牌到卡堆 */
	public static final int HAND_CARD_TO_DECK = 125;
	/** 手牌到场上 */
	public static final int HAND_CARD_TO_PLAY = 126;
	/** 弃牌 */
	public static final int DISCARD_SYNC = 127;
	/** 揭示 */
	public static final int REVEAL_CARD_SYNC = 128;
	/** 查看卡牌 */
	public static final int CHECK_CARD_SYNC = 129;
	/** 返回手牌 */
	public static final int RETURN_HAND_CARDS = 130;
	/** 召唤部队牌 */
	public static final int SUMMON_SYNC = 131;
	/** 召唤替换 */
	public static final int SUMMON_SELECT = 132;
	/** 转换 */
	public static final int TRANSFORM_SYNC = 133;
	/** 移动同步 */
	public static final int MOVE_SYNC = 134;
	/** 销毁场上部队死亡 */
	public static final int DESTORY_TROOP_CARD = 135;
	/** 对撞伤害效果同步 */
	public static final int ATTACK_SYNC = 136;
	/** 升级区域 */
	public static final int AREA_LV_UP_END = 137;
	/** 销毁区域 */
	public static final int DESTORY_AREA = 138;
	/** 发现卡牌 */
	public static final int FIND_CARD_SYNC = 139;
	/** 发现结果 */
	public static final int FIND_RESULT = 140;
	/** 组装陷阱 */
	public static final int CONSTRUCT_TRAP_SYNC = 141;
	/** 组装陷阱消息结果 */
	public static final int CONSTRUCT_TRAP_RES = 142;
	/** 请求选择目标-卡牌 */
	public static final int CARD_TARGET_SELECT_REQUEST = 143;
	/** 请求选择目标 */
	public static final int TARGET_SELECT_REQUEST = 144;
	/** 请求选择目标-移动 */
	public static final int MOVE_TARGET_SELECT_REQUEST = 145;
	/** 打出部队牌 */
	public static final int PLAY_TROOP_SYNC = 146;
	/** 打出法术牌 */
	public static final int PLAY_SPELL_SYNC = 147;
	/** 打出法术-陷阱牌 */
	public static final int PLAY_TRAP_SYNC = 148;
	/** 打出神器牌结束 */
	public static final int PLAY_ARTI_SYNC = 149;
	/** 打牌结束 */
	public static final int PLAY_CARD_END = 150;
	/** 部队属性-飞行 */
	public static final int TROOP_FLIGHT_SYNC = 151;
	/** 部队属性-嘲讽 */
	public static final int TROOP_GUARDIAN_SYNC = 152;
	/** 部队属性-睡眠 */
	public static final int TROOP_SLEEP_SYNC = 153;
	/** 部队属性-可攻击 */
	public static final int TROOP_ATTACK_SYNC = 154;
	/** 部队属性-圣盾 */
	public static final int TROOP_FORCE_SHIELD_SYNC = 155;
	/** 部队属性-法术盾 */
	public static final int TROOP_SPELL_BLOCK_SYNC = 156;
	/** 部队属性-控制 */
	public static final int TROOP_CONTROL_SYNC = 157;
	/** 部队属性-先锋 */
	public static final int TROOP_SPEED_SYNC = 158;
	/** 部队属性-吸血 */
	public static final int TROOP_LIFEDRAIN_SYNC = 159;
	/** 是否能被攻击 */
	public static final int CAN_BE_ATTACK = 160;
	/** 是否能被对面区域攻击 */
	public static final int CAN_BE_OPP_AREA_ATTACK = 161;
	/** 部队属性-无敌 */
	public static final int TROOP_INVINCIBLE_SYNC = 162;
	/** 部队属性-晕眩 */
	public static final int TROOP_STUN_SYNC = 163;
	/** 部队属性-同步放大 */
	public static final int AMPLIFY_SYNC = 164;
	/** 部队属性-被阻挡也可攻击英雄 */
	public static final int ALWAYS_ATTACK_HERO = 165;
	/** 部队属性-沉睡（是否已攻击过） */
	public static final int TROOP_ATTACKED_SYNC = 166;
	/** 部队属性-攻击类型 */
	public static final int TROOP_ATTACK_TYPE = 167;
	/** 部队属性-本回合受到伤害 */
	public static final int TROOP_DAMAGED_SYNC = 168;

	/** 效果-亡语触发 */
	public static final int TROOP_DEATHCRY_SYNC = 194;
	/** 效果-蛊惑触发 */
	public static final int TROOP_ENCHANT_SYNC = 195;
	/** 效果-吸血触发 */
	public static final int LIFEDRAIN_SYNC = 196;
	/** 效果-陷阱触发 */
	public static final int TRAP_TRIGGER_SYNC = 197;
	/** 效果-神器触发 */
	public static final int ARTI_TRIGGER_SYNC = 198;
	/** 效果-开始触发 */
	public static final int START_SYNC = 199;
	/** 效果-战吼触发*/
	public static final int WARCRY_SYNC = 200;

	/** 开始 */
	public static final int SYNC_START = 201;
	/** 结束 */
	public static final int SYNC_END = 202;
	
	/** 回合开始 */
	public static final int MY_TURN = 211;
	/** 回合开始 */
	public static final int MY_TURN_END = 212;

	/** 回合结束 */
	public static final int TRUN_FINISH = 213;
	/** 回合结束 */
	public static final int TRUN_FINISH_END = 214;
	
	/** 回合结束 */
	public static final int OTHER_SYNC = 2;

}
