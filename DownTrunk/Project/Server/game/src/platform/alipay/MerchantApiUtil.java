package platform.alipay;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import util.Tools;

/**
 * @author blueSky
 */
public class MerchantApiUtil {

	/**
	 * ��ȡ����ǩ��
	 * 
	 * @param paramMap
	 *            ǩ������
	 * @param paySecret
	 *            ǩ����Կ
	 * @return
	 */
	public static String getSign(Map<String, Object> paramMap, String paySecret) {
		SortedMap<String, Object> smap = new TreeMap<String, Object>(paramMap);
		StringBuffer stringBuffer = new StringBuffer();
		for (Map.Entry<String, Object> m : smap.entrySet()) {
			Object value = m.getValue();
			if (value != null && !Tools.isEmptyString(String.valueOf(value))) {
				stringBuffer.append(m.getKey()).append("=").append(m.getValue()).append("&");
			}
		}
		stringBuffer.delete(stringBuffer.length() - 1, stringBuffer.length());

		String argPreSign = stringBuffer.append("&paySecret=").append(paySecret).toString();
		String signStr = MD5Util.encode(argPreSign).toUpperCase();

		return signStr;
	}

	/**
	 * ��ȡ����ƴ�Ӵ�
	 * 
	 * @param paramMap
	 * @return
	 */
	public static String getParamStr(Map<String, Object> paramMap) {
		SortedMap<String, Object> smap = new TreeMap<String, Object>(paramMap);
		StringBuffer stringBuffer = new StringBuffer();
		for (Map.Entry<String, Object> m : smap.entrySet()) {
			Object value = m.getValue();
			if (value != null && !Tools.isEmptyString(String.valueOf(value))) {
				stringBuffer.append(m.getKey()).append("=").append(value).append("&");
			}
		}
		stringBuffer.delete(stringBuffer.length() - 1, stringBuffer.length());

		return stringBuffer.toString();
	}

	/**
	 * ��֤�̻�ǩ��
	 * 
	 * @param paramMap
	 *            ǩ������
	 * @param paySecret
	 *            ǩ��˽Կ
	 * @param signStr
	 *            ԭʼǩ������
	 * @return
	 */
	public static boolean isRightSign(Map<String, Object> paramMap, String paySecret, String signStr) {

		if (Tools.isEmptyString(signStr)) {
			return false;
		}

		String sign = getSign(paramMap, paySecret);
		if (signStr.equals(sign)) {
			return true;
		} else {
			return false;
		}
	}
}
