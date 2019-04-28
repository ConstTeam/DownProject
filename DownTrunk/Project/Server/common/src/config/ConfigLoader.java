package config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;

import config.model.ArgumentsModel;
import config.model.UseCountModel;
import config.model.card.ArtifactCardMapping;
import config.model.card.CardDeckMapping;
import config.model.card.CardDeckModel;
import config.model.card.CardModel;
import config.model.card.InitCardModel;
import config.model.card.SpellCardMapping;
import config.model.card.TroopCardMapping;
import config.model.drop.DropModel;
import config.model.ladder.LadderDownModel;
import config.model.ladder.LadderUpModel;
import config.model.ladder.MatchTimeModel;
import config.model.message.ErrorCode;
import config.model.message.MessageBox;
import config.model.quest.AwardModel;
import config.model.quest.QuestModel;
import config.model.quest.QuestSubModel;
import config.model.quest.SignInQuestModel;
import config.model.robot.RobotCardGroupMapping;
import config.model.robot.RobotCardGroupModel;
import config.model.robot.RobotCardModel;
import config.model.robot.RobotModel;
import config.model.shop.CardPackShopModel;
import config.model.shop.RealCardPackShopModel;
import config.model.skill.SkillModel;
import config.model.guide.GuideGateModel;
import config.model.guide.GuideGateCardGroupModel;
import config.model.guide.GuideGateCardGroupMapping;
import config.model.guide.GuideCardModel;
import config.model.guide.GuideGateAIModel;
import util.ErrorPrint;
import util.ExcelXlsLoader;
import util.Tools;

/**
 * 配置文件加载
 * 
 */
public class ConfigLoader {

	private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

	public static boolean flag = true;
	/** 字符串分隔符 */
	private static final String SPLIT_STRING = ";";
	/** 字符串分隔符 */
	public static final String SPLIT_STRING_COMMA = ",";

	/** 部队卡牌 */
	private static final String TROOP_CARD = "Card/TroopCard_Common.xls";
	/** 法术卡牌 */
	private static final String SPELL_CARD = "Card/SpellCard_Common.xls";
	/** 神器卡牌 */
	private static final String ARTIFACT_CARD = "Card/ArtifactCard_Common.xls";
	/** 卡牌组 */
	private static final String CARD_DECK = "Card/CardDeck_Server.xls";
	/** 技能 */
	private static final String SKILL = "Card/Skill_Common.xls";
	/** 初始卡 */
	private static final String INIT_CARD = "Card/InitCard_Server.xls";
	/** 掉落 */
	private static final String DROP = "Drop/Drop_Server.xls";
	/** 卡包商店 */
	private static final String CARD_PACK_SHOP = "Shop/CardPackShop_Common.xls";
	private static final String REAL_CARD_PACK_SHOP = "Shop/RealCardPackShop_Common.xls";
	/** 错误代码 */
	private static final String ERROR_MESSAGE = "Message/ErrorMessage_Server.xls";
	/** 弹出消息 */
	private static final String MESSAGE_BOX = "Message/MessageBox_Server.xls";
	/** 参数配置表 */
	private static final String ARGUMENTS = "InitValues_Common.xls";
	/** 天梯升级 */
	private static final String LADDER_UP = "Ladder/LadderUp_Common.xls";
	/** 天梯降段 */
	private static final String LADDER_DOWN = "Ladder/LadderDown_Common.xls";
	/** 赛季时间 */
	private static final String MATCH_TIME = "Ladder/MatchTime_Common.xls";

	/** 每日任务配置表 */
	private static final String DAILY_QUEST = "Quest/DailyQuest_Common.xls";
	/** 任务子配置表 */
	private static final String QUEST_SUB = "Quest/QuestSub_Common.xls";
	/** 签到奖励配置表 */
	private static final String SIGNIN_QUEST = "Quest/SignInQuest_Common.xls";

	/** 可用次数 */
	private static final String USE_COUNT = "UseCount_Common.xls";
	/** 可用次数 */
	private static final String AWARD = "Award_Common.xls";
	/** 新手关卡 */
	private static final String GUIDE_GATE = "Guide/GuideGate_Common.xls";
	/** 新手关卡卡组 */
	private static final String GUIDE_GATE_CARDGROUP = "Guide/GuideGateCardGroup_Server.xls";
	/** 新手关卡AI */
	private static final String GUIDE_GATE_AI = "Guide/GuideGateAI_Server.xls";
	
	/** 机器人 */
	private static final String ROBOT= "Robot/Robot_Server.xls";
	/** 新手关卡卡组 */
	private static final String ROBOT_CARDGROUP = "Robot/RobotCardGroup_Server.xls";
	
	public static HashMap<String, String> reloadMapping = new HashMap<>();
	public static String startServerTime = "";
	private static String path = "";

	public static boolean load() {
		try {
			loadArguments();
			loadMessageBox();
			loadErrorMessage();
			loadCardModel();
			loadDeckCard();
			loadSkill();
			loadInitCard();
			loadDrop();
			loadCardPackShop();
			loadRealCardPackShop();
			loadLadderUp();
			loadLadderDown();
			loadMatchTime();
			loadSubQuest();
			loadSignInQuest();
			loadQuest();
			loadUseCount();
			loadAward();
			loadGuideGate();
			loadGuideGateCardGroup();
			loadGuideGateAI();
			loadRobot();
			loadRobotCardGroup();
			ConfigCheck.check();			
		} catch (Exception e) {
			flag = false;
			ErrorPrint.print(e);
		}
		return flag;
	}


	/**
	 * 加载技能信息
	 * 
	 * @throws Exception
	 */
	public static void loadSkill() throws Exception {
		registerMethod(SKILL, "loadSkill");
		ArrayList<Object> arraylist = ExcelXlsLoader.loadArrayListModel(SkillModel.class,
				loadFilePath(SKILL));
		HashMap<String, HashMap<Integer, SkillModel>> skillModels = new HashMap<>();
		HashMap<String, String> skillValueArgModels = new HashMap<>();

		for (Object gameData : arraylist) {
			SkillModel model = (SkillModel) gameData;
			if (skillModels.get(model.ID) == null) {
				skillModels.put(model.ID, new HashMap<>());
			}
			skillModels.get(model.ID).put(model.SubID, model);
			if (!Tools.isEmptyString(model.Value)) {
				if (skillValueArgModels.get(model.Value) == null) {
					skillValueArgModels.put(model.Value, getValueArg(model.Value));
				}
			}
		}
		ConfigData.skillModels = skillModels;
		ConfigData.skillValueArgModels = skillValueArgModels;
		logger.info("{}载入完毕。", SKILL);
	}

	private static String getValueArg(String value) {
		int begin = value.indexOf("[");
		int end = value.indexOf("]");
		if (begin == -1) {
			if (end != -1) {
				logger.error("Skill_Common配置表，Value填写有误：" + value, new Throwable());
			}
			return value;
		}
		if (end == -1) {
			logger.error("Skill_Common配置表，Value填写有误：" + value, new Throwable());
			return value;
		}
		value = value.substring(begin + 1, end);
		return value;
	}
	
	public static void loadCardModel() throws Exception {
		ConfigData.cardModels = new HashMap<>();
		ConfigData.cardModelBySubType = new HashMap<>();
		ConfigData.cardModelByQuality = new HashMap<>();
		ConfigData.constructTraps = new HashMap<>();
		loadTroopCard();
		loadSpellCard();
		loadArtifactCard();
	}
	
	/**
	 * 加载卡牌信息
	 * 
	 * @throws Exception
	 */
	public static void loadTroopCard() throws Exception {
		registerMethod(TROOP_CARD, "loadCardModel");
		ArrayList<Object> arraylist = ExcelXlsLoader.loadArrayListModel(TroopCardMapping.class,
				loadFilePath(TROOP_CARD));
		HashMap<String, CardModel> cardModels = ConfigData.cardModels;
		HashMap<String, HashMap<String, CardModel>> cardModelBySubType = ConfigData.cardModelBySubType;
		HashMap<String, HashMap<String, CardModel>> cardModelByQuality = ConfigData.cardModelByQuality;
		for (Object gameData : arraylist) {
			TroopCardMapping value = (TroopCardMapping) gameData;
			CardModel model = new CardModel();
			model.type = CardModel.TROOP;
			model.ID = value.ID;
			model.Attack = value.Attack;
			model.Hp = value.Hp;
			model.Cost = value.Cost;
			model.Genius = value.Genius;
			model.SubType = value.SubType;
			model.Quality = value.Quality;
			model.BuyGold = value.BuyGold;
			model.SellGold = value.SellGold;
			model.Limit = value.Limit;
			model.RuneStr = value.Rune;
			model.IsShow = value.IsShow;
			model.Open = value.Open;
			String rune = String.format("[%s]", value.Rune);
			JSONArray array = (JSONArray) JSONArray.parse(rune);
			model.Rune = new int[array.size()];
			for (int i = 0; i < array.size(); i++) {
				model.Rune[i] = (int) array.get(i);
			}
			cardModels.put(model.ID, model);
			if (model.Open == 0) {
				continue;
			}
			if (!Tools.isEmptyString(value.SubType)) {
				if (cardModelBySubType.get(value.SubType) == null) {
					cardModelBySubType.put(value.SubType, new HashMap<>());
				}
				cardModelBySubType.get(value.SubType).put(model.ID, model);
			}
			if (cardModelByQuality.get(String.valueOf(value.Quality)) == null) {
				cardModelByQuality.put(String.valueOf(value.Quality), new HashMap<>());
			}
			cardModelByQuality.get(String.valueOf(value.Quality)).put(model.ID, model);
		}
		ConfigData.cardModels = cardModels;
		logger.info("{}载入完毕。", TROOP_CARD);
	}

	/**
	 * 加载卡牌信息
	 * 
	 * @throws Exception
	 */
	public static void loadSpellCard() throws Exception {
		registerMethod(SPELL_CARD, "loadCardModel");
		ArrayList<Object> arraylist = ExcelXlsLoader.loadArrayListModel(SpellCardMapping.class,
				loadFilePath(SPELL_CARD));
		HashMap<String, CardModel> cardModels = ConfigData.cardModels;
		HashMap<String, HashMap<String, CardModel>> cardModelBySubType = ConfigData.cardModelBySubType;
		HashMap<String, HashMap<String, CardModel>> cardModelByQuality = ConfigData.cardModelByQuality;
		HashMap<String, CardModel> constructTraps = ConfigData.constructTraps;
		for (Object gameData : arraylist) {
			SpellCardMapping value = (SpellCardMapping) gameData;
			CardModel model = new CardModel();
			model.type = CardModel.SPELL;
			model.ID = value.ID;
			model.Cost = value.Cost;
			model.Genius = value.Genius;
			model.SubType = value.SubType;
			model.Quality = value.Quality;
			model.BuyGold = value.BuyGold;
			model.SellGold = value.SellGold;
			model.Limit = value.Limit;
			model.RuneStr = value.Rune;
			model.IsShow = value.IsShow;
			model.Open = value.Open;
			String rune = String.format("[%s]", value.Rune);
			JSONArray array = (JSONArray) JSONArray.parse(rune);
			model.Rune = new int[array.size()];
			for (int i = 0; i < array.size(); i++) {
				model.Rune[i] = (int) array.get(i);
			}
			cardModels.put(model.ID, model);
			if (model.Open == 0) {
				continue;
			}
			if (!Tools.isEmptyString(value.SubType)) {
				if ("Trap".equals(value.SubType)) {
					model.type = CardModel.TRAP;
					if (model.Rune[0] != 0) {
						if (cardModelBySubType.get(value.SubType) == null) {
							cardModelBySubType.put(value.SubType, new HashMap<>());
						}
						cardModelBySubType.get(value.SubType).put(model.ID, model);
					}
					if (!Tools.isEmptyString(model.Genius)) {
						constructTraps.put(model.Genius, model);
					}
				} else {
					if (cardModelBySubType.get(value.SubType) == null) {
						cardModelBySubType.put(value.SubType, new HashMap<>());
					}
					cardModelBySubType.get(value.SubType).put(model.ID, model);
				}
			} else {
				if (CardModel.TRAP_INPUT.equals(model.Genius) || CardModel.TRAP_OUTPUT.equals(model.Genius)) {
					if (cardModelBySubType.get(value.Genius) == null) {
						cardModelBySubType.put(value.Genius, new HashMap<>());
					}
					cardModelBySubType.get(value.Genius).put(model.ID, model);
				}
			}
			if (cardModelByQuality.get(String.valueOf(value.Quality)) == null) {
				cardModelByQuality.put(String.valueOf(value.Quality), new HashMap<>());
			}
			cardModelByQuality.get(String.valueOf(value.Quality)).put(model.ID, model);
		}
		ConfigData.cardModels = cardModels;
		logger.info("{}载入完毕。", SPELL_CARD);
	}

	/**
	 * 加载卡牌信息
	 * 
	 * @throws Exception
	 */
	public static void loadArtifactCard() throws Exception {
		registerMethod(ARTIFACT_CARD, "loadCardModel");
		ArrayList<Object> arraylist = ExcelXlsLoader.loadArrayListModel(ArtifactCardMapping.class,
				loadFilePath(ARTIFACT_CARD));
		HashMap<String, CardModel> cardModels = ConfigData.cardModels;
		HashMap<String, HashMap<String, CardModel>> cardModelBySubType = ConfigData.cardModelBySubType;
		HashMap<String, HashMap<String, CardModel>> cardModelByQuality = ConfigData.cardModelByQuality;
		for (Object gameData : arraylist) {
			ArtifactCardMapping value = (ArtifactCardMapping) gameData;
			CardModel model = new CardModel();
			model.type = CardModel.ARTIFACT;
			model.ID = value.ID;
			model.Cost = value.Cost;
			model.Genius = value.Genius;
			model.SubType = value.SubType;
			model.Quality = value.Quality;
			model.BuyGold = value.BuyGold;
			model.SellGold = value.SellGold;
			model.Limit = value.Limit;
			model.RuneStr = value.Rune;
			model.IsShow = value.IsShow;
			model.Open = value.Open;
			String rune = String.format("[%s]", value.Rune);
			JSONArray array = (JSONArray) JSONArray.parse(rune);
			model.Rune = new int[array.size()];
			for (int i = 0; i < array.size(); i++) {
				model.Rune[i] = (int) array.get(i);
			}
			cardModels.put(model.ID, model);
			if (model.Open == 0) {
				continue;
			}
			if (!Tools.isEmptyString(value.SubType)) {
				if (cardModelBySubType.get(value.SubType) == null) {
					cardModelBySubType.put(value.SubType, new HashMap<>());
				}
				cardModelBySubType.get(value.SubType).put(model.ID, model);
			}
			if (cardModelByQuality.get(String.valueOf(value.Quality)) == null) {
				cardModelByQuality.put(String.valueOf(value.Quality), new HashMap<>());
			}
			cardModelByQuality.get(String.valueOf(value.Quality)).put(model.ID, model);
		}
		ConfigData.cardModels = cardModels;
		logger.info("{}载入完毕。", ARTIFACT_CARD);
	}

	/**
	 * 加载卡牌组信息
	 * 
	 * @throws Exception
	 */
	public static void loadDeckCard() throws Exception {
		registerMethod(CARD_DECK, "loadDeckCard");
		ArrayList<Object> arraylist = ExcelXlsLoader.loadArrayListModel(CardDeckMapping.class,
				loadFilePath(CARD_DECK));
		HashMap<Integer, CardDeckModel> cardDeckModels = new HashMap<>();

		for (Object gameData : arraylist) {
			CardDeckMapping value = (CardDeckMapping) gameData;
			if ("Open".equalsIgnoreCase(value.CardId)) {
				CardDeckModel model = cardDeckModels.get(value.DeckId);
				model.open = Boolean.valueOf(value.Number == 1);
				continue;
			}
			if (ConfigData.cardModels.get(value.CardId) == null) {
				CardDeckModel model = new CardDeckModel();
				model.DeckId = value.DeckId;
				model.DeckName = value.CardId;
				model.Runes.add(value.Number / 100);
				model.Runes.add((value.Number % 100) / 10);
				model.Runes.add(value.Number % 10);
				cardDeckModels.put(model.DeckId, model);
			} else {
				CardDeckModel model = cardDeckModels.get(value.DeckId);
				String cardId = value.CardId;
				int number = value.Number;
				model.cardIds.put(cardId, number);
			}
		}
		ConfigData.cardDeckModels = cardDeckModels;
		logger.info("{}载入完毕。", CARD_DECK);
	}
	
	/**
	 * 加载初始卡牌信息
	 * 
	 * @throws Exception
	 */
	public static void loadInitCard()throws Exception {
		registerMethod(INIT_CARD, "loadInitCard");
		ArrayList<Object> arraylist = ExcelXlsLoader.loadArrayListModel(InitCardModel.class,
				loadFilePath(INIT_CARD));
		HashMap<Integer, InitCardModel> initCardModels = new HashMap<>();

		for (Object gameData : arraylist) {
			InitCardModel model = (InitCardModel) gameData;
			initCardModels.put(model.ID, model);
		}
		ConfigData.initCardModels = initCardModels;
		logger.info("{}载入完毕。", INIT_CARD);
	}
	
	/**
	 * 加载掉落信息
	 * 
	 * @throws Exception
	 */
	public static void loadDrop() throws Exception {
		registerMethod(DROP, "loadDrop");
		ArrayList<Object> arraylist = ExcelXlsLoader.loadArrayListModel(DropModel.class,
				loadFilePath(DROP));
		HashMap<Integer, Vector<DropModel>> dropModels = new HashMap<>();

		for (Object gameData : arraylist) {
			DropModel model = (DropModel) gameData;
			if (dropModels.get(model.ID) == null) {
				dropModels.put(model.ID, new Vector<DropModel>());
			}
			dropModels.get(model.ID).add(model);
		}
		ConfigData.dropModels = dropModels;
		logger.info("{}载入完毕。", DROP);
	}
	
	/**
	 * 加载虚拟卡包商店
	 * 
	 * @throws Exception
	 */
	public static void loadCardPackShop() throws Exception {
		registerMethod(CARD_PACK_SHOP, "loadCardPackShop");
		ArrayList<Object> arraylist = ExcelXlsLoader.loadArrayListModel(CardPackShopModel.class,
				loadFilePath(CARD_PACK_SHOP));
		HashMap<Integer, CardPackShopModel> cardPackShopModels = new HashMap<>();

		for (Object gameData : arraylist) {
			CardPackShopModel model = (CardPackShopModel) gameData;
			cardPackShopModels.put(model.ID, model);
		}
		ConfigData.cardPackShopModels = cardPackShopModels;
		logger.info("{}载入完毕。", CARD_PACK_SHOP);
	}
	
	/**
	 * 加载真实卡包商店
	 * 
	 * @throws Exception
	 */
	public static void loadRealCardPackShop() throws Exception {
		registerMethod(REAL_CARD_PACK_SHOP, "loadRealCardPackShop");
		ArrayList<Object> arraylist = ExcelXlsLoader.loadArrayListModel(RealCardPackShopModel.class,
				loadFilePath(REAL_CARD_PACK_SHOP));
		HashMap<Integer, RealCardPackShopModel> realCardPackShopModels = new HashMap<>();

		for (Object gameData : arraylist) {
			RealCardPackShopModel model = (RealCardPackShopModel) gameData;
			realCardPackShopModels.put(model.ID, model);
		}
		ConfigData.realCardPackShopModels = realCardPackShopModels;
		logger.info("{}载入完毕。", REAL_CARD_PACK_SHOP);
	}
	
	
	/**
	 * 加载天梯配置表
	 * 
	 * @throws Exception
	 */
	public static void loadLadderUp() throws Exception {
		registerMethod(LADDER_UP, "loadLadderUp");
		ArrayList<Object> arraylist = ExcelXlsLoader.loadArrayListModel(LadderUpModel.class,
				loadFilePath(LADDER_UP));
		HashMap<Integer, LadderUpModel> ladderUpModels = new HashMap<>();

		for (Object gameData : arraylist) {
			LadderUpModel model = (LadderUpModel) gameData;
			ladderUpModels.put(model.LadderID, model);
		}
		ConfigData.ladderUpModels = ladderUpModels;
		logger.info("{}载入完毕。", LADDER_UP);
	}
	
	/**
	 * 天梯降段配置表
	 * @throws Exception 
	 */
	public static void loadLadderDown() throws Exception {
		registerMethod(LADDER_DOWN, "loadLadderDown");
		ArrayList<Object> arraylist = ExcelXlsLoader.loadArrayListModel(LadderDownModel.class,
				loadFilePath(LADDER_DOWN));
		HashMap<Integer, LadderDownModel> ladderDownModels = new HashMap<>();

		for (Object gameData : arraylist) {
			LadderDownModel model = (LadderDownModel) gameData;
			ladderDownModels.put(model.LadderID, model);
		}
		ConfigData.ladderDownModels = ladderDownModels;
		logger.info("{}载入完毕。", LADDER_DOWN);
	}
	
	/**
	 * 赛季时间配置
	 * @throws Exception
	 */
	public static void loadMatchTime() throws Exception {
		registerMethod(MATCH_TIME, "loadMatchTime");
		ArrayList<Object> arraylist = ExcelXlsLoader.loadArrayListModel(MatchTimeModel.class,
				loadFilePath(MATCH_TIME));
		HashMap<Integer, MatchTimeModel> matchTimeModels = new HashMap<>();

		for (Object gameData : arraylist) {
			MatchTimeModel model = (MatchTimeModel) gameData;
			matchTimeModels.put(model.ID, model);
		}
		ConfigData.matchTimeModels = matchTimeModels;
		logger.info("{}载入完毕。", MATCH_TIME);
	}

	/**
	 * 任务配置表
	 * @throws Exception
	 */
	public static void loadQuest() throws Exception {
		registerMethod(DAILY_QUEST, "loadQuest");
		ArrayList<Object> arraylist = ExcelXlsLoader.loadArrayListModel(QuestModel.class,
				loadFilePath(DAILY_QUEST));
		HashMap<Integer, QuestModel> questModels = new HashMap<>();
		HashMap<Integer, ArrayList<QuestModel>> questModelsByLevel = new HashMap<>();
		for (int i = 1; i <= 4; i++) {
			questModelsByLevel.put(i, new ArrayList<>());
		}
		
		for (Object gameData : arraylist) {
			QuestModel model = (QuestModel) gameData;
			questModels.put(model.ID, model);
			if (questModelsByLevel.get(model.Level) == null) {
				questModelsByLevel.put(model.Level, new ArrayList<>());
			}
			questModelsByLevel.get(model.Level).add(model);
		}
		ConfigData.questModels = questModels;
		ConfigData.questModelsByLevel = questModelsByLevel;
		logger.info("{}载入完毕。", DAILY_QUEST);
	}

	/**
	 * 可用次数配置表
	 * @throws Exception
	 */
	public static void loadUseCount() throws Exception {
		registerMethod(USE_COUNT, "loadUseCount");
		ArrayList<Object> arraylist = ExcelXlsLoader.loadArrayListModel(UseCountModel.class,
				loadFilePath(USE_COUNT));
		HashMap<Integer, Integer> useCountModels = new HashMap<>();
		
		for (Object gameData : arraylist) {
			UseCountModel model = (UseCountModel) gameData;
			useCountModels.put(model.Type, model.Value);
		}
		ConfigData.useCountModels = useCountModels;
		logger.info("{}载入完毕。", USE_COUNT);
	}

	/**
	 * 奖励配置表
	 * @throws Exception
	 */
	public static void loadAward() throws Exception {
		registerMethod(AWARD, "loadAward");
		ArrayList<Object> arraylist = ExcelXlsLoader.loadArrayListModel(AwardModel.class,
				loadFilePath(AWARD));
		HashMap<String, AwardModel> awardModels = new HashMap<>();
		
		for (Object gameData : arraylist) {
			AwardModel model = (AwardModel) gameData;
			awardModels.put(model.AwardId, model);
		}
		ConfigData.awardModels = awardModels;
		logger.info("{}载入完毕。", AWARD);
	}
	
	/**
	 * 子任务配置表
	 * @throws Exception
	 */
	public static void loadSubQuest() throws Exception {
		registerMethod(QUEST_SUB, "loadSubQuest");
		ArrayList<Object> arraylist = ExcelXlsLoader.loadArrayListModel(QuestSubModel.class,
				loadFilePath(QUEST_SUB));
		HashMap<Integer, QuestSubModel> questSubModels = new HashMap<>();

		for (Object gameData : arraylist) {
			QuestSubModel model = (QuestSubModel) gameData;
			questSubModels.put(model.ID, model);
		}
		ConfigData.questSubModels = questSubModels;
		logger.info("{}载入完毕。", QUEST_SUB);
	}
	
	/**
	 * 签到奖励表
	 * @throws Exception
	 */
	public static void loadSignInQuest() throws Exception {
		registerMethod(SIGNIN_QUEST, "loadSignInQuest");
		ArrayList<Object> arraylist = ExcelXlsLoader.loadArrayListModel(SignInQuestModel.class,
				loadFilePath(SIGNIN_QUEST));
		HashMap<Integer, SignInQuestModel> signInModels = new HashMap<>();

		for (Object gameData : arraylist) {
			SignInQuestModel model = (SignInQuestModel) gameData;
			signInModels.put(model.Days, model);
		}
		ConfigData.signInModels = signInModels;
		logger.info("{}载入完毕。", SIGNIN_QUEST);
	}
	
	/**
	 * 新手关卡配置表
	 * 
	 * @throws Exception
	 */
	public static void loadGuideGate() throws Exception {
		registerMethod(GUIDE_GATE, "loadGuideGate");
		HashMap<Integer, GuideGateModel> guideGateModels = new HashMap<>();

		ArrayList<Object> arraylist = ExcelXlsLoader.loadArrayListModel(GuideGateModel.class, loadFilePath(GUIDE_GATE));
		for (Object gameData : arraylist) {
			GuideGateModel model = (GuideGateModel) gameData;
			guideGateModels.put(model.ID, model);
		}
		ConfigData.guideGateModels = guideGateModels;
		logger.info("{}载入完毕。", GUIDE_GATE);
	}

	/**
	 * 新手关卡卡组配置表
	 * 
	 * @throws Exception
	 */
	public static void loadGuideGateCardGroup() throws Exception {
		registerMethod(GUIDE_GATE_CARDGROUP, "loadGuideGateCardGroup");
		HashMap<Integer, GuideGateCardGroupModel> guideGateCardGroupModels = new HashMap<>();

		ArrayList<Object> arraylist = ExcelXlsLoader.loadArrayListModel(GuideGateCardGroupMapping.class, loadFilePath(GUIDE_GATE_CARDGROUP));
		for (Object gameData : arraylist) {
			GuideGateCardGroupMapping model = (GuideGateCardGroupMapping) gameData;
			if (guideGateCardGroupModels.get(model.GroupID) == null) {
				GuideGateCardGroupModel groupModel = new GuideGateCardGroupModel();
				groupModel.GroupID = model.GroupID;
				groupModel.Runes.add(model.CardID / 100);
				groupModel.Runes.add((model.CardID % 100) / 10);
				groupModel.Runes.add(model.CardID % 10);
				guideGateCardGroupModels.put(model.GroupID, groupModel);
			} else {
				GuideGateCardGroupModel groupModel = guideGateCardGroupModels.get(model.GroupID);
				GuideCardModel guideCard= new GuideCardModel();
				guideCard.CardID = model.CardID;
				guideCard.PathFinder = model.PathFinder;
				groupModel.cards.add(guideCard);
			}
		}
		ConfigData.guideGateCardGroupModels = guideGateCardGroupModels;
		logger.info("{}载入完毕。", GUIDE_GATE_CARDGROUP);
	}
	
	public static void loadGuideGateAI() throws Exception {
		registerMethod(GUIDE_GATE_AI, "loadGuideGateAI");
		HashMap<Integer, Vector<GuideGateAIModel>> guideGateAIModels = new HashMap<>();

		ArrayList<Object> arraylist = ExcelXlsLoader.loadArrayListModel(GuideGateAIModel.class, loadFilePath(GUIDE_GATE_AI));
		for (Object gameData : arraylist) {
			GuideGateAIModel model = (GuideGateAIModel) gameData;
			if (guideGateAIModels.get(model.ID) == null) {
				guideGateAIModels.put(model.ID, new Vector<GuideGateAIModel>());
			}
			guideGateAIModels.get(model.ID).add(model);
		}
		ConfigData.guideGateAIModels = guideGateAIModels;
		logger.info("{}载入完毕。", GUIDE_GATE_AI);
	}
	
	/**
	 * 机器人配置表
	 * 
	 * @throws Exception
	 */
	public static void loadRobot() throws Exception {
		registerMethod(ROBOT, "loadRobot");
		HashMap<Integer, RobotModel> robotModels = new HashMap<>();

		ArrayList<Object> arraylist = ExcelXlsLoader.loadArrayListModel(RobotModel.class, loadFilePath(ROBOT));
		for (Object gameData : arraylist) {
			RobotModel model = (RobotModel) gameData;
			robotModels.put(model.ID, model);
		}
		ConfigData.robotModels = robotModels;
		logger.info("{}载入完毕。", ROBOT);
	}

	/**
	 * 机器人卡组配置表
	 * 
	 * @throws Exception
	 */
	public static void loadRobotCardGroup() throws Exception {
		registerMethod(ROBOT_CARDGROUP, "loadRobotCardGroup");
		HashMap<Integer, RobotCardGroupModel> robotCardGroupModels = new HashMap<>();

		ArrayList<Object> arraylist = ExcelXlsLoader.loadArrayListModel(RobotCardGroupMapping.class, loadFilePath(ROBOT_CARDGROUP));
		for (Object gameData : arraylist) {
			RobotCardGroupMapping model = (RobotCardGroupMapping) gameData;
			if (robotCardGroupModels.get(model.GroupID) == null) {
				RobotCardGroupModel groupModel = new RobotCardGroupModel();
				groupModel.GroupID = model.GroupID;
				groupModel.Runes.add(model.CardID / 100);
				groupModel.Runes.add((model.CardID % 100) / 10);
				groupModel.Runes.add(model.CardID % 10);
				robotCardGroupModels.put(model.GroupID, groupModel);
			} else {
				RobotCardGroupModel groupModel = robotCardGroupModels.get(model.GroupID);
				RobotCardModel robotCard= new RobotCardModel();
				robotCard.CardID = model.CardID;
				robotCard.PathFinder = model.PathFinder;
				groupModel.cards.add(robotCard);
			}
		}
		ConfigData.robotCardGroupModels = robotCardGroupModels;
		logger.info("{}载入完毕。", ROBOT_CARDGROUP);
	}

	/**
	 * 加载错误代码配置表
	 * 
	 * @throws Exception
	 */
	public static void loadErrorMessage() throws Exception {
		registerMethod(ERROR_MESSAGE, "loadErrorMessage");
		HashMap<String, Integer> errorCode = new HashMap<>();
		HashMap<Integer, String> errorMessage = new HashMap<>();

		ArrayList<Object> arraylist = ExcelXlsLoader.loadArrayListModel(ErrorCode.class, loadFilePath(ERROR_MESSAGE));
		for (Object gameData : arraylist) {
			ErrorCode value = (ErrorCode) gameData;
			errorCode.put(value.errorCode, value.id);
			errorMessage.put(value.id, value.message);
		}
		ConfigData.errorCode = errorCode;
		ConfigData.errorMessage = errorMessage;
		logger.info("{}载入完毕。", ERROR_MESSAGE);
	}

	/**
	 * 弹出消息配置表
	 * 
	 * @throws Exception
	 */
	public static void loadMessageBox() throws Exception {
		registerMethod(MESSAGE_BOX, "loadMessageBox");
		HashMap<Integer, String> messageBox = new HashMap<>();

		ArrayList<Object> arraylist = ExcelXlsLoader.loadArrayListModel(MessageBox.class, loadFilePath(MESSAGE_BOX));
		for (Object gameData : arraylist) {
			MessageBox value = (MessageBox) gameData;
			messageBox.put(value.id, value.message);
		}
		ConfigData.messageBox = messageBox;
		logger.info("{}载入完毕。", MESSAGE_BOX);
	}

	/**
	 * 
	 * 加载参数配置表
	 * 
	 * @throws Exception
	 */
	public static void loadArguments() throws Exception {
		registerMethod(ARGUMENTS, "loadArguments");
		HashMap<String, Integer> arguments = new HashMap<>();
		HashMap<String, String> argumentsByString = new HashMap<>();

		ArrayList<Object> arraylist = ExcelXlsLoader.loadArrayListModel(ArgumentsModel.class, loadFilePath(ARGUMENTS));
		for (Object gameData : arraylist) {
			ArgumentsModel value = (ArgumentsModel) gameData;
			try {
				int i = Integer.parseInt(value.Value);
				arguments.put(value.ArgId, i);
			} catch (NumberFormatException e) {
				argumentsByString.put(value.ArgId, value.Value);
			}
		}

		ConfigData.arguments = arguments;
		ConfigData.argumentsByString = argumentsByString;
		logger.info("{}载入完毕。", ARGUMENTS);
	}

	/**
	 * 字符串分割
	 * 
	 * @param str
	 * @return
	 */
	@SuppressWarnings("unused")
	private static ArrayList<Integer> stringSpilt(String str) {
		ArrayList<Integer> times = new ArrayList<>();

		String Spstr = null;
		while (str.indexOf(SPLIT_STRING) != -1) {
			Spstr = str.substring(0, str.indexOf(SPLIT_STRING));
			str = str.substring(Spstr.length() + 1);
			times.add(Integer.parseInt(Spstr));
		}
		if (str.length() > 0) {
			times.add(Integer.parseInt(str));
		}

		return times;
	}

	/**
	 * 获取载入文件路径
	 * 
	 * @param configPath
	 * @return
	 * @throws Exception
	 */
	private static String loadFilePath(String configPath) throws Exception {
		String parentPath = ConfigData.getConfigPath();
		String returnPath = "";
		if (parentPath != null) {
			String configTemp = configPath;
			if (configPath.indexOf("/") != -1) {
				configTemp = configPath.substring(configPath.lastIndexOf("/"));
			}
			returnPath = parentPath + ConfigLoader.path + configTemp;
		}
		File file = new File(returnPath);
		if (!file.exists()) {
			returnPath = parentPath + configPath;
		}
		return returnPath;
	}

	/**
	 * 载入文件
	 * 
	 * @param configPath
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
	private static String loadFile(String configPath) throws Exception {
		String txtStr = null;
		String parentPath = ConfigData.getConfigPath();
		;
		if (parentPath != null) {
			configPath = parentPath + File.separator + configPath;
		}
		txtStr = FileUtils.readFileToString(new File(configPath), "UTF8");
		return txtStr;
	}

	public static void setServerInfo(String startServerTime, String platform) {
		ConfigLoader.startServerTime = startServerTime;
	}

	private static void registerMethod(String key, String value) {
		if (key.lastIndexOf("/") != -1) {
			reloadMapping.put(key.substring(key.lastIndexOf("/") + 1, key.length()), value);
		} else {
			reloadMapping.put(key, value);
		}
	}
}
