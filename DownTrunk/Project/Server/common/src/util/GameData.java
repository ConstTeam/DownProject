package util;

import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GameData implements Cloneable {
	/**
	 * 设置对象的值
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean setValue(String key, String value) {
		try {
			if (key.equals("NO_USE")) {
				return false;
			}
			Field field = getClass().getField(key);
			Class<?> class1 = field.getType();
			if (class1 == Integer.TYPE)
				field.setInt(this, Integer.parseInt(value));
			else if (class1 == Integer.class)
				field.set(this, Integer.valueOf(value));
			else if (class1 == Long.TYPE)
				field.setLong(this, Long.parseLong(value));
			else if (class1 == Long.class)
				field.set(this, Long.valueOf(value));
			else if (class1 == Boolean.TYPE)
				field.setBoolean(this, Boolean.getBoolean(value));
			else if (class1 == Boolean.class)
				field.set(this, Boolean.valueOf(value));
			else if (class1 == Float.TYPE)
				field.setFloat(this, Float.valueOf(value).floatValue());
			else if (class1 == Float.class)
				field.set(this, Float.valueOf(value));
			else if (class1 == Double.TYPE)
				field.setDouble(this, Double.valueOf(value).doubleValue());
			else if (class1 == Double.class)
				field.set(this, Double.valueOf(value));
			else if (class1 == (java.lang.String.class))
				field.set(this, value);
			else if (class1 == Date.class) {
				SimpleDateFormat sdf = new SimpleDateFormat(
						"yyyy-MM-dd hh:mm:ss");
				try {
					field.set(this, sdf.parse(value));
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
			return true;
		} catch (NoSuchFieldException nosuchfieldexception) {
			nosuchfieldexception.printStackTrace();
		} catch (SecurityException securityexception) {
			securityexception.printStackTrace();
		} catch (IllegalAccessException illegalaccessexception) {
			illegalaccessexception.printStackTrace();
		}
		return false;
	}

	/**
	 * 初始化多个对象
	 * 
	 * @param s
	 * @param GameDataClass
	 * @param arraylist
	 */
	public static final ArrayList<GameData> initObjects(String s,
			Class<? extends GameData> GameDataClass) {
		ArrayList<GameData> arraylist = new ArrayList<>();
		String as[] = s.split("\r\n");
		String as1[] = as[1].split("\t");
		for (int i = 2; i < as.length; i++) {
			if (as[i].length() < 1 && i < as.length - 1) {
				System.err.println(GameDataClass.getSimpleName()
						+ " read the configuration file data "
						+ "failed on line " + (i + 1)
						+ ", because the file does "
						+ "not match the number of data and the "
						+ "number of columms");
				continue;
			}
			String as2[] = as[i].split("\t");
			if (as2.length < 1 || null == as2[0] || as2[0].equals("")) {
				System.err.println(GameDataClass.getSimpleName()
						+ " read the configuration file data "
						+ "failed on line " + (i + 1)
						+ ", because this line of data or "
						+ "incomplete data length of zero");
				continue;
			}
			GameData gameobject = null;
			try {
				gameobject = GameDataClass.newInstance();
			} catch (IllegalAccessException illegalaccessexception) {
				illegalaccessexception.printStackTrace();
			} catch (InstantiationException instantiationexception) {
				instantiationexception.printStackTrace();
			}
			if (gameobject == null)
				break;
			initObject(gameobject, as1, as2);
			arraylist.add(gameobject);
		}
		return arraylist;
	}

	/**
	 * 初始化单个对象的值
	 * 
	 * @param gameobject
	 * @param as
	 *            属性
	 * @param as1
	 *            值
	 * 
	 */
	public static void initObject(GameData gameobject, String as[],
			String as1[]) {
		for (int i = 0; i < as1.length; i++)
			if (as1[i].length() > 0) {
				if (as1[i].length() > 2 && as1[i].charAt(0) == '"'
						&& as1[i].charAt(as1[i].length() - 1) == '"')
					as1[i] = as1[i].substring(1, as1[i].length() - 1);
				gameobject.setValue(as[i], as1[i]);
			}
	}

	/** 复制对象 */
	@Override
	public Object clone() {
		GameData gameData = null;
		try {
			gameData = (GameData) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return gameData;
	}
}