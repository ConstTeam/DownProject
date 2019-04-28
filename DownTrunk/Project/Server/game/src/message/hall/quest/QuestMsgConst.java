package message.hall.quest;

/**
 *任务模块消息号
 *
 */
public class QuestMsgConst {
	
	/* ------------------ 接收 ------------------*/
	/** 获取任务信息 */
	public static final int QUEST_INFO = 1;
	/** 刷新任务 */
	public static final int FLUSH_QUEST = 2;
	
	/** 七日连登信息 */
	public static final int SIGNIN_INFO = 3;
	/** 领取七日连登奖励 */
	public static final int RECEIVE_SIGNIN = 4;
	/** 设置连登绑定手机号 */
	public static final int SIGNIN_BIND_PHONE_NUMBER = 5;
	
	/* ------------------ 发送 ------------------*/
	/** 同步任务信息 */
	public static final int QUEST_INFO_SYNC = 1;
	/** 同步单条任务信息 */
	public static final int QUEST_INFO_SINGLE_SYNC = 2;
	
	/** 同步七日连登信息 */
	public static final int SIGNIN_INFO_SYNC = 3;
	/** 领取七日连登奖励 */
	public static final int RECEIVE_SIGNIN_RET = 4;
}
