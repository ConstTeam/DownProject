package config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.model.ArgumentsModel;
import config.model.HeroModel;
import config.model.UseCountModel;
import config.model.message.ErrorCode;
import config.model.message.MessageBox;
import config.model.quest.AwardModel;
import config.model.skill.SkillModel;
import util.ErrorPrint;
import util.ExcelXlsLoader;
import util.Tools;

/**
 * �����ļ�����
 * 
 */
public class ConfigLoader {

	private static final Logger logger = LoggerFactory.getLogger(ConfigLoader.class);

	public static boolean flag = true;
	/** �ַ����ָ��� */
	private static final String SPLIT_STRING = ";";
	/** �ַ����ָ��� */
	public static final String SPLIT_STRING_COMMA = ",";

	private static final String SKILL = "Card/Skill_Common.xls";
	/** ������� */
	private static final String ERROR_MESSAGE = "Message/ErrorMessage_Server.xls";
	/** ������Ϣ */
	private static final String MESSAGE_BOX = "Message/MessageBox_Server.xls";
	/** �������ñ� */
	private static final String ARGUMENTS = "InitValues_Common.xls";

	/** ��ɫ���ñ� */
	private static final String HERO_COMMON = "Hero_Common.xls";

	/** ���ô��� */
	private static final String USE_COUNT = "UseCount_Common.xls";
	/** ���ô��� */
	private static final String AWARD = "Award_Common.xls";
	
	public static HashMap<String, String> reloadMapping = new HashMap<>();
	public static String startServerTime = "";
	private static String path = "";

	public static boolean load() {
		try {
			loadArguments();
			loadMessageBox();
			loadErrorMessage();
			loadHeroCommon();
			ConfigCheck.check();			
		} catch (Exception e) {
			flag = false;
			ErrorPrint.print(e);
		}
		return flag;
	}


	/**
	 * ���ؼ�����Ϣ
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
		logger.info("{}������ϡ�", SKILL);
	}

	private static String getValueArg(String value) {
		int begin = value.indexOf("[");
		int end = value.indexOf("]");
		if (begin == -1) {
			if (end != -1) {
				logger.error("Skill_Common���ñ�Value��д����" + value, new Throwable());
			}
			return value;
		}
		if (end == -1) {
			logger.error("Skill_Common���ñ�Value��д����" + value, new Throwable());
			return value;
		}
		value = value.substring(begin + 1, end);
		return value;
	}
	
	/**
	 * ���ô������ñ�
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
		logger.info("{}������ϡ�", USE_COUNT);
	}
	
	/**
	 * Hero���ñ�
	 * @throws Exception
	 */
	public static void loadHeroCommon() throws Exception {
		registerMethod(HERO_COMMON, "loadHeroCommon");
		ArrayList<Object> arraylist = ExcelXlsLoader.loadArrayListModel(HeroModel.class,
				loadFilePath(HERO_COMMON));
		HashMap<Integer, HeroModel> heroModels = new HashMap<>();
		
		for (Object gameData : arraylist) {
			HeroModel model = (HeroModel) gameData;
			heroModels.put(model.ID, model);
		}
		ConfigData.heroModels = heroModels;
		logger.info("{}������ϡ�", USE_COUNT);
	}

	/**
	 * �������ñ�
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
		logger.info("{}������ϡ�", AWARD);
	}
	
	/**
	 * ���ش���������ñ�
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
		logger.info("{}������ϡ�", ERROR_MESSAGE);
	}

	/**
	 * ������Ϣ���ñ�
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
		logger.info("{}������ϡ�", MESSAGE_BOX);
	}

	/**
	 * 
	 * ���ز������ñ�
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
		logger.info("{}������ϡ�", ARGUMENTS);
	}

	/**
	 * �ַ����ָ�
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
	 * ��ȡ�����ļ�·��
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
	 * �����ļ�
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
