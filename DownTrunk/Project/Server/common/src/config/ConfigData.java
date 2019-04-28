package config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import config.model.card.CardDeckModel;
import config.model.card.CardModel;
import config.model.card.InitCardModel;
import config.model.drop.DropModel;
import config.model.ladder.LadderDownModel;
import config.model.ladder.LadderUpModel;
import config.model.ladder.MatchTimeModel;
import config.model.notice.NoticeRollModel;
import config.model.notice.NoticeTextModel;
import config.model.quest.AwardModel;
import config.model.quest.QuestModel;
import config.model.quest.QuestSubModel;
import config.model.quest.SignInQuestModel;
import config.model.recharge.RechargeModel;
import config.model.robot.RobotCardGroupModel;
import config.model.robot.RobotModel;
import config.model.shop.CardPackShopModel;
import config.model.shop.RealCardPackShopModel;
import config.model.skill.SkillModel;
import config.model.guide.GuideGateModel;
import config.model.guide.GuideGateCardGroupModel;
import config.model.guide.GuideGateAIModel;

/**
 * 配置文件数据
 * 
 */
public class ConfigData {

	/** 配置表路径 */
	private static String configPath;

	/** 参数（HashMap<参数Id，值>） */
	public static HashMap<String, Integer> arguments;
	public static HashMap<String, String> argumentsByString;

	/** 错误代码（HashMap<错误代码，错误信息Id>） */
	public static HashMap<String, Integer> errorCode;
	/** 错误信息（HashMap<错误信息Id，错误信息>） */
	public static HashMap<Integer, String> errorMessage;
	
	/** 弹出信息（HashMap<Id，信息>） */
	public static HashMap<Integer, String> messageBox;

	/** 充值商品信息（HashMap<ID，充值表Model>） */
	public static HashMap<String, RechargeModel> rechargeModels;

	/** 文本公告 */
	public static HashMap<String, NoticeTextModel> noticeTextModels;
	/** 滚动公告 */
	public static HashMap<Integer, NoticeRollModel> noticeRollModels;
	
	/** 卡牌信息 */
	public static HashMap<String, CardModel> cardModels = new HashMap<>();
	/** 技能信息 */
	public static HashMap<String, HashMap<Integer, SkillModel>> skillModels;
	/** 技能信息 */
	public static HashMap<String, String> skillValueArgModels;
	/** 卡牌信息 <子类型,ArrayList<卡牌>> */
	public static HashMap<String, HashMap<String, CardModel>> cardModelBySubType = new HashMap<>();
	/** 卡牌信息 <品质,ArrayList<卡牌>> */
	public static HashMap<String, HashMap<String, CardModel>> cardModelByQuality = new HashMap<>();
	/** 组装陷阱卡牌信息 */
	public static HashMap<String, CardModel> constructTraps = new HashMap<>();
	
	/** 卡牌组信息 */
	public static HashMap<Integer, CardDeckModel> cardDeckModels;
	/** 初始卡牌信息 */
	public static HashMap<Integer, InitCardModel> initCardModels;
	/** 掉落信息 */
	public static HashMap<Integer, Vector<DropModel>> dropModels;
	/** 卡包商店信息 */
	public static HashMap<Integer, CardPackShopModel> cardPackShopModels;
	public static HashMap<Integer, RealCardPackShopModel> realCardPackShopModels;
	/** 天梯升级信息 */
	public static HashMap<Integer, LadderUpModel> ladderUpModels;
	/** 天梯降段*/
	public static HashMap<Integer, LadderDownModel> ladderDownModels;
	/** 赛季时间信息*/
	public static HashMap<Integer, MatchTimeModel> matchTimeModels;
	public static int VISITOR_MAX_NUMBER = 0;

	/** 任务信息 */
	public static HashMap<Integer, QuestModel> questModels;
	/** 任务信息*/
	public static HashMap<Integer, ArrayList<QuestModel>> questModelsByLevel;
	/** 任务Sub信息 */
	public static HashMap<Integer, QuestSubModel> questSubModels;
	/** 签到奖励信息 */
	public static HashMap<Integer, SignInQuestModel> signInModels;
	
	/** 可使用次数信息 */
	public static HashMap<Integer, Integer> useCountModels;
	/** 奖励信息 */
	public static HashMap<String, AwardModel> awardModels;
	
	/** 新手关卡 */
	public static HashMap<Integer, GuideGateModel> guideGateModels;
	/** 新手关卡卡组 */
	public static HashMap<Integer, GuideGateCardGroupModel> guideGateCardGroupModels;
	/** 新手关卡AI */
	public static HashMap<Integer, Vector<GuideGateAIModel>> guideGateAIModels;
	
	/** 机器人 */
	public static HashMap<Integer, RobotModel> robotModels;
	/** 机器人卡组 */
	public static HashMap<Integer, RobotCardGroupModel> robotCardGroupModels;
	
	public static String getConfigPath() {
		return configPath;
	}

	public static void setConfigPath(String configPath) {
		ConfigData.configPath = configPath;
	}
}
