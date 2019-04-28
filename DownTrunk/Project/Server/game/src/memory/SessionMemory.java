package memory;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import net.ISession;

/**
 * �û�����
 * 
 */
public class SessionMemory {

	private static SessionMemory instance;

	/** ȫ�ֽ�ɫsession�б�PlayerId, Session�� */
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
	 * ����µ�Session
	 */
	public ISession add(int playerId, ISession session) {
		return this.sessions.put(playerId, session);
	}

	/**
	 * ���ݽ�ɫId�Ƴ�
	 */
	public boolean removeByPlayerId(int playerId, ISession session) {
		return sessions.remove(playerId, session);
	}

	/**
	 * ���ݽ�ɫId��ȡSession
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