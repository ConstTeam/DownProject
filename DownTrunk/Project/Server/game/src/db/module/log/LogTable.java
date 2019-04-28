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
		this.put("��������", "log_curr");
		this.put("���ֲ���ʯ����", "log_curr_club");
		this.put("�豸��Ϣ", "log_device");
		this.put("��¼", "log_login");
		this.put("��������", "log_online");
		this.put("������ɫ", "log_user");
		this.put("��ֵ", "log_recharge");
		this.put("�����¼", "log_room");
		this.put("", "log");
	}
}
