package memory;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import db.module.player.Player;

/**
 * �û�����
 * 
 */
public class UserMemory {

	private static UserMemory instance;

	/** ȫ�ֽ�ɫ�б�PlayerId, Player�� */
	private ConcurrentHashMap<Integer, Player> playerMapping;

	private UserMemory() {
		playerMapping = new ConcurrentHashMap<>();
	}

	public static UserMemory getInstance() {
		if (instance == null) {
			instance = new UserMemory();
		}

		return instance;
	}

	/**
	 * ����µ��˺źͽ�ɫ
	 * 
	 * @param account
	 * @param player
	 */
	public Player add(Player player) {
		return this.playerMapping.put(player.getPlayerId(), player);
	}

	/**
	 * ���ݽ�ɫId�Ƴ��˺źͽ�ɫ
	 * 
	 * @param playerId
	 */
	public boolean remove(int playerId, Player player) {
		return playerMapping.remove(playerId, player);
	}

	/**
	 * ���ݽ�ɫId��ȡ��ɫ
	 * 
	 * @param playerId
	 * @return
	 */
	public Player getPlayer(int playerId) {
		return this.playerMapping.get(playerId);
	}

	/**
	 * ��ȡ�������
	 * 
	 * @return
	 */
	public Collection<Player> getOnlinePlayers() {
		return this.playerMapping.values();
	}
	
	public int size() {
		return this.playerMapping.size();
	}
	
}