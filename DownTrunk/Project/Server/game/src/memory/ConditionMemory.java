package memory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Condition;

/**
 * �߳�����Condition�ڴ�
 * 
 */
public class ConditionMemory {
	
	private static ConditionMemory instance;

	/** Condition���� */
	private ConcurrentHashMap<String, Condition> conditions;
	
	private ConditionMemory() {
		conditions = new ConcurrentHashMap<>();
	}
	
	public static ConditionMemory getInstance() {
		if (instance == null) {
			instance = new ConditionMemory();
		}
		
		return instance;
	}
	
	public void addCondition(String accountId, Condition condition) {
		conditions.put(accountId, condition);
	}
	public Condition removeCondition(String accountId) {
		return conditions.remove(accountId);
	}
	public Condition getCondition(String accountId) {
		return conditions.get(accountId);
	}
}