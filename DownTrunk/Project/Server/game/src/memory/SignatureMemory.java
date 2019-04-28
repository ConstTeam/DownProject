package memory;

import java.util.HashMap;

/**
 * ��¼ǩ���ڴ�
 * 
 * 1����ͨ��¼--�ṩ�����û�������ǩ���ķ���
 * 2��ʹ��LockMemory��ÿ����Լ��������в������ƣ����ǩ���ڴ�������̰߳�ȫ�Ĳ���
 * 
 */
public class SignatureMemory {

	private static SignatureMemory instance;

	/** ǩ�����ϣ�HashMap<Username, Signature>�� */
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
	 * �������ͨ��¼��ʹ�ô˷���������¼ǩ��
	 * @param accountId
	 * @return ��¼ǩ��
	 */
	public String createSignature(String accountId) {
		String sign = "" + accountId.hashCode() + System.currentTimeMillis();
		signatures.put(accountId, sign);
		
		return sign;
	}
}
