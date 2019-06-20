package config;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import config.model.ArgumentsModel;
import config.model.HeroModel;
import config.model.SceneModel;
import config.model.message.ErrorCode;
import config.model.message.MessageBox;
import util.ErrorPrint;
import util.ExcelXlsLoader;

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

	/** 错误代码 */
	private static final String ERROR_MESSAGE = "Message/ErrorMessage_Server.xls";
	/** 弹出消息 */
	private static final String MESSAGE_BOX = "Message/MessageBox_Server.xls";
	/** 参数配置表 */
	private static final String ARGUMENTS = "InitValues_Common.xls";

	/** 角色配置表 */
	private static final String HERO_COMMON = "Hero_Common.xls";
	/** 场景配置表 */
	private static final String SCENE_COMMON = "Scene_Common.xls";

	public static HashMap<String, String> reloadMapping = new HashMap<>();
	public static String startServerTime = "";
	private static String path = "";

	public static boolean load() {
		try {
			loadArguments();
			loadMessageBox();
			loadErrorMessage();
			loadHeroCommon();
			loadSceneCommon();
			ConfigCheck.check();			
		} catch (Exception e) {
			flag = false;
			ErrorPrint.print(e);
		}
		return flag;
	}
	
	/**
	 * Hero配置表
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
		logger.info("{}载入完毕。", HERO_COMMON);
	}
	
	/**
	 * Scene配置表
	 * @throws Exception
	 */
	public static void loadSceneCommon() throws Exception {
		registerMethod(SCENE_COMMON, "loadSceneCommon");
		ArrayList<Object> arraylist = ExcelXlsLoader.loadArrayListModel(SceneModel.class,
				loadFilePath(SCENE_COMMON));
		HashMap<Integer, SceneModel> sceneModels = new HashMap<>();
		
		for (Object gameData : arraylist) {
			SceneModel model = (SceneModel) gameData;
			sceneModels.put(model.ID, model);
		}
		ConfigData.sceneModels = sceneModels;
		logger.info("{}载入完毕。", SCENE_COMMON);
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
