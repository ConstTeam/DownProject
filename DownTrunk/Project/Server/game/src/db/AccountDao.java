package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

import app.ServerStaticInfo;
import module.Account;
import util.ErrorPrint;

/**
 * 账号数据访问对象
 *
 */
public class AccountDao {
	
	public static Account getAccount(Connection con, String platformId, String platform, String channel) {
		String sql = "SELECT * FROM player_account_info WHERE platform_id = ? AND platform = ? AND channel = ?";
		Account account = new Account();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, platformId);
			pstmt.setString(2, platform);
			pstmt.setString(3, channel);
			rs = pstmt.executeQuery();
			if (rs.next()) { // 仅查询一条
				account.playerId = rs.getInt("player_id");
				account.platformId = rs.getString("platform_id");
			}
			return account;
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
	
	/**
	 * 创建账号
	 * 
	 * @param con
	 * @param platformId 渠道帐号
	 * @param platform 渠道
	 * @param channel 平台
	 * @throws SQLException 
	 */
	public static void createAccount(Connection con, int playerId, String platformId, String platform, String channel) throws SQLException {
		
		String insertAccountSql = "INSERT INTO player_account_info (player_id,platform_id,platform,channel,create_time) VALUES(?,?,?,?,?)";
		PreparedStatement pstmt = null;
		
		try {
			// 插入账号
			pstmt = con.prepareStatement(insertAccountSql);
			pstmt.setObject(1, playerId);
			pstmt.setObject(2, platformId);
			pstmt.setObject(3, platform);
			pstmt.setObject(4, channel);
			pstmt.setObject(5, new Date());
			pstmt.execute();

		} finally {
			pstmt.close();
		}
	}
	
	public static Account getAccountByPlayerId(Connection con, int playerId) {
		String sql = "SELECT * FROM player_account_info WHERE player_id = ?";
		Account account = new Account();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, playerId);
			rs = pstmt.executeQuery();
			if (rs.next()) { // 仅查询一条
				account.playerId = rs.getInt("player_id");
				account.platformId = rs.getString("platform_id");
			}
			return account;
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
	
	public static String getLid() {
		return ServerStaticInfo.getServerId() + "_" + UUID.randomUUID().toString();
	}
}
