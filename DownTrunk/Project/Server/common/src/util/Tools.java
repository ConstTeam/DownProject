package util;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import config.model.DrawModel;

public class Tools {

	private static final Logger logger = LoggerFactory.getLogger(Tools.class);

	/** 汉字正则表达式 */
	private static String regexIsHanZi = "[\\u4e00-\\u9fa5]+";
	/** 身份证号正则表达式 */
	private static String regexIsIdCardNumber = "([0-9]{17}([0-9]|X))|([0-9]{15})";

	public static double expression(String expression, Object a, Object b, Object c, Object d, Object e, Object f,
			Object g, Object h, Object i) {

		// 升级数值公式（A上限值，B初始值，C最高等级，D当前等级）
		// 突破上限数值公式（E最高上限突破，F当前上限突破，D装备当前等级，G升级数值公式）
		expression = expression.replaceAll("A", a.toString());
		expression = expression.replaceAll("B", b.toString());
		expression = expression.replaceAll("C", c.toString());
		expression = expression.replaceAll("D", d.toString());
		expression = expression.replaceAll("E", e.toString());
		expression = expression.replaceAll("F", f.toString());
		expression = expression.replaceAll("G", g.toString());
		expression = expression.replaceAll("H", h.toString());
		expression = expression.replaceAll("I", i.toString());

		ScriptEngine jse = new ScriptEngineManager().getEngineByName("JavaScript");
		double eval;
		try {
			eval = Double.parseDouble(jse.eval(expression).toString());
			eval += 0.0000001; // 补正误差值
			eval = Math.floor(eval * 100) / 100;
			return eval;
		} catch (ScriptException e1) {
			logger.error("升级公式转换出错！公式为：" + expression);
			ErrorPrint.print(e1);
			return -1;
		}
	}

	/**
	 * 判断是否为空字符串
	 * 
	 * @param value
	 * @return
	 */
	public static boolean isEmptyString(String value) {
		return value == null || value.length() <= 0;
	}
	
	public static boolean random() {
		int random = random(0, 1);
		return random == 1;
	}

	public static int random(int min, int max) {
		return new Random().nextInt(max - min + 1) + min;
	}

	public static boolean rate(float rate, int min, int max) {

		rate = rate * max;
		if (rate == min)
			return false;
		if (rate == max)
			return true;

		int random = new Random().nextInt(max) + min;
		if (random <= rate) {
			return true;
		} else {
			return false;
		}
	}

	public static ArrayList<String> stringSpilt(String str, String spiltStr) {
		ArrayList<String> strs = new ArrayList<>();

		String Spstr;
		while (str.indexOf(spiltStr) != -1) {
			Spstr = str.substring(0, str.indexOf(spiltStr));
			str = str.substring(Spstr.length() + 1);
			strs.add(Spstr);
		}
		strs.add(str);

		return strs;
	}
	
	public static boolean isNumber(String str) {
		try {
			Integer.parseInt(str);
			return true;
		} catch (Exception e) {
		}
		return false;
	}

	public static String md5(String source) {
		StringBuffer md5StrBuff = new StringBuffer();
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.reset();
			digest.update(source.getBytes("utf-8"));
			byte[] byteArray = digest.digest();
			String tmp = "";
			for (int i = 0; i < byteArray.length; i++) {
				tmp = Integer.toHexString(0xFF & byteArray[i]);
				if (tmp.length() == 1)
					md5StrBuff.append("0").append(tmp);
				else
					md5StrBuff.append(tmp);
			}

		} catch (Exception e) {
			ErrorPrint.print(e);
		}

		return md5StrBuff.toString();
	}

	public static boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}

	public static int getTotalValue(ArrayList<DrawModel> drawModels) {

		int total = 0;
		for (DrawModel drawModel : drawModels) {
			total += drawModel.value;
		}
		return total;
	}

	public static DrawModel getRandomDraw(ArrayList<DrawModel> drawModels, int totalValue) {
		int random = Tools.random(1, totalValue);
		Integer value = 0;
		for (DrawModel drawModel : drawModels) {
			value += drawModel.value;

			if (random <= value) {
				return drawModel;
			}
		}
		return null;
	}

	public static DrawModel getRandomDraw(ArrayList<DrawModel> drawModels) {
		return getRandomDraw(drawModels, Tools.getTotalValue(drawModels));
	}

	/**
	 * 按照权重随机一个对象
	 * 
	 * @param objList
	 * @param objWeight
	 * @return
	 */
	public static <T> T randomWeightObject(List<T> objList, List<Integer> objWeight) {
		if (objList == null || objWeight == null || objList.size() != objWeight.size()) {
			throw new RuntimeException("random weight object exception");
		}

		// 累加权重
		int totalWeight = 0;
		List<Integer> fmtWeight = new ArrayList<Integer>(objWeight.size());
		for (int i = 0; i < objWeight.size(); i++) {
			totalWeight += objWeight.get(i);
			fmtWeight.add(totalWeight);
		}

		int randomWeight = random(1, totalWeight);
		for (int i = 0; i < fmtWeight.size(); i++) {
			if (randomWeight <= fmtWeight.get(i)) {
				return objList.get(i);
			}
		}
		return null;
	}

	public static <T> T fromJson(String jsonInfo, Class<T> t) {
		try {
			JSONObject json = JSON.parseObject(jsonInfo);
			return JSON.toJavaObject(json, t);
		} catch (Exception e) {
			ErrorPrint.print(e);
		}
		return null;
	}

	public static HashMap<String, Object> split(String rule) {
		String[] split = rule.split("\\|", -1);
		HashMap<String, Object> info = new HashMap<>();
		for (String str : split) {
			String[] s = str.split(",", -1);
			int d = s.length;
			if (d == 2) {
				info.put(s[0], s[1]);
			} else {
				info.put(s[0], s);
			}
		}
		return info;
	}

	public static int[] splitToInt(String rule) throws NumberFormatException {
		String[] split = rule.split("\\|", -1);
		int[] values = new int[split.length];

		for (int i = 0; i < split.length; i++) {
			values[i] = Integer.parseInt(split[i]);
		}
		return values;
	}

	public static String[] splitToString(String rule) {
		String[] split = rule.split("\\|", -1);
		String[] values = new String[split.length];

		for (int i = 0; i < split.length; i++) {
			values[i] = split[i];
		}
		return values;
	}

	public static int[] splitBySlash(String rule) throws NumberFormatException {
		if (rule.indexOf("/") != -1) {
			String[] split = rule.split("/", -1);
			int[] values = new int[split.length];
	
			for (int i = 0; i < split.length; i++) {
				values[i] = Integer.parseInt(split[i]);
			}
			return values;
		} else {
			String[] split = rule.split("-", -1);
			int min = Integer.parseInt(split[0]);
			int max = Integer.parseInt(split[1]);
			int[] values = new int[max - min + 1];
	
			for (int i = 0; i < values.length; i++) {
				values[i] = min + i;
			}
			return values;
		}
	}

	public static boolean validatorIdCardNumber(String id) {
		boolean matchRegular = matchRegular(id, regexIsIdCardNumber);
		if (!matchRegular)
			logger.info("身份证号验证失败");
		return matchRegular;
	}
	
	public static boolean validatorIdCardName(String name) {
		boolean matchRegular = matchRegular(name, regexIsHanZi);
		if (!matchRegular)
			logger.info("身份证姓名验证失败");
		return matchRegular;
	}

	/**
	 * 正则表达式
	 * 
	 * @param str要匹配的字符串
	 * @param reg正则
	 * @return
	 */
	public static boolean matchRegular(String str, String reg) {
		Pattern pattern = Pattern.compile(reg);
		Matcher matcher = pattern.matcher(str);
		boolean matches = matcher.matches();
		return matches;
	}
	
	public static String encodeToUTF8(String key) {
		try {
			return URLEncoder.encode(key, "utf-8");
		} catch (Exception e) {
			return "";
		}

	}
	
	public static String decodeToUTF8(String key) {
		try {
			return URLDecoder.decode(key, "utf-8");
		} catch (Exception e) {
			return "";
		}

	}
}
