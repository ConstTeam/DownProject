package memory;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import db.module.player.Player;

/**
 * 用户集合
 * 
 */
public class UserMemory {

	private static UserMemory instance;

	/** 全局角色列表（PlayerId, Player） */
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
	 * 添加新的账号和角色
	 * 
	 * @param account
	 * @param player
	 */
	public Player add(Player player) {
		return this.playerMapping.put(player.getPlayerId(), player);
	}

	/**
	 * 根据角色Id移除账号和角色
	 * 
	 * @param playerId
	 */
	public boolean remove(int playerId, Player player) {
		return playerMapping.remove(playerId, player);
	}

	/**
	 * 根据角色Id获取角色
	 * 
	 * @param playerId
	 * @return
	 */
	public Player getPlayer(int playerId) {
		return this.playerMapping.get(playerId);
	}

	/**
	 * 获取在线玩家
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