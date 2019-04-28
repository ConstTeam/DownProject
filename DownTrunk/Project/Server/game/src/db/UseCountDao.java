package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.ServerManager;
import util.ErrorPrint;

/**
 * 
 */
public class UseCountDao {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(UseCountDao.class);

	public static void addUseCount(Connection con, int playerId, int type) throws SQLException {
		String insertSql = "INSERT INTO player_use_count(player_id, use_count_type, use_count_value) VALUES (?,?,?) ON DUPLICATE KEY UPDATE use_count_value = use_count_value + ?";
		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement(insertSql);
			pstmt.setInt(1, playerId);
			pstmt.setInt(2, type);
			pstmt.setInt(3, 1);
			pstmt.setInt(4, 1);
			pstmt.execute();
		} finally {
			pstmt.close();
		}
	}
	
	public static void clearUseCount(Connection con, int playerId) throws SQLException {
		String insertSql = "DELETE FROM player_use_count WHERE player_id = ?";
		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement(insertSql);
			pstmt.setInt(1, playerId);
			pstmt.execute();
		} finally {
			pstmt.close();
		}
	}

	public static HashMap<Integer, Integer> getUseCount(Connection con, int playerId) {
		
		String sql = "SELECT * FROM player_use_count WHERE player_id = ?";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		HashMap<Integer, Integer> useCount = new HashMap<>();
		
		try {
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, playerId);
			rs = pstmt.executeQuery();
			while (rs.next()) { 
				int type = rs.getInt("use_count_type");
				int value = rs.getInt("use_count_value");
				useCount.put(type, value);
			}
			return useCount;
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

	public static int getUseCountByType(int playerId, int type) {
		
		String sql = "SELECT * FROM player_use_count WHERE player_id = ? AND use_count_type = ?";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		Connection connect = ServerManager.gameDBConnect.getDBConnect();
		if (connect == null) { // 数据库连接池已满
			return -1;
		}
		try {
			pstmt = connect.prepareStatement(sql);
			pstmt.setInt(1, playerId);
			pstmt.setInt(2, type);
			rs = pstmt.executeQuery();
			if (rs.next()) { 
				int value = rs.getInt("use_count_value");
				return value;
			}
			return 0;
		} catch (Exception e) {
			ErrorPrint.print(e);
		} finally {
			try {
				rs.close();
				pstmt.close();
			} catch (SQLException e) {
				ErrorPrint.print(e);
			}
			ServerManager.gameDBConnect.closeConnect(connect);
		}
		return -1;
	}
}