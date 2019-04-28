package handler.gm;

import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

public class DESCoder {
	/**
	* 密钥算法
	* */
	 public static final String KEY_ALGORITHM = "DESede";
	 /**
	* 加密/解密算法/工作模式/填充方式
	* */
	 public static final String CIPHER_ALGORITHM = "DESede/ECB/NoPadding";
	 /**
	* 转换密钥
	*
	* @param key
	* 二进制密钥
	* @return Key 密钥
	* */
	 public static Key toKey(byte[] key) throws Exception {
		 // 实例化Des密钥
		 DESedeKeySpec dks = new DESedeKeySpec(key);
		 // 实例化密钥工厂
		 SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
		 // 生成密钥
		 SecretKey secretKey = keyFactory.generateSecret(dks);
		 return secretKey;
	 }
	 /**
	* 加密数据
	*
	* @param data
	* 待加密数据
	* @param key
	* 密钥
	* @return byte[] 加密后的数据
	* */
	 public static byte[] encrypt(byte[] data, byte[] key) throws Exception {
		 // 还原密钥
		 Key k = toKey(key);
		 // 实例化
		 Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
		 // 初始化，设置为加密模式
		cipher.init(Cipher.ENCRYPT_MODE, k);
		 // 执行操作
		 byte[] input = data;
		 if (data.length % 8 != 0) {
		input = new byte[(data.length / 8 + 1) * 8];
		 System.arraycopy(data, 0, input, 0, data.length);
		 }
		 return cipher.doFinal(input);
	 }
	 /**
	* 解密数据
	*
	* @param data
	* 待解密数据
	* @param key
	* 密钥
	* @return byte[] 解密后的数据
	* */
	 public static byte[] decrypt(byte[] data, byte[] key) throws Exception {
		 // 欢迎密钥
		 Key k = toKey(key);
		 // 实例化 
		 Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
		 // 初始化，设置为解密模式
		 cipher.init(Cipher.DECRYPT_MODE, k);
		 // 执行操作
		 return cipher.doFinal(data);
	 }
}
