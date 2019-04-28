package net;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import util.FileUtils;

public class LanguageKit {
	/** 通用语言表 */
	public static Map<String, String> languageMap = new HashMap<String, String>();

	/** code为键值通用语言表 */
	public static Map<Integer, String> codelanguageMap = new HashMap<Integer, String>();

	/** 显示类型 */
	public static Map<String, Integer> showTypeMap = new HashMap<String, Integer>();

	/** 初始化语言表 */
	public static void init() {
		String path = System.getProperty("configPath") 
				+ "programmer/language.map";
		if (!new File(path).exists()) {
			return;
		}
		String serveletConfig = FileUtils.readStringFileByUTF(path);
		String[] strings = serveletConfig.split("\r\n");
		for (int i = 1; i < strings.length; i++) {
			String[] keyValue = strings[i].split("\t");
			if (keyValue.length == 2 && keyValue[0] != null
					&& keyValue[1] != null) {
				languageMap.put(keyValue[0].trim(), keyValue[0].trim());
				codelanguageMap.put(keyValue[0].trim().hashCode(),
						keyValue[0].trim());
			} else if (keyValue.length == 3 && keyValue[0] != null
					&& keyValue[1] != null && keyValue[2] != null) {
				languageMap.put(keyValue[0].trim(), keyValue[0].trim());
				codelanguageMap.put(keyValue[0].trim().hashCode(),
						keyValue[0].trim());
				showTypeMap.put(keyValue[0].trim(),
						Integer.valueOf(keyValue[2].trim()));
			} else {
				if (strings[i].startsWith("#")) {
					continue;
				}
				if ("".equals(strings[i].trim())) {
					continue;
				}
			}
		}
	}

	/** 把错误信息写到传入的data中 */
	public static void getlanguage(IByteBuffer data, String key, String... args) {
		if (languageMap == null) {
			return;
		}
		// if (languageMap.size() == 0)
		// {
		// return;
		// }
		data.writeInt(getCode(key));
		if (args == null) {
			data.writeByte(0);
			return;
		}
		data.writeByte(args.length);
		for (String v : args) {
			data.writeUTF(v);
		}

	}

	public static int getShowType(String key) {
		if (showTypeMap == null) {
			return 1;
		}
		if (!showTypeMap.containsKey(key)) {
			return 1;
		}
		return showTypeMap.get(key);
	}

	/**
	 * 从data序列化得到一个exception
	 * 
	 * @param data
	 * @param type
	 * @return
	 */
	public static DataAccessException readException(IByteBuffer data, int type) {
		int code = data.readInt();
		String key = codelanguageMap.get(code);

		int argsLen = data.readByte();

		String[] args = new String[argsLen];
		for (int i = 0; i < argsLen; i++) {
			args[i] = data.readUTF();
		}
		return new DataAccessException(type, key, args);

	}

	/** 得到国际化语言 */
	public static String getlanguage(String key) {
		if (languageMap == null || key == null) {
			return "system.unknow";
		}
		if (languageMap.size() == 0) {
			return "system.unknow";
		}
		if (!languageMap.containsKey(key)) {
			return "system.unknow";
		}
		return languageMap.get(key);

	}

	/** 得到错误信息码的行数，根据行数来确定错误信息的内容 */
	protected static int getCode(String key) {
		if (key == null) {
			return "system.unknow".hashCode();
		}
		if (!languageMap.containsKey(key)) {

			return "system.unknow".hashCode();
		}

		return key.hashCode();
	}
}