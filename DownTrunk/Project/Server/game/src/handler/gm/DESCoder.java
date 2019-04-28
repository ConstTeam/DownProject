package handler.gm;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

public class DESCoder {
	/**
	* ��Կ�㷨
	* */
	 public static final String KEY_ALGORITHM = "DESede";
	 /**
	* ����/�����㷨/����ģʽ/��䷽ʽ
	* */
	 public static final String CIPHER_ALGORITHM = "DESede/ECB/NoPadding";
	 /**
	* ת����Կ
	*
	* @param key
	* ��������Կ
	* @return Key ��Կ
	* */
	 public static Key toKey(byte[] key) throws Exception {
		 // ʵ����Des��Կ
		 DESedeKeySpec dks = new DESedeKeySpec(key);
		 // ʵ������Կ����
		 SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
		 // ������Կ
		 SecretKey secretKey = keyFactory.generateSecret(dks);
		 return secretKey;
	 }
	 /**
	* ��������
	*
	* @param data
	* ����������
	* @param key
	* ��Կ
	* @return byte[] ���ܺ������
	* */
	 public static byte[] encrypt(byte[] data, byte[] key) throws Exception {
		 // ��ԭ��Կ
		 Key k = toKey(key);
		 // ʵ����
		 Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
		 // ��ʼ��������Ϊ����ģʽ
		cipher.init(Cipher.ENCRYPT_MODE, k);
		 // ִ�в���
		 byte[] input = data;
		 if (data.length % 8 != 0) {
		input = new byte[(data.length / 8 + 1) * 8];
		 System.arraycopy(data, 0, input, 0, data.length);
		 }
		 return cipher.doFinal(input);
	 }
	 /**
	* ��������
	*
	* @param data
	* ����������
	* @param key
	* ��Կ
	* @return byte[] ���ܺ������
	* */
	 public static byte[] decrypt(byte[] data, byte[] key) throws Exception {
		 // ��ӭ��Կ
		 Key k = toKey(key);
		 // ʵ���� 
		 Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
		 // ��ʼ��������Ϊ����ģʽ
		 cipher.init(Cipher.DECRYPT_MODE, k);
		 // ִ�в���
		 return cipher.doFinal(data);
	 }
}
