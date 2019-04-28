package db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.ServerManager;
import config.ConfigData;
import config.model.quest.QuestModel;
import module.UseCountConst;
import module.quest.Quest;
import util.ErrorPrint;

/**
 * 
 */
public class QuestDao {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(QuestDao.class);

	/**
	 * 初始化任务
	 * 
	 * @param con
	 * @param playerId
	 * @param questIds
	 * @throws SQLException
	 */
	public static void initQuestInfo(Connection con, int playerId, HashMap<Integer, Integer> questIds) throws SQLException {
		String insertSql = "INSERT INTO quest_info(player_id, quest_index, quest_id, quest_value, quest_state, update_time) VALUES (?,?,?,?,?,?)";
		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement(insertSql);
			for (int i = 1; i <= 4; i++) {
				pstmt.setInt(1, playerId);
				pstmt.setInt(2, i);
				pstmt.setInt(3, questIds.get(i));
				pstmt.setInt(4, 0);
				pstmt.setInt(5, 0);
				pstmt.setObject(6, new Date());
				pstmt.addBatch();
			}
			pstmt.executeBatch();
		} finally {
			pstmt.close();
		}
	}
	
	/**
	 * 更新任务信息
	 * 刷新多条任务
	 * 
	 * @param con
	 * @param playerId
	 * @param questIds
	 * @throws SQLException
	 */
	public static void updateQuestInfo(Connection con, int playerId, HashMap<Integer, Integer> questIds) throws SQLException {
		String insertSql = "REPLACE INTO quest_info(player_id, quest_index, quest_id, quest_value, quest_state, update_time) VALUES (?,?,?,?,?,?)";
		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement(insertSql);
			Iterator<Entry<Integer, Integer>> iterator = questIds.entrySet().iterator();
			while (iterator.hasNext()) {
				Entry<Integer, Integer> next = iterator.next();
				Integer index = next.getKey();
				pstmt.setInt(1, playerId);
				pstmt.setInt(2, index);
				pstmt.setInt(3, questIds.get(index));
				pstmt.setInt(4, 0);
				pstmt.setInt(5, 0);
				pstmt.setObject(6, new Date());
				pstmt.addBatch();
			}
			pstmt.executeBatch();
		} finally {
			pstmt.close();
		}
	}
	
	/**
	 * 更新任务信息
	 * 刷新单条任务
	 * 
	 * @param con
	 * @param playerId
	 * @param quest
	 * @throws SQLException
	 */
	public static void updateQuestInfo(Connection con, int playerId, Quest quest) throws SQLException {
		String insertSql = "REPLACE INTO quest_info(player_id, quest_index, quest_id, quest_value, quest_state, update_time) VALUES (?,?,?,?,?,?)";
		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement(insertSql);
			pstmt.setInt(1, playerId);
			pstmt.setInt(2, quest.getIndex());
			pstmt.setInt(3, quest.getQuestId());
			pstmt.setInt(4, quest.getValue());
			pstmt.setInt(5, quest.getState());
			pstmt.setObject(6, new Date());
			pstmt.execute();
		} finally {
			pstmt.close();
		}
	}
	
	public static void addQuestFlushInfo(Connection con, int playerId, int questId, int cd) throws SQLException {
		String insertSql = "REPLACE INTO quest_flush_info(player_id, quest_id, cd) VALUES (?,?,?)";
		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement(insertSql);
			pstmt.setInt(1, playerId);
			pstmt.setInt(2, questId);
			pstmt.setInt(3, cd);
			pstmt.execute();
		} finally {
			pstmt.close();
		}
	}
	
	public static void updateQuestFlushCount(Connection con, int playerId) throws SQLException {
		String updateSql = "UPDATE quest_flush_info SET cd = cd - 1 WHERE player_id = ? AND cd > 0";
		String deleteSql = "DELETE FROM quest_flush_info WHERE player_id = ? AND cd = 0";
		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement(updateSql);
			pstmt.setInt(1, playerId);
			pstmt.execute();
			
			pstmt.close();
			pstmt = con.prepareStatement(deleteSql);
			pstmt.setInt(1, playerId);
			pstmt.execute();
		} finally {
			pstmt.close();
		}
	}
	
	/**
	 * 获取任务信息
	 * 
	 * @param playerId
	 * @param questIds
	 * @return
	 */
	public static ArrayList<Quest> getQuestInfo(int playerId, HashMap<Integer, Integer> questIds) {
		Connection connect = ServerManager.gameDBConnect.getDBConnect();
		if (connect == null) { // 数据库连接池已满
			return null;
		}
		try {
			if (questIds != null) {
				getQuestFlushInfo(connect, playerId, questIds);
			}
			return getQuestInfo(connect, playerId);
		} finally {
			ServerManager.gameDBConnect.closeConnect(connect);
		}
	}
	
	/**
	 * 获取任务信息
	 * 
	 * @param connect
	 * @param playerId
	 * @return
	 */
	public static ArrayList<Quest> getQuestInfo(Connection connect, int playerId) {
		
		String sql = "SELECT * FROM quest_info WHERE player_id = ? ORDER BY quest_index";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ArrayList<Quest> questInfo = new ArrayList<>();
		
		try {
			pstmt = connect.prepareStatement(sql);
			pstmt.setInt(1, playerId);
			rs = pstmt.executeQuery();
			while (rs.next()) { 
				Quest quest = new Quest();
				quest.setPlayerId(playerId);
				quest.setIndex(rs.getInt("quest_index"));
				quest.setQuestId(rs.getInt("quest_id"));
				quest.setValue(rs.getInt("quest_value"));
				quest.setState(rs.getInt("quest_state"));
				questInfo.add(quest);
			}
			return questInfo;
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
	 * 获取CD中任务Id
	 * 
	 * @param connect
	 * @param playerId
	 * @param questIds
	 */
	public static void getQuestFlushInfo(Connection connect, int playerId, HashMap<Integer, Integer> questIds) {
		
		String sql = "SELECT * FROM quest_flush_info WHERE player_id = ? AND cd > 0";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			pstmt = connect.prepareStatement(sql);
			pstmt.setInt(1, playerId);
			rs = pstmt.executeQuery();
			while (rs.next()) { 
				questIds.put(rs.getInt("quest_id"), rs.getInt("cd"));
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
	}
	
	/**
	 * 刷新单条任务
	 * 
	 * @param playerId
	 * @param quest
	 * @return
	 */
	public static boolean flushSingleQuest(int playerId, Quest quest) {
		
		Connection connect = ServerManager.gameDBConnect.getDBConnect();
		if (connect == null) { // 数据库连接池已满
			return false;
		}
		try {
			connect.setAutoCommit(false);
			updateQuestInfo(connect, playerId, quest);
			UseCountDao.addUseCount(connect, playerId, UseCountConst.QUEST_FLUSH_COUNT);
			return true;
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
			ServerManager.gameDBConnect.closeConnect(connect);
		}
		return false;
	}
	
	public static boolean gmFlushSingleQuest(int playerId, Quest quest) {
		
		Connection connect = ServerManager.gameDBConnect.getDBConnect();
		if (connect == null) { // 数据库连接池已满
			return false;
		}
		try {
			connect.setAutoCommit(false);
			updateQuestInfo(connect, playerId, quest);
			return true;
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
			ServerManager.gameDBConnect.closeConnect(connect);
		}
		return false;
	}

	/**
	 * 刷新任务
	 * 
	 * @param playerId
	 * @param quests
	 * @return
	 */
	public static ArrayList<Quest> flushQuest(int playerId) {
		
		Connection connect = ServerManager.gameDBConnect.getDBConnect();
		if (connect == null) { // 数据库连接池已满
			return null;
		}
		try {
			ClearDao.dailyClearByPlayerId(connect, playerId);
			return getQuestInfo(connect, playerId);
		} finally {
			ServerManager.gameDBConnect.closeConnect(connect);
		}
	}
	
	public static void updateQuestInfo(Connection connect, int playerId, ArrayList<Quest> quests, ArrayList<Quest> completeQuest, ArrayList<Quest> changeQuest) throws SQLException {
		
		for (Quest quest : quests) {
			boolean isSet = false;
			if (quest.getState() == 1) {
				quest.setState(2);
				QuestModel questModel = ConfigData.questModels.get(quest.getQuestId());			
				if (questModel != null) {
					int addGold = AwardDao.addAward(connect, playerId, questModel.AwardId);
					if (addGold > 0) {
						completeQuest.add(quest);
						quest.setGold(addGold);
						isSet = true;
					}
				}
			}
			updateQuestInfo(connect, playerId, quest);
			if (!isSet && quest.isChange()) {
				changeQuest.add(quest);
			}
		}
	}
	
}