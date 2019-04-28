package memory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 锁
 *
 */
public class LockMemory {

	private ConcurrentHashMap<Integer, ReentrantLock> locks;
	
	private boolean fair = false;
	
	private LockMemory() {
		locks = new ConcurrentHashMap<>();
	}
	
	private LockMemory(boolean fair) {
		this.fair = fair;
		locks = new ConcurrentHashMap<>();
	}
	
	public static LockMemory getInstance() {
		return new LockMemory();
	}
	
	public static LockMemory getInstance(boolean fair) {
		return new LockMemory(fair);
	}

	public void createLock(int id) {
		ReentrantLock lock = new ReentrantLock(this.fair);
		locks.put(id, lock);
	}
	
	public void removeLock(int id) {
		ReentrantLock remove = locks.remove(id);
		if (remove.isLocked()) {
			remove.unlock();
		}
	}
	
	/**
	 * 获取锁
	 * 
	 * @param id
	 * @return 创建结果
	 */
	public ReentrantLock getLock(int id) {
		ReentrantLock lock = new ReentrantLock(this.fair);
		ReentrantLock putIfAbsent = locks.putIfAbsent(id, lock);
		if (null == putIfAbsent) {
			return lock;
		} else {
			return putIfAbsent;
		}
	}
	
	public void lock(int id) {
		ReentrantLock lock = getLock(id);
		lock.lock();
	}
	
	public void unlock(int id) {
		ReentrantLock lock = getLock(id);
		lock.unlock();
	}
}
