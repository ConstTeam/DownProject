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
 * �����ļ�����
 * 
 */
public class ConfigData {

	/** ���ñ�·�� */
	private static String configPath;

	/** ������HashMap<����Id��ֵ>�� */
	public static HashMap<String, Integer> arguments;
	public static HashMap<String, String> argumentsByString;

	/** ������루HashMap<������룬������ϢId>�� */
	public static HashMap<String, Integer> errorCode;
	/** ������Ϣ��HashMap<������ϢId��������Ϣ>�� */
	public static HashMap<Integer, String> errorMessage;
	
	/** ������Ϣ��HashMap<Id����Ϣ>�� */
	public static HashMap<Integer, String> messageBox;

	/** ��ֵ��Ʒ��Ϣ��HashMap<ID����ֵ��Model>�� */
	public static HashMap<String, RechargeModel> rechargeModels;

	/** �ı����� */
	public static HashMap<String, NoticeTextModel> noticeTextModels;
	/** �������� */
	public static HashMap<Integer, NoticeRollModel> noticeRollModels;
	
	/** ������Ϣ */
	public static HashMap<String, CardModel> cardModels = new HashMap<>();
	/** ������Ϣ */
	public static HashMap<String, HashMap<Integer, SkillModel>> skillModels;
	/** ������Ϣ */
	public static HashMap<String, String> skillValueArgModels;
	/** ������Ϣ <������,ArrayList<����>> */
	public static HashMap<String, HashMap<String, CardModel>> cardModelBySubType = new HashMap<>();
	/** ������Ϣ <Ʒ��,ArrayList<����>> */
	public static HashMap<String, HashMap<String, CardModel>> cardModelByQuality = new HashMap<>();
	/** ��װ���忨����Ϣ */
	public static HashMap<String, CardModel> constructTraps = new HashMap<>();
	
	/** ��������Ϣ */
	public static HashMap<Integer, CardDeckModel> cardDeckModels;
	/** ��ʼ������Ϣ */
	public static HashMap<Integer, InitCardModel> initCardModels;
	/** ������Ϣ */
	public static HashMap<Integer, Vector<DropModel>> dropModels;
	/** �����̵���Ϣ */
	public static HashMap<Integer, CardPackShopModel> cardPackShopModels;
	public static HashMap<Integer, RealCardPackShopModel> realCardPackShopModels;
	/** ����������Ϣ */
	public static HashMap<Integer, LadderUpModel> ladderUpModels;
	/** ���ݽ���*/
	public static HashMap<Integer, LadderDownModel> ladderDownModels;
	/** ����ʱ����Ϣ*/
	public static HashMap<Integer, MatchTimeModel> matchTimeModels;
	public static int VISITOR_MAX_NUMBER = 0;

	/** ������Ϣ */
	public static HashMap<Integer, QuestModel> questModels;
	/** ������Ϣ*/
	public static HashMap<Integer, ArrayList<QuestModel>> questModelsByLevel;
	/** ����Sub��Ϣ */
	public static HashMap<Integer, QuestSubModel> questSubModels;
	/** ǩ��������Ϣ */
	public static HashMap<Integer, SignInQuestModel> signInModels;
	
	/** ��ʹ�ô�����Ϣ */
	public static HashMap<Integer, Integer> useCountModels;
	/** ������Ϣ */
	public static HashMap<String, AwardModel> awardModels;
	
	/** ���ֹؿ� */
	public static HashMap<Integer, GuideGateModel> guideGateModels;
	/** ���ֹؿ����� */
	public static HashMap<Integer, GuideGateCardGroupModel> guideGateCardGroupModels;
	/** ���ֹؿ�AI */
	public static HashMap<Integer, Vector<GuideGateAIModel>> guideGateAIModels;
	
	/** ������ */
	public static HashMap<Integer, RobotModel> robotModels;
	/** �����˿��� */
	public static HashMap<Integer, RobotCardGroupModel> robotCardGroupModels;
	
	public static String getConfigPath() {
		return configPath;
	}

	public static void setConfigPath(String configPath) {
		ConfigData.configPath = configPath;
	}
}
