package db.log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.ServerManager;
import app.ServerStaticInfo;
import config.model.recharge.RechargeModel;
import db.module.log.LogData;
import db.module.log.LogTable;
import db.module.player.Player;
import module.RechargeInfo;
import util.ErrorPrint;

/**
 * �˺����ݷ��ʶ���
 *
 */
public class LogDao {
	
	private static final Logger logger = LoggerFactory.getLogger(LogDao.class);

	public static void device(Player player, String deviceId, String ip, String logStr) {
		HashMap<String, Object> info = split(logStr);
		LogData logData = new LogData();
		logData.put("player_id", player.getPlayerId());
        logData.put("os", info.get("os"));//
        logData.put("os_version", "");//
	    logData.put("device_model", info.get("dm"));//
	    logData.put("device_name", info.get("dn"));
	    logData.put("device_type", info.get("dt"));
	    logData.put("device_guid", deviceId);// device_id
	    logData.put("sms", info.get("sms"));//
	    logData.put("gdn", info.get("gdn"));//
	    logData.put("gms", info.get("gms"));//
	    logData.put("pf", info.get("pf"));//
	    logData.put("idfa", info.get("idfa"));//
        logData.put("system_language", info.get("sl"));//
        LogDao.logRecord("�豸��Ϣ", logData);
	}

	public static void recharge(String orderId, Player player, RechargeInfo info, RechargeModel model, int number, int exNum, String ip) {
		LogData logData = new LogData();
		logData.put("order_id", orderId);// ������
		logData.put("player_id", player.getPlayerId());// ���id
		logData.put("account_id", player.getAccountId()); // �ʺ�
		logData.put("platform", info != null ? info.getPlatform() : ""); // ƽ̨ 
		logData.put("channel", info != null ? info.getChannel() : ""); // ����
		logData.put("server_id", ServerStaticInfo.getServerId()); // ������Id
		logData.put("shop_id", model.ID); // ��ƷId
		logData.put("shop_name", model.DisplayName); // ��Ʒ��
		logData.put("type", model.Type);
		logData.put("ex_number", exNum);
		logData.put("number", number);// ����
		logData.put("price", info != null ? info.getPrice() : 0);// ����
		logData.put("is_first", 0);// �Ƿ��׳�
		logData.put("ip", ip); // IP
		LogDao.logRecord("��ֵ", logData);
	}

	public static void logRecord(String action, LogData logData) {

		String tableName = LogTable.getInstance().get(action);
		if (tableName == null || tableName == "") {
			System.err.println("LogDao error! action:" + action + " not exist!");
			return;
		}

		logData.put("operator", action);
		logData.put("create_time", new Date());
		log(tableName, logData);
	}

	public static void money(int playerId, int type, int way, int num, int exNum, String ip, String arg) {
		LogData logData = new LogData();
		logData.put("player_id", playerId);// ���id
		logData.put("type", type); // ��ͨ���ͣ�1��� 2��ʯ 3Ԥ����ʯ��
		logData.put("way", way); // ;��
		logData.put("ex_number", exNum);
		logData.put("number", num);// ����
		logData.put("arg1", arg);// ��ע
		logData.put("ip", ip); // IP
		LogDao.logRecord("��������", logData);
	}

	public static void clubDiamond(int clubId, int type, int way, int num, int exNum, String ip, String arg) {
		LogData logData = new LogData();
		logData.put("club_id", clubId);// ���ֲ�id
		logData.put("type", type); // ��ͨ���ͣ�2��ʯ 3Ԥ����ʯ��
		logData.put("way", way); // ;��
		logData.put("ex_number", exNum);
		logData.put("number", num);// ����
		logData.put("arg1", arg);// ��ע
		logData.put("ip", ip); // IP
		LogDao.logRecord("���ֲ���ʯ����", logData);
	}

	public static void roomRecord(int roomId, int creatorId, int gameType, int roomType, int roomCharge, String arg) {
		LogData logData = new LogData();
		logData.put("room_id", roomId);// ����id
		logData.put("creator_id", creatorId); // ������id
		logData.put("game_type", gameType); // ��Ϸ����
		logData.put("room_type", roomType); // ��������
		logData.put("room_charge", roomCharge);// ����
		logData.put("arg1", arg);// ��ע
		LogDao.logRecord("�����¼", logData);
	}
	
	/**
	 * ��¼��־
	 * 
	 * @param player
	 * @param deviceId
	 * @param platform
	 * @param channel
	 * @param ip
	 * @param arg
	 */
	public static void login(Player player, String deviceId, String platform, String channel, String ip, String arg) {
		LogData logData = new LogData();
		logData.put("player_id", player.getPlayerId());// ���id
		logData.put("account_id", player.getAccountId()); // �ʺ�
		logData.put("device_id", deviceId); // �豸��
		logData.put("platform", platform); // ƽ̨ 
		logData.put("channel", channel); // ����
		logData.put("server_id", ServerStaticInfo.getServerId()); // ������Id
		logData.put("type", ServerStaticInfo.serverType);// ����������Game��Hall
		logData.put("arg1", arg);// ��ע
		logData.put("ip", ip); // IP
		LogDao.logRecord("��¼", logData);
	}

	/**
	 * ������ɫ��־
	 * 
	 * @param player
	 * @param deviceId
	 * @param platform
	 * @param channel
	 * @param ip
	 * @param arg
	 */
	public static void user(Player player, String deviceId, String platform, String channel, String ip, String arg) {
		LogData logData = new LogData();
		logData.put("player_id", player.getPlayerId());// ���id
		logData.put("account_id", player.getAccountId()); // �ʺ�
		logData.put("device_id", deviceId); // �豸��
		logData.put("platform", platform); // ƽ̨ 
		logData.put("channel", channel); // ����
		logData.put("server_id", ServerStaticInfo.getServerId()); // ������Id
		logData.put("arg1", arg);// ��ע
		logData.put("ip", ip); // IP
		LogDao.logRecord("������ɫ", logData);
	}
	
	public static void onlineLog(int olNumber, int maxNum) {
		LogData logData = new LogData();
		logData.put("online_player_num", olNumber);
		logData.put("max_player_num", maxNum);
		logData.put("server_id", ServerStaticInfo.getServerId());
		logData.put("create_time", new Date());
		log("log_online", logData);
	}
	
	private static void log(String tableName, LogData logData) {
		Connection connect = ServerManager.logDBConnect.getDBConnect();
		if (connect == null) { // ���ݿ����ӳ�����
			return;
		}
		String insertSql = "INSERT ignore INTO " + tableName + " (" + logData.getKeys() + ") VALUES("
				+ logData.getValues() + ")";
		PreparedStatement pstmt = null;

		try {

			pstmt = connect.prepareStatement(insertSql);
			Iterator<Entry<String, Object>> iterator = logData.entrySet().iterator();
			int i = 1;
			while (iterator.hasNext()) {
				Entry<String, Object> entry = iterator.next();
				pstmt.setObject(i, entry.getValue());
				i++;
			}
			pstmt.execute();

			return;
		} catch (Exception e) {
			ErrorPrint.print(e);
			// TODO ����log.sql
		} finally {
			try {
				pstmt.close();
				ServerManager.logDBConnect.closeConnect(connect);
			} catch (SQLException e) {
				ErrorPrint.print(e);
			}
		}
		return;
	}
	
	private static HashMap<String, Object> split(String logStr) {
		String[] split = logStr.split("\\|", -1);
		HashMap<String, Object> info = new HashMap<>();
		for (String str : split) {
			String[] s = str.split(",", -1);
			int d = s.length;
			if (d < 2) {
				logger.error("��ȡ�豸��Ϣ����" + s[0]);
			}
			info.put(s[0], s[1]);
		}
		return info;
	}
}
