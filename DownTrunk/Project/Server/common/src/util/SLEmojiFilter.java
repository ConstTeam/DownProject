package util;

import org.apache.commons.lang.StringUtils;

/**
 * emoji���߹�����
 */
public class SLEmojiFilter {
	/**
	 * ����Ƿ���emoji�ַ�
	 * 
	 * @param source
	 * @return һ�����о��׳�
	 */
	public static boolean containsEmoji(String source) {
		if (StringUtils.isBlank(source)) {
			return false;
		}
		int len = source.length();
		for (int i = 0; i < len; i++) {
			char codePoint = source.charAt(i);
			if (isEmojiCharacter(codePoint)) {
				// do nothing���жϵ������������ȷ���б����ַ�
				return true;
			}
		}
		return false;
	}

	private static boolean isEmojiCharacter(char codePoint) {
		return (codePoint == 0x0) || (codePoint == 0x9) || (codePoint == 0xA) || (codePoint == 0xD)
				|| ((codePoint >= 0x20) && (codePoint <= 0xD7FF)) || ((codePoint >= 0xE000) && (codePoint <= 0xFFFD))
				|| ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF));
	}

	/**
	 * ����emoji ���� �������������͵��ַ�
	 * 
	 * @param source
	 * @return
	 */
	public static String filterEmoji(String source) {
		source = source.replaceAll("[\\ud800\\udc00-\\udbff\\udfff\\ud800-\\udfff]", "*");
		if (!containsEmoji(source)) {
			return source;// �����������ֱ�ӷ���
		}
		// ��������������
		StringBuilder buf = null;

		int len = source.length();

		for (int i = 0; i < len; i++) {
			char codePoint = source.charAt(i);

			if (isEmojiCharacter(codePoint)) {
				if (buf == null) {
					buf = new StringBuilder(source.length());
				}

				buf.append(codePoint);
			} else {
				buf.append("*");
			}
		}

		if (buf == null) {
			return source;// ���û���ҵ� emoji���飬�򷵻�Դ�ַ���
		} else {
			if (buf.length() == len) {// ������������ھ������ٵ�toString����Ϊ�����������ַ���
				buf = null;
				return source;
			} else {
				return buf.toString();
			}
		}

	}

	public static String string2Unicode(String string) {
		StringBuffer unicode = new StringBuffer();
		for (int i = 0; i < string.length(); i++) {
			// ȡ��ÿһ���ַ�
			char c = string.charAt(i);
			// ת��Ϊunicode
			unicode.append("\\u" + Integer.toHexString(c));
		}
		return unicode.toString();
	}

	public static String getUnicode(String source) {
		String returnUniCode = null;
		String uniCodeTemp = null;
		for (int i = 0; i < source.length(); i++) {
			uniCodeTemp = "\\u" + Integer.toHexString((int) source.charAt(i));
			returnUniCode = returnUniCode == null ? uniCodeTemp : returnUniCode + uniCodeTemp;
		}
		return returnUniCode;
	}

	private static final char[] HEX_CHAR = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
			'f' };

	public static String bytesToHexFun1(byte[] bytes) {
		// һ��byteΪ8λ����������ʮ������λ��ʶ
		char[] buf = new char[bytes.length * 2];
		int a = 0;
		int index = 0;
		for (byte b : bytes) { // ʹ�ó���ȡ�����ת��
			if (b < 0) {
				a = 256 + b;
			} else {
				a = b;
			}

			buf[index++] = HEX_CHAR[a / 16];
			buf[index++] = HEX_CHAR[a % 16];
		}

		return new String(buf);
	}
}