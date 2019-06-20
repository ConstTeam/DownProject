package config;

import java.util.HashMap;

import config.model.HeroModel;
import config.model.SceneModel;
import config.model.recharge.RechargeModel;

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

	/** Hero��Ϣ */
	public static HashMap<Integer, HeroModel> heroModels;
	/** Scene��Ϣ */
	public static HashMap<Integer, SceneModel> sceneModels;
	
	public static String getConfigPath() {
		return configPath;
	}

	public static void setConfigPath(String configPath) {
		ConfigData.configPath = configPath;
	}
}
