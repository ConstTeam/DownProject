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
 * 账号数据访问对象
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
        LogDao.logRecord("设备信息", logData);
	}

	public static void recharge(String orderId, Player player, RechargeInfo info, RechargeModel model, int number, int exNum, String ip) {
		LogData logData = new LogData();
		logData.put("order_id", orderId);// 订单号
		logData.put("player_id", player.getPlayerId());// 玩家id
		logData.put("account_id", player.getAccountId()); // 帐号
		logData.put("platform", info != null ? info.getPlatform() : ""); // 平台 
		logData.put("channel", info != null ? info.getChannel() : ""); // 渠道
		logData.put("server_id", ServerStaticInfo.getServerId()); // 服务器Id
		logData.put("shop_id", model.ID); // 商品Id
		logData.put("shop_name", model.DisplayName); // 商品名
		logData.put("type", model.Type);
		logData.put("ex_number", exNum);
		logData.put("number", number);// 数量
		logData.put("price", info != null ? info.getPrice() : 0);// 单价
		logData.put("is_first", 0);// 是否首充
		logData.put("ip", ip); // IP
		LogDao.logRecord("充值", logData);
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
		logData.put("player_id", playerId);// 玩家id
		logData.put("type", type); // 流通类型（1金币 2钻石 3预扣钻石）
		logData.put("way", way); // 途径
		logData.put("ex_number", exNum);
		logData.put("number", num);// 数量
		logData.put("arg1", arg);// 备注
		logData.put("ip", ip); // IP
		LogDao.logRecord("货币流向", logData);
	}

	public static void clubDiamond(int clubId, int type, int way, int num, int exNum, String ip, String arg) {
		LogData logData = new LogData();
		logData.put("club_id", clubId);// 俱乐部id
		logData.put("type", type); // 流通类型（2钻石 3预扣钻石）
		logData.put("way", way); // 途径
		logData.put("ex_number", exNum);
		logData.put("number", num);// 数量
		logData.put("arg1", arg);// 备注
		logData.put("ip", ip); // IP
		LogDao.logRecord("俱乐部钻石流向", logData);
	}

	public static void roomRecord(int roomId, int creatorId, int gameType, int roomType, int roomCharge, String arg) {
		LogData logData = new LogData();
		logData.put("room_id", roomId);// 房间id
		logData.put("creator_id", creatorId); // 创建者id
		logData.put("game_type", gameType); // 游戏类型
		logData.put("room_type", roomType); // 房间类型
		logData.put("room_charge", roomCharge);// 房费
		logData.put("arg1", arg);// 备注
		LogDao.logRecord("房间记录", logData);
	}
	
	/**
	 * 登录日志
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
		logData.put("player_id", player.getPlayerId());// 玩家id
		logData.put("account_id", player.getAccountId()); // 帐号
		logData.put("device_id", deviceId); // 设备号
		logData.put("platform", platform); // 平台 
		logData.put("channel", channel); // 渠道
		logData.put("server_id", ServerStaticInfo.getServerId()); // 服务器Id
		logData.put("type", ServerStaticInfo.serverType);// 服务器类型Game或Hall
		logData.put("arg1", arg);// 备注
		logData.put("ip", ip); // IP
		LogDao.logRecord("登录", logData);
	}

	/**
	 * 创建角色日志
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
		logData.put("player_id", player.getPlayerId());// 玩家id
		logData.put("account_id", player.getAccountId()); // 帐号
		logData.put("device_id", deviceId); // 设备号
		logData.put("platform", platform); // 平台 
		logData.put("channel", channel); // 渠道
		logData.put("server_id", ServerStaticInfo.getServerId()); // 服务器Id
		logData.put("arg1", arg);// 备注
		logData.put("ip", ip); // IP
		LogDao.logRecord("创建角色", logData);
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
		if (connect == null) { // 数据库连接池已满
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
			// TODO 存入log.sql
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
				logger.error("读取设备信息有误" + s[0]);
			}
			info.put(s[0], s[1]);
		}
		return info;
	}
}
