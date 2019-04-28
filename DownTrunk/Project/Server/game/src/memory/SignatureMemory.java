package memory;

import java.util.HashMap;

/**
 * 登录签名内存
 * 
 * 1、普通登录--提供根据用户名创建签名的方法
 * 2、使用LockMemory中每玩家自己的锁进行并发控制，因此签名内存必须是线程安全的操作
 * 
 */
public class SignatureMemory {

	private static SignatureMemory instance;

	/** 签名集合（HashMap<Username, Signature>） */
	private HashMap<String, String> signatures;
	
	private SignatureMemory() {
		signatures = new HashMap<>();
	}
	
	public static SignatureMemory getInstance() {
		if (instance == null) {
			instance = new SignatureMemory();
		}
		
		return instance;
	}
	
	public String getSignature(String accountId) {
		return signatures.get(accountId);
	}
	
	public String removeSignature(String accountId) {
		return signatures.remove(accountId);
	}
	
	/**
	 * 如果是普通登录，使用此方法创建登录签名
	 * @param accountId
	 * @return 登录签名
	 */
	public String createSignature(String accountId) {
		String sign = "" + accountId.hashCode() + System.currentTimeMillis();
		signatures.put(accountId, sign);
		
		return sign;
	}
}
