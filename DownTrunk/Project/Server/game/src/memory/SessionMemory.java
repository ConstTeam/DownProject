package memory;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import net.ISession;

/**
 * 用户集合
 * 
 */
public class SessionMemory {

	private static SessionMemory instance;

	/** 全局角色session列表（PlayerId, Session） */
	private ConcurrentHashMap<Integer, ISession> sessions;

	private SessionMemory() {
		sessions = new ConcurrentHashMap<>();
	}

	public static SessionMemory getInstance() {
		if (instance == null) {
			instance = new SessionMemory();
		}

		return instance;
	}

	/**
	 * 添加新的Session
	 */
	public ISession add(int playerId, ISession session) {
		return this.sessions.put(playerId, session);
	}

	/**
	 * 根据角色Id移除
	 */
	public boolean removeByPlayerId(int playerId, ISession session) {
		return sessions.remove(playerId, session);
	}

	/**
	 * 根据角色Id获取Session
	 */
	public ISession getSession(int playerId) {
		return this.sessions.get(playerId);
	}
	
	public int size() {
		return this.sessions.size();
	}

	public Collection<ISession> getSessions() {
		return sessions.values();
	}
}