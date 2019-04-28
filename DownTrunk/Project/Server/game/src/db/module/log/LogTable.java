package db.module.log;

import java.util.HashMap;

public class LogTable extends HashMap<String, String> {

	private static final long serialVersionUID = 1L;

	public static final int NON = 0;

	private static LogTable instance;

	public static LogTable getInstance() {
		if (instance == null) {
			instance = new LogTable();
		}

		return instance;
	}

	private LogTable() {
		this.put("货币流向", "log_curr");
		this.put("俱乐部钻石流向", "log_curr_club");
		this.put("设备信息", "log_device");
		this.put("登录", "log_login");
		this.put("在线人数", "log_online");
		this.put("创建角色", "log_user");
		this.put("充值", "log_recharge");
		this.put("房间记录", "log_room");
		this.put("", "log");
	}
}
