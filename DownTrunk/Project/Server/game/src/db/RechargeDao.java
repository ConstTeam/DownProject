package db;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.ServerManager;
import config.ConfigData;
import config.model.recharge.RechargeModel;
import db.log.LogDao;
import db.module.player.Player;
import module.RechargeInfo;
import util.ErrorPrint;

/**
 * ��ֵDAO
 * 
 */
public class RechargeDao {

	private static final Logger logger = LoggerFactory.getLogger(RechargeDao.class);

	/**
	 * ���Ӷ�����Ϣ
	 * 
	 * @param rechargeInfo
	 * @return
	 */
	public static boolean addRechargeInfo(RechargeInfo rechargeInfo) {

		String insertSql = "INSERT IGNORE INTO recharge_info "
				+ "(account_id,player_id,order_id,platform,channel,price,diamond,shop_id,is_succ,create_time) "
				+ "VALUES(?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement pstmt = null;
		Connection con = ServerManager.gameDBConnect.getDBConnect();
		if (con == null) {
			logger.error("���ݿ����ӳ�����");
			return false;
		}
		try {
			if (getRechargeInfo(con, rechargeInfo.getOrderId()) != null) {
				return false;
			}
			int playerId = rechargeInfo.getPlayerId();
			String accountId = rechargeInfo.getAccountId();
			pstmt = con.prepareStatement(insertSql);
			pstmt.setString(1, accountId);
			pstmt.setInt(2, playerId);
			pstmt.setString(3, rechargeInfo.getOrderId());
			pstmt.setString(4, rechargeInfo.getPlatform());
			pstmt.setString(5, rechargeInfo.getChannel());
			pstmt.setString(6, rechargeInfo.getPrice());
			pstmt.setInt(7, rechargeInfo.getDiamond());
			pstmt.setString(8, rechargeInfo.getPayCode());
			pstmt.setBoolean(9, false);
			pstmt.setObject(10, new Date(rechargeInfo.getTime().getTimeInMillis()));
			
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			ErrorPrint.print(e);
		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (SQLException e1) {
				ErrorPrint.print(e1);
			}
			ServerManager.gameDBConnect.closeConnect(con);
		}
		return false;
	}

	/**
	 * ���Ӵ��󶩵���Ϣ
	 * 
	 * @param rechargeInfo
	 * @return
	 */
	public static boolean addErrorRechargeInfo(RechargeInfo rechargeInfo) {

		Connection con = ServerManager.gameDBConnect.getDBConnect();
		if (con == null) {
			logger.error("���ݿ����ӳ�����");
			return false;
		}

		String insertSql = "INSERT IGNORE INTO recharge_info_error "
				+ "(account_id,player_id,order_id,platform,channel,price,diamond,shop_id,is_succ,create_time) "
				+ "VALUES(?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement pstmt = null;
		try {
			int playerId = rechargeInfo.getPlayerId();
			String accountId = rechargeInfo.getAccountId();
			pstmt = con.prepareStatement(insertSql);
			pstmt.setString(1, accountId);
			pstmt.setInt(2, playerId);
			pstmt.setString(3, rechargeInfo.getOrderId());
			pstmt.setString(4, rechargeInfo.getPlatform());
			pstmt.setString(5, rechargeInfo.getChannel());
			pstmt.setString(6, rechargeInfo.getPrice());
			pstmt.setInt(7, rechargeInfo.getDiamond());
			pstmt.setString(8, rechargeInfo.getPayCode());
			pstmt.setBoolean(9, false);
			pstmt.setObject(10, new Date(rechargeInfo.getTime().getTimeInMillis()));
			pstmt.execute();

			return true;
		} catch (SQLException e) {
			ErrorPrint.print(e);
		} finally {
			try {
				pstmt.close();
			} catch (SQLException e1) {
				ErrorPrint.print(e1);
			}
			ServerManager.gameDBConnect.closeConnect(con);
		}
		return false;
	}

	/**
	 * ���ӳ�ʼ��������Ϣ
	 * 
	 * @param rechargeInfo
	 * @return
	 */
	public static boolean addPreRechargeInfo(RechargeInfo rechargeInfo) {

		Connection con = ServerManager.gameDBConnect.getDBConnect();
		if (con == null) {
			logger.error("���ݿ����ӳ�����");
			return false;
		}

		String insertSql = "INSERT IGNORE INTO recharge_info_pre "
				+ "(account_id,player_id,order_id,platform,channel,price,diamond,shop_id,is_succ,create_time) "
				+ "VALUES(?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement pstmt = null;
		try {
			int playerId = rechargeInfo.getPlayerId();
			String accountId = rechargeInfo.getAccountId();
			pstmt = con.prepareStatement(insertSql);
			pstmt.setString(1, accountId);
			pstmt.setInt(2, playerId);
			pstmt.setString(3, rechargeInfo.getOrderId());
			pstmt.setString(4, rechargeInfo.getPlatform());
			pstmt.setString(5, rechargeInfo.getChannel());
			pstmt.setString(6, rechargeInfo.getPrice());
			pstmt.setInt(7, rechargeInfo.getDiamond());
			pstmt.setString(8, rechargeInfo.getPayCode());
			pstmt.setBoolean(9, false);
			pstmt.setObject(10, new Date(rechargeInfo.getTime().getTimeInMillis()));
			pstmt.execute();

			return true;
		} catch (SQLException e) {
			ErrorPrint.print(e);
			logger.error("��ʼ��������Ϣʧ�ܣ�orderId��{}��playerId��{}��payCode��{}��price��{}", rechargeInfo.getOrderId(), 
					rechargeInfo.getPlayerId(), rechargeInfo.getPayCode(), rechargeInfo.getPrice());
		} finally {
			try {
				pstmt.close();
			} catch (SQLException e1) {
				ErrorPrint.print(e1);
			}
			ServerManager.gameDBConnect.closeConnect(con);
		}
		return false;
	}
	
	private static boolean changeRechargeState(Connection con, String orderId) throws SQLException {
		
		String insertSql = "UPDATE recharge_info SET is_succ = 1 WHERE order_id = ? AND is_succ = 0";
		PreparedStatement pstmt = null;
		try {
			pstmt = con.prepareStatement(insertSql);
			pstmt.setString(1, orderId);
			return pstmt.executeUpdate() > 0;
		} finally {
			try {
				pstmt.close();
			} catch (SQLException e) {
				ErrorPrint.print(e);
			}
		}
	}

	/**
	 * ��ȡ������Ϣ
	 * 
	 * @param con
	 * @param orderId
	 * @param platform
	 * @return
	 */
	public static RechargeInfo getRechargeInfo(Connection con, String orderId) {

		String selectSql = "SELECT * FROM recharge_info WHERE order_id = ?";
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			// ��ѯ��ɫ��Ϣ
			pstmt = con.prepareStatement(selectSql);
			pstmt.setString(1, orderId);

			rs = pstmt.executeQuery();
			if (rs.next()) {
				return getResult(rs);
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

	private static RechargeInfo getResult(ResultSet rs) throws SQLException {

		RechargeInfo rechargeInfo = new RechargeInfo(rs.getInt("player_id"), rs.getString("order_id"),
				rs.getString("shop_id"), rs.getString("price"), rs.getInt("diamond"), rs.getString("platform"),
				rs.getString("channel"));
		rechargeInfo.getTime().setTime((Date) rs.getObject("create_time"));
		rechargeInfo.setAccountId(rs.getString("account_id"));
		rechargeInfo.setDeal(rs.getBoolean("is_succ"));
		rechargeInfo.setLid((BigInteger) rs.getObject("lid"));
		return rechargeInfo;
	}

	/**
	 * ��ֵ����
	 * 
	 * @param rechargeInfo
	 * @param platform
	 * @return
	 */
	public static boolean recharge(RechargeInfo rechargeInfo) {

		int playerId = rechargeInfo.getPlayerId();
		RechargeModel rechargeModel = ConfigData.rechargeModels.get(rechargeInfo.getPayCode());
		if (rechargeModel == null) {
			logger.error("��ֵʧ�ܣ���ƷId�����ñ��в����ڡ���ң�{}��payCode��{}", playerId, rechargeInfo.getPayCode());
			return false;
		}
		int exNum = 0;
		int number = rechargeModel.Num;
		Connection con = ServerManager.gameDBConnect.getDBConnect();
		if (con == null) {
			logger.error("���ݿ����ӳ�����");
			return false;
		}
		try {
			Player player = PlayerDao.getPlayerInfo(con, playerId);
			if (player == null) {
				logger.error("��ֵʧ�ܣ���ȡ�����Ϣʧ�ܡ���ң�{}��payCode��{}", playerId, rechargeInfo.getPayCode());
				return false;
			}
			
			con.setAutoCommit(false);
			
			/*
			 *  ��ֵ
			 */
			switch (rechargeModel.Type) {
			case DBModuleConst.DIAMOND:
				PlayerDao.addDiamond(con, playerId, number);
				exNum = player.getDiamond();
				break;
			case DBModuleConst.GOLD:
				PlayerDao.addGold(con, playerId, number);
				exNum = player.getGold();
				break;
			}
			
			if (!changeRechargeState(con, rechargeInfo.getOrderId())) {
				con.rollback();
				return false;
			}
			
			con.commit();
			// �����־
			try {
				// ��ֵ���
				LogDao.recharge(rechargeInfo.getOrderId(), player, rechargeInfo, rechargeModel, number, exNum, "");
				// ��������
				LogDao.money(player.getPlayerId(), rechargeModel.Type, 100, number, exNum,
						"", "��ֵ");
			} catch (Exception e) {
				ErrorPrint.print(e);
			}
			return true;
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
			} catch (SQLException e2) {
				ErrorPrint.print(e2);
			}
			ServerManager.gameDBConnect.closeConnect(con);
		}
		return false;
	}
}
