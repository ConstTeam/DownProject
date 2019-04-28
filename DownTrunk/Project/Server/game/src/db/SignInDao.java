package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import module.quest.SignInQuest;

/**
 * 
 */
public class SignInDao {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(QuestDao.class);

	/**
	 * 初始化签到信息
	 * 
	 * @param con
	 * @param playerId
	 * @throws SQLException
	 */
	public static void initSignInInfo(Connection con, int playerId) throws SQLException {
		String insertSql = "INSERT INTO player_sign_in(player_id, sign_in_date, sign_in_days) VALUES (?,?,?)";
		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement(insertSql);
			pstmt.setInt(1, playerId);
			pstmt.setObject(2, new Date());
			pstmt.setInt(3, 0);
			pstmt.execute();
		} finally {
			pstmt.close();
		}
	}
	
	/**
	 * 获取签到信息
	 * 
	 * @param con
	 * @param playerId
	 * @throws SQLException
	 */
	public static SignInQuest getSignInInfo(Connection con, int playerId) throws SQLException {
		String insertSql = "SELECT * FROM player_sign_in WHERE player_id = ?";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = con.prepareStatement(insertSql);
			pstmt.setInt(1, playerId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				SignInQuest signIn = new SignInQuest();
				Calendar c_signIn = Calendar.getInstance();
				c_signIn.setTime((Date)rs.getObject("sign_in_date"));
				Calendar c_now = Calendar.getInstance();
				int signInDays = rs.getInt("sign_in_days");
				if (signInDays <7 && (signInDays == 0 || c_now.get(Calendar.YEAR) > c_signIn.get(Calendar.YEAR) || 
					c_now.get(Calendar.MONTH) > c_signIn.get(Calendar.MONTH) || 
					c_now.get(Calendar.DATE) > c_signIn.get(Calendar.DATE)) ) {
					signIn.setCanReceive(true);
				} else {
					signIn.setCanReceive(false);
				}
				signIn.setSignInDay(signInDays);
				signIn.setPhoneNumber(rs.getString("bind_phone_number"));
				return signIn;
			}
			return null;
		} finally {
			rs.close();
			pstmt.close();
		}
	}
	
	/**
	 * 更新签到信息
	 * 
	 * @param con
	 * @param playerId
	 * @param sign_in_days
	 * @throws SQLException
	 */
	public static void updateSignInInfo(Connection con, int playerId) throws SQLException {
		String updateSql = "UPDATE player_sign_in SET sign_in_date = ?, sign_in_days = sign_in_days+1 WHERE player_id = ?";
		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement(updateSql);
			pstmt.setObject(1, new Date());
			pstmt.setInt(2, playerId);
			pstmt.execute();
		} finally {
			pstmt.close();
		}
	}
	
	/**
	 * 更新签到绑定手机号
	 * 
	 * @param con
	 * @param playerId
	 * @param sign_in_days
	 * @throws SQLException
	 */
	public static void updateSignInPhoneNumber(Connection con, int playerId, String phoneNumber) throws SQLException {
		String updateSql = "UPDATE player_sign_in SET bind_phone_number = ? WHERE player_id = ?";
		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement(updateSql);
			pstmt.setString(1, phoneNumber);
			pstmt.setInt(2, playerId);
			pstmt.execute();
		} finally {
			pstmt.close();
		}
	}
	
}