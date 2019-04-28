package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import module.ClearConst;
import module.quest.Quest;
import quest.QuestManager;
import util.ErrorPrint;
import util.TimeFormat;

/**
 * 
 */
public class ClearDao {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(ClearDao.class);

	public static void SaveClearTime(Connection con, int playerId, String type) throws SQLException {
		String insertSql = "REPLACE INTO player_clear_time(player_id, clear_time, clear_type) VALUES (?,?,?)";
		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement(insertSql);
			pstmt.setInt(1, playerId);
			pstmt.setObject(2, new Date());
			pstmt.setString(3, type);
			pstmt.execute();
		} finally {
			pstmt.close();
		}
	}

	public static Calendar getClearTime(Connection con, int playerId, String type) {
		String sql = "SELECT clear_time FROM player_clear_time WHERE clear_type = ? AND player_id = ?";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Calendar calendar = null;
		try {
			pstmt = con.prepareStatement(sql);
			pstmt.setString(1, type);
			pstmt.setInt(2, playerId);
			rs = pstmt.executeQuery();
			if (rs.next()) {
				calendar = Calendar.getInstance();
				calendar.setTime((Date) rs.getObject("clear_time"));
				return calendar;
			}
			SaveClearTime(con, playerId, type);
		} catch (Exception e) {
			ErrorPrint.print(e);
		} finally {
			try {
				pstmt.close();
				rs.close();
			} catch (SQLException e) {
				ErrorPrint.print(e);
			}
		}
		return calendar;
	}

	public static void dailyClearByPlayerId(Connection connect, int playerId) {
		
		try {
			connect.setAutoCommit(false);
			Calendar now = Calendar.getInstance();
			Calendar clearTime = ClearDao.getClearTime(connect, playerId, ClearConst.DAILY);
			if (clearTime == null) {
				return;
			}
			Calendar nextClearTime = TimeFormat.nextClearTime(clearTime);
			if (now.getTimeInMillis() < nextClearTime.getTimeInMillis()) {
				return;
			}
			
			HashMap<Integer, Integer> questIds = new HashMap<>();
			ArrayList<Quest> questInfo = QuestDao.getQuestInfo(playerId, questIds);
			HashMap<Integer, Integer> flushQuest = QuestManager.getFlushQuest(questInfo, questIds);
			QuestDao.updateQuestInfo(connect, playerId, flushQuest);
			ClearDao.SaveClearTime(connect, playerId, ClearConst.DAILY);
			UseCountDao.clearUseCount(connect, playerId);
			QuestDao.updateQuestFlushCount(connect, playerId);
			
		} catch (Exception e) {
			try {
				connect.rollback();
			} catch (SQLException e2) {
				ErrorPrint.print(e2);
			}
			ErrorPrint.print(e);
		} finally {
			try {
				connect.setAutoCommit(true);
			} catch (SQLException e2) {
				ErrorPrint.print(e2);
			}
		}
		return;
	}
}