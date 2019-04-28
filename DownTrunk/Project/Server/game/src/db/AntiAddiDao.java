package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.ServerManager;
import db.module.player.Player;
import module.AntiAddiInfo;
import util.ErrorPrint;

/**
 * 防沉迷
 * 
 */
public class AntiAddiDao {

	private static final Logger logger = LoggerFactory.getLogger(AntiAddiDao.class);
	
	public static boolean addAntiAddiInfo(Connection con, int playerId) {

		String insert = "INSERT INTO anti_addi_info (player_id, online_time, login_time, logout_time) VALUES(?,?,?,?)";
		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement(insert);
			pstmt.setInt(1, playerId);
			pstmt.setInt(2, 0);
			pstmt.setObject(3, new Date());
			pstmt.setObject(4, new Date());
			pstmt.execute();
			return true;
		} catch (SQLException e) {
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
	
	public static AntiAddiInfo getAntiAddiInfo(Connection con, int playerId) {
		String sql = "SELECT * FROM anti_addi_info WHERE player_id = ?";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		
		try {
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, playerId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				return getResult(con, rs);
			}
		} catch (SQLException e) {
			ErrorPrint.print(e);
		} finally {
			try {
				pstmt.close();
				rs.close();
			} catch (SQLException e) {
				ErrorPrint.print(e);
			}
		}
		return null;
	}
	
	private static void updateLoginTime(Connection con, int playerId, AntiAddiInfo antiAddiInfo) throws SQLException {
		
		String sql = "UPDATE anti_addi_info SET online_time = ?, login_time = ?, logout_time = ? WHERE player_id = ?";
		PreparedStatement pstmt = null;

		try {
			pstmt = con.prepareStatement(sql);
			pstmt.setObject(1, antiAddiInfo.getOnLineTime());
			pstmt.setObject(2, antiAddiInfo.getLoginTime().getTime());
			pstmt.setObject(3, antiAddiInfo.getLogoutTime().getTime());
			pstmt.setInt(4, playerId);
			pstmt.execute();
		} finally {
			pstmt.close();
		}
	}

	private static int setAntiAddiState(Connection con, int playerId, int state) throws SQLException {
		String sql = "UPDATE player_static_info SET anti_addi_state = ? WHERE player_id = ?";
		PreparedStatement pstmt = null;

		try {
			pstmt = con.prepareStatement(sql);
			pstmt.setInt(1, state);
			pstmt.setInt(2, playerId);
			return pstmt.executeUpdate();
		} finally {
			pstmt.close();
		}
	}
	
	public static boolean loginOnlineTime(Connection con, Player player, AntiAddiInfo antiAddiInfo) {
		try {
			con.setAutoCommit(false);
			boolean result = false;
			Calendar now = Calendar.getInstance();
			int playerId = player.getPlayerId();
			int state = player.getAntiAddiState();
			// XXX 从Dao里移走
			if (state != AntiAddiInfo.ANTI_TIME_ONE || antiAddiInfo.getOnLineTime() != 0) {
				Calendar logoutTime = antiAddiInfo.getLogoutTime();//获取下线时间
				long offLineTime = now.getTimeInMillis() - logoutTime.getTimeInMillis();
				if (offLineTime >= AntiAddiInfo.ANTI_TIME_OFFLINE_TIME) {//下线时间超过5个小时 状态清除
					state = AntiAddiInfo.ANTI_TIME_ONE;
					antiAddiInfo.setOnLineTime((long) 0);
					result = true;
				}
			}

			antiAddiInfo.setLoginTime(now);//设置登录时间
			updateLoginTime(con, playerId, antiAddiInfo);
			if (result) {
				player.setAntiAddiState(state);
				setAntiAddiState(con, playerId, state);
			}
			
			con.commit();
			return result;
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
		}
		return false;
	}
	
	public static boolean onlineTime(Player player, AntiAddiInfo antiAddiInfo, boolean isLogout) {
		Connection con = ServerManager.gameDBConnect.getDBConnect();
		if (con == null) { // 数据库连接池已满
			logger.error("数据库连接池已满！");
			return false;
		}
		try {
			con.setAutoCommit(false);
			boolean result = false;
			// XXX 从Dao里移走
			Calendar now = Calendar.getInstance();
			int playerId = player.getPlayerId();
			int state = player.getAntiAddiState();
			Calendar loginTime = antiAddiInfo.getLoginTime();//获取登录时间
			long onLineTime = now.getTimeInMillis() - loginTime.getTimeInMillis() + antiAddiInfo.getOnLineTime();
			if (state != AntiAddiInfo.ANTI_TIME_THREE) {
				if (onLineTime >= AntiAddiInfo.ANTI_TIME_ONLINE_THREE) {
					state = AntiAddiInfo.ANTI_TIME_THREE;
					result = true;
				} else if (state != AntiAddiInfo.ANTI_TIME_TWO && onLineTime >= AntiAddiInfo.ANTI_TIME_ONLINE_TWO) {
					state = AntiAddiInfo.ANTI_TIME_TWO;
					result = true;
				}	
			}

			antiAddiInfo.setOnLineTime(onLineTime);
			if (isLogout) {
				antiAddiInfo.setLogoutTime(now);
			} else {
				antiAddiInfo.setLoginTime(now);
			}
			
			updateLoginTime(con, playerId, antiAddiInfo);
			if (result) {
				player.setAntiAddiState(state);
				setAntiAddiState(con, playerId, state);
			}
			
			con.commit();
			return result;
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
		return false;
	}
	
	private static AntiAddiInfo getResult(Connection con, ResultSet rs) throws SQLException {
		AntiAddiInfo antiAddiInfo = new AntiAddiInfo();
		antiAddiInfo.setPlayerId(rs.getInt("player_id"));
		antiAddiInfo.setOnLineTime(rs.getBigDecimal("online_time").longValue());
		antiAddiInfo.getLoginTime().setTime((Date)rs.getObject("login_time"));
		antiAddiInfo.getLogoutTime().setTime((Date)rs.getObject("logout_time"));
		return antiAddiInfo;
	}

}
