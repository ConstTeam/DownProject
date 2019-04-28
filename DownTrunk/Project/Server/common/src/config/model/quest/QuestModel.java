package config.model.quest;

public class QuestModel {

	/** 任务id */
	public int ID;
	/** 任务类型 */
	public int Type;
	/** 任务Sub配置表Id */
	public int SubId;
	/** 完成任务需达成数量 */
	public int Value;
	/** 任务难度等级 */
	public int Level;
	/** 奖励Id */
	public String AwardId;
	/** 间隔次数 */
	public int FlushCount;
}
