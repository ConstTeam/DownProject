package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.ServerManager;
import config.ConfigData;
import db.module.player.Player;
import memory.UserMemory;
import module.ClearConst;
import redis.RedisProxy;
import redis.data.PlayerInfo;
import util.ErrorPrint;

/**
 * 角色数据访问对象
 * 
 */
public class PlayerDao {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(PlayerDao.class);
	
	public static Player getPlayerInfo(int playerId) {
		
		String selectSql = "SELECT * FROM player_info WHERE player_id = ?";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = ServerManager.gameDBConnect.getDBConnect();
		if (con == null) { // 数据库连接池已满
			return null;
		}
		try {
			// 查询角色信息
			pstmt = con.prepareStatement(selectSql);
			pstmt.setInt(1, playerId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				return getPlayerValue(rs, con);
			}
		} catch (Exception e) {
			ErrorPrint.print(e);
		} finally {
			try {
				rs.close();
				pstmt.close();
				ServerManager.gameDBConnect.closeConnect(con);
			} catch (SQLException e) {
				ErrorPrint.print(e);
			}
		}
		return null;
	}
	
	/**
	 * 从DB中加载角色信息
	 * 
	 * @return 角色信息
	 */
	public static Player getPlayerInfo(Connection con, int playerId) {

		String selectSql = "SELECT * FROM player_info WHERE player_id = ?";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			// 查询角色信息
			pstmt = con.prepareStatement(selectSql);
			pstmt.setInt(1, playerId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				return getPlayerValue(rs, con);
			}
		} catch (Exception e) {
			ErrorPrint.print(e);
		} finally {
			try {
				rs.close();
				pstmt.close();
			} catch (SQLException e) {
				ErrorPrint.print(e);
			}
		}
		return null;
	}

	public static Player updatePlayerInfo(Connection con, Player player) {

		String selectSql = "SELECT * FROM player_info WHERE player_id = ?";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			// 查询角色信息
			pstmt = con.prepareStatement(selectSql);
			pstmt.setInt(1, player.getPlayerId());
			rs = pstmt.executeQuery();
			if (rs.next()) {
				return getPlayerValue(rs, con, player);
			}
		} catch (Exception e) {
			ErrorPrint.print(e);
		} finally {
			try {
				rs.close();
				pstmt.close();
			} catch (SQLException e) {
				ErrorPrint.print(e);
			}
		}
		return player;
	}
	
	public static boolean updatePlayerInfo(Connection con, PlayerInfo playerInfo) {
		String insertSql = "UPDATE player_info SET last_login_time = ?, icon = ?, nickname = ? WHERE player_id = ?";
		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement(insertSql);
			pstmt.setString(1, playerInfo.getLastLoginTime());
			pstmt.setString(2, playerInfo.getIcon());
			pstmt.setString(3, playerInfo.getNickname());
			pstmt.setInt(4, playerInfo.getPlayerId());
			pstmt.execute();
			
			return true;
		} catch (Exception e) {
			ErrorPrint.print(e);
		} finally {
			try {
				pstmt.close();
			} catch (SQLException e) {
				ErrorPrint.print(e);
			}
		}
		return false;
	}

	private static Player getPlayerValue(ResultSet rs, Connection con) throws SQLException {
		return getPlayerValue(rs, con, new Player());
	}

	private static Player getPlayerValue(ResultSet rs, Connection con, Player player) throws SQLException {
		player.setPlayerId(rs.getInt("player_id"));
		player.setNickname(rs.getString("nickname"));
		player.setIcon(rs.getString("icon"));
		player.setAntiAddiState(rs.getInt("anti_addi_state"));
		player.setAntiAddi(rs.getBoolean("is_anti_addi"));
		player.setDiamond(rs.getInt("diamond"));
		player.setGold(rs.getInt("gold"));
		return player;
	}

	/**
	 * 创建帐号 创建角色
	 * 
	 * @param con
	 * @param accountId
	 * @param nickName
	 */
	public static boolean createPlayer(Connection con, int playerId, String nickname, String icon, String platformId,
			String platform, String channel) {

		String insertSql = "INSERT INTO player_info (player_id, nickname, icon, diamond, gold, last_login_time) VALUES (?,?,?,?,?,?)";
		PreparedStatement pstmt = null;
		try {

			int diamond = 0;
			int gold = 0;

			con.setAutoCommit(false); // 设置不自动提交

			AccountDao.createAccount(con, playerId, platformId, platform, channel); // 创建帐号

			// 插入角色
			pstmt = con.prepareStatement(insertSql);
			pstmt.setInt(1, playerId);
			pstmt.setString(2, nickname);
			pstmt.setString(3, icon);
			pstmt.setInt(4, diamond);
			pstmt.setInt(5, gold);
			pstmt.setObject(6, new Date()); // 最后登录时间
			pstmt.execute();
			
			Player player = getPlayerInfo(con, playerId);
			if (player == null) {
				con.rollback();
				return false;
			}

			// 任务初始化
			ClearDao.SaveClearTime(con, playerId, ClearConst.DAILY);
			addRole(con, playerId, 0);
			addScene(con, playerId, 0);
			
			con.commit();
			return true;
		} catch (SQLException e) {
			try {
				con.rollback();
			} catch (SQLException e1) {
				ErrorPrint.print(e1);
			}
			ErrorPrint.print(e);
		} finally {
			try {
				con.setAutoCommit(true); // 设置自动提交
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (SQLException e) {
				ErrorPrint.print(e);
			}
		}
		return false;
	}

	/**
	 * 判断玩家货币是否足够
	 * 
	 * @param con
	 * @param playerId
	 *            玩家Id
	 * @param type
	 *            货币类型
	 * @param money
	 *            数量
	 * @return
	 */
	public static boolean moneyIsEnough(Connection con, int playerId, String type, int money) {

		String selectMoneySql = "SELECT * FROM player_info WHERE player_id = ? AND " + type + " >= ?";
		PreparedStatement pstmt = null;
		ResultSet rs = null;

		try {
			pstmt = con.prepareStatement(selectMoneySql);
			pstmt.setInt(1, playerId);
			pstmt.setInt(2, money);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				return true;
			}
		} catch (Exception e) {
			ErrorPrint.print(e);
		} finally {
			try {
				pstmt.close();
			} catch (SQLException e) {
				ErrorPrint.print(e);
			}
		}
		return false;
	}
	
	public static int getPlayerGold(Connection con, int playerId) throws SQLException {

		String selectSql = "SELECT * FROM player_info WHERE player_id = ?";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			// 查询角色信息
			pstmt = con.prepareStatement(selectSql);
			pstmt.setInt(1, playerId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				return rs.getInt("gold");
			}
			return -1;
		} finally {
			if (rs != null) {
				rs.close();
			}
			pstmt.close();
		}
	}
	
	@SuppressWarnings("unused")
	private static int setMoney(Connection con, int playerId, String moneyType, int money) throws SQLException {

		String updateMoneySql = "UPDATE player_info SET " + moneyType + " =  ? WHERE player_id = ?";
		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement(updateMoneySql);
			pstmt.setInt(1, money);
			pstmt.setInt(2, playerId);
			return pstmt.executeUpdate();
		} finally {
			pstmt.close();
		}
	}

	/**
	 * 增加钻石
	 * 
	 * @param con
	 * @param playerId
	 * @param diamond
	 * @throws SQLException
	 */
	public static void addDiamond(Connection con, int playerId, int diamond) throws SQLException {
		String update = "UPDATE player_info SET diamond = diamond + ? WHERE player_id = ?";
		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement(update);
			pstmt.setInt(1, diamond);
			pstmt.setInt(2, playerId);
			pstmt.execute();
		} finally {
			pstmt.close();
		}
	}
	
	/**
	 * 增加金币
	 * 
	 * @param con
	 * @param playerId
	 * @param gold
	 */
	public static int addGold(Connection con, int playerId, int gold) throws SQLException {
		String update = "UPDATE player_info SET gold = gold + ? WHERE player_id = ?";
		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement(update);
			pstmt.setInt(1, gold);
			pstmt.setInt(2, playerId);
			pstmt.execute();
			int playerGold = getPlayerGold(con, playerId);
			
			RedisProxy.getInstance().updatePlayerGoldRanking(playerId, gold);
			Player player = UserMemory.getInstance().getPlayer(playerId);
			if (player != null) {
				player.setGold(playerGold);
			}
			return playerGold;
		} finally {
			pstmt.close();
		}
	}
	
	public static boolean addDiamond(int playerId, int diamond) {
		Connection con = ServerManager.gameDBConnect.getDBConnect();
		if (con == null) { // 数据库连接池已满
			return false;
		}
		try {
			if (!moneyIsEnough(con, playerId, DBModuleConst.DIAMOND_STR, diamond)) {
				return false;
			}
			addDiamond(con, playerId, diamond);
			return true;
		} catch (Exception e) {
			ErrorPrint.print(e);
		} finally {
			ServerManager.gameDBConnect.closeConnect(con);
		}
		return false;
	}
	
	public static void addScene(Connection con, int playerId, int sceneId) throws SQLException {
		String update = "INSERT INTO player_scene (player_id, scene) VALUES(?,?)";
		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement(update);
			pstmt.setInt(1, playerId);
			pstmt.setInt(2, sceneId);
			pstmt.execute();
		} finally {
			pstmt.close();
		}
	}
	
	public static int addScene(int playerId, int sceneId, int gold) {
		Connection con = ServerManager.gameDBConnect.getDBConnect();
		if (con == null) { // 数据库连接池已满
			return -1;
		}
		try {
			con.setAutoCommit(false);
			addGold(con, playerId, -gold);
			addScene(con, playerId, sceneId);
			return PlayerDao.getScene(con, playerId);
		} catch (Exception e) {
			try {
				con.rollback();
			} catch (SQLException e1) {
				ErrorPrint.print(e1);
			}
			ErrorPrint.print(e);
		} finally {
			try {
				con.setAutoCommit(true);
			} catch (SQLException e) {
				ErrorPrint.print(e);
			}
			ServerManager.gameDBConnect.closeConnect(con);
		}
		return -1;
	}
	
	public static int getScene(int playerId) {
		Connection con = ServerManager.gameDBConnect.getDBConnect();
		if (con == null) { // 数据库连接池已满
			return -1;
		}
		try {
			return getScene(con, playerId);
		} finally {
			ServerManager.gameDBConnect.closeConnect(con);
		}
	}
	
	public static int getScene(Connection con, int playerId) {

		String selectSql = "SELECT * FROM player_scene WHERE player_id = ?";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		HashMap<Integer, Boolean> roles = new HashMap<>();
		try {
			// 查询角色信息
			pstmt = con.prepareStatement(selectSql);
			pstmt.setInt(1, playerId);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				roles.put(rs.getInt("scene"), true);
			}
			int ids = 0;
			for (int id : ConfigData.sceneModels.keySet()) {
				if (roles.get(id) != null) {
					ids = ids + (1 << id);
				}
			}
			return ids;
		} catch (Exception e) {
			ErrorPrint.print(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				pstmt.close();
			} catch (Exception e) {
				ErrorPrint.print(e);
			}
		}
		return -1;
	}
	
	public static void addRole(Connection con, int playerId, int role) throws SQLException {
		String update = "INSERT INTO player_role (player_id, role) VALUES(?,?)";
		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement(update);
			pstmt.setInt(1, playerId);
			pstmt.setInt(2, role);
			pstmt.execute();
		} finally {
			pstmt.close();
		}
	}
	
	public static int addRole(int playerId, int role, int gold) {
		Connection con = ServerManager.gameDBConnect.getDBConnect();
		if (con == null) { // 数据库连接池已满
			return -1;
		}
		try {
			con.setAutoCommit(false);
			addGold(con, playerId, -gold);
			addRole(con, playerId, role);
			return PlayerDao.getRole(con, playerId);
		} catch (Exception e) {
			try {
				con.rollback();
			} catch (SQLException e1) {
				ErrorPrint.print(e1);
			}
			ErrorPrint.print(e);
		} finally {
			try {
				con.setAutoCommit(true);
			} catch (SQLException e) {
				ErrorPrint.print(e);
			}
			ServerManager.gameDBConnect.closeConnect(con);
		}
		return -1;
	}
	
	public static int getRole(int playerId) {
		Connection con = ServerManager.gameDBConnect.getDBConnect();
		if (con == null) { // 数据库连接池已满
			return -1;
		}
		try {
			return getRole(con, playerId);
		} finally {
			ServerManager.gameDBConnect.closeConnect(con);
		}
	}
	
	public static int getRole(Connection con, int playerId) {

		String selectSql = "SELECT * FROM player_role WHERE player_id = ?";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		HashMap<Integer, Boolean> roles = new HashMap<>();
		try {
			// 查询角色信息
			pstmt = con.prepareStatement(selectSql);
			pstmt.setInt(1, playerId);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				roles.put(rs.getInt("role"), true);
			}
			int ids = 0;
			for (int id : ConfigData.heroModels.keySet()) {
				if (roles.get(id) != null) {
					ids = ids + (1 << id);
				}
			}
			return ids;
		} catch (Exception e) {
			ErrorPrint.print(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				pstmt.close();
			} catch (Exception e) {
				ErrorPrint.print(e);
			}
		}
		return -1;
	}
	
	public static void insertOpenPanel(Connection con, String itemName) throws SQLException {
		String insertSql = "INSERT INTO display_items (items, open) VALUES(?, 1) ON DUPLICATE KEY UPDATE open = 1";
		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement(insertSql);
			pstmt.setString(1, itemName);
			pstmt.execute();
		} finally {
			if (pstmt != null) {
				pstmt.close();
			}
		}
	}

	/**
	 * 判断功能是否开启
	 * 
	 * @param con
	 * @param exchangeItemName
	 * @return
	 */
	public static boolean isOpenPanel(Connection con, String itemName) {
		String selectSql = "SELECT open FROM display_items WHERE items = ?";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = con.prepareStatement(selectSql);
			pstmt.setString(1, itemName);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				if (rs.getInt("open") == 1) {
					return true;
				}
			}
		} catch (SQLException e) {
			ErrorPrint.print(e);
		} finally {
			try {
				pstmt.close();
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException e1) {
				ErrorPrint.print(e1);
			}
		}
		return false;
	}

	public static HashMap<String, Boolean> isOpenPanel(Connection con) {
		String selectSql = "SELECT * FROM display_items";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		HashMap<String, Boolean> isOpen = new HashMap<>();
		try {
			pstmt = con.prepareStatement(selectSql);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				isOpen.put(rs.getString("items"), rs.getBoolean("open"));
			}
			return isOpen;
		} catch (SQLException e) {
			ErrorPrint.print(e);
		} finally {
			try {
				pstmt.close();
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException e1) {
				ErrorPrint.print(e1);
			}
		}
		return isOpen;
	}
}
