package config;

import java.util.HashMap;

import config.model.HeroModel;
import config.model.SceneModel;
import config.model.recharge.RechargeModel;

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

	/** Hero信息 */
	public static HashMap<Integer, HeroModel> heroModels;
	/** Scene信息 */
	public static HashMap<Integer, SceneModel> sceneModels;
	
	public static String getConfigPath() {
		return configPath;
	}

	public static void setConfigPath(String configPath) {
		ConfigData.configPath = configPath;
	}
}
