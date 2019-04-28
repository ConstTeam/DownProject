package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.ServerManager;
import app.ServerStaticInfo;
import redis.RedisProxy;
import util.ErrorPrint;
import util.TimeFormat;
import util.Tools;

/**
 * 账号数据访问对象
 *
 */
public class ServerDao {
	
	private static final Logger logger = LoggerFactory.getLogger(ServerDao.class);
	
	public static String init() throws SQLException {
		Connection con = ServerManager.gameDBConnect.getDBConnect();
		if (con == null) {
			logger.error("数据库连接池已满！", new Throwable("ConnectionPool is Full!"));
			return null;
		}
		try {
			String serverTime = getServerTime(con, ServerStaticInfo.getServerType());
			if (Tools.isEmptyString(serverTime)) {
				RedisProxy.getInstance().clearDatabase();
			}
			return insertServerList(con, ServerStaticInfo.getServerType());
		} finally {
			ServerManager.gameDBConnect.closeConnect(con);
		}
	}

	public static void insertServerInfo(Connection con) throws SQLException {
		String insertSql = "INSERT ignore INTO server_info(id, server_id) (SELECT max(id)+1, ? FROM server_info)";
		PreparedStatement pstmt = null;
		try {	
			pstmt = con.prepareStatement(insertSql);
			pstmt.setString(1, ServerStaticInfo.getServerId());
			pstmt.execute();
		} finally {
			pstmt.close();
		}
	}
	
	public static String insertServerList(Connection con, String type) throws SQLException {
		String insertSql = "INSERT ignore INTO server_list(server_id, type, create_time) VALUES (?,?,?)";
		PreparedStatement pstmt = null;
		try {	
			String date = TimeFormat.getTime();
			pstmt = con.prepareStatement(insertSql);
			pstmt.setString(1, ServerStaticInfo.getServerId());
			pstmt.setString(2, type);
			pstmt.setString(3, date);
			pstmt.execute();
			return getServerTime(con, type);
		} finally {
			pstmt.close();
		}
	}

	private static String getServerTime(Connection con, String type) throws SQLException {
		
		String sql = "SELECT create_time FROM server_list WHERE server_id = ? AND type = ?";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, ServerStaticInfo.getServerId());
			pstmt.setString(2, type);
			rs = pstmt.executeQuery();
			if (rs.next()) { // 仅查询一条
				return TimeFormat.formatTimeByDate((Date) rs.getObject("create_time"));
			}
		} finally {
			pstmt.close();
			rs.close();
		}
		return "";
	}
	
	private static int getPlayerCount(Connection con) throws SQLException {
		
		String sql = "SELECT count(player_id) player_count FROM player_info";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if (rs.next()) { // 仅查询一条
				return rs.getInt("player_count");
			}
			return 0;
		} finally {
			pstmt.close();
			rs.close();
		}
	}
	
	@SuppressWarnings("unused")
	private static int getMaxPlayerId(Connection con) throws SQLException {
		
		String sql = "SELECT max(player_id) max_id FROM player_info";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if (rs.next()) { // 仅查询一条
				return rs.getInt("max_id");
			}
			return 0;
		} finally {
			pstmt.close();
			rs.close();
		}
	}
	
	public static void setAutoincrement(Connection con, int player_id) throws SQLException {
		
		String sql = "alter table player_info AUTO_INCREMENT = ?";
		PreparedStatement pstmt = null;
		
		try {
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, player_id);
			pstmt.execute();
		} finally {
			pstmt.close();
		}
	}

	public static boolean isCanCreateAccount(Connection con) {
		String sql = "SELECT max_player_id FROM player_id_limit";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			int playerCount = getPlayerCount(con);
			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();
			if (rs.next()) { // 仅查询一条
				boolean result = playerCount < rs.getInt("max_player_id");
				if (!result) {
					logger.info("创建角色数量已达到上限！当前玩家数量：" + playerCount + "，玩家上限数量：" + (rs.getInt("max_player_id")));
				}
				return result;
			}
			return true;
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
		return true;
	}
	
	public static boolean monitor(Connection con) {
		String sql = "SELECT player_id FROM player_info limit 1";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			pstmt = con.prepareStatement(sql);
			rs = pstmt.executeQuery();
			return true;
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
		return false;
	}
}
