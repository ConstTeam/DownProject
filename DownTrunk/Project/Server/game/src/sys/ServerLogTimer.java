package sys;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import app.ServerStaticInfo;
import db.log.LogDao;
import util.ErrorPrint;

public class ServerLogTimer {

	private static final Logger logger = LoggerFactory.getLogger(ServerLogTimer.class);// 日志类
	
	private static int max_online_num = 0;

	static class MyTask extends java.util.TimerTask {
		@Override
		public void run() {
			int number = 0;
			int maxOnlineNum = max_online_num;
			switch (ServerStaticInfo.getServerType()) {
			case "Hall":
				number = HallServerOnlineManager.getInstance().getOnlineCount();
				break;
			case "Game":
				number = GameServerOnlineManager.getInstance().getOnlineCount();
				break;
			}
			logger.info("============ 服务器状态记录。当前在线人数：" + number + " 峰值在线人数：" + maxOnlineNum + " ============");
			try {
				LogDao.onlineLog(number, maxOnlineNum);
				max_online_num = number;
			} catch (Exception e) {
				ErrorPrint.print(e);
			}
		}

	}

	public static void setTimer() {

		Calendar now = Calendar.getInstance();
		Calendar clearTime = Calendar.getInstance();
		int minute = clearTime.get(Calendar.MINUTE);
		minute = minute - (minute % 10); // 10分钟一次
		clearTime.set(Calendar.MINUTE, minute);
		clearTime.set(Calendar.SECOND, 0);
		clearTime.add(Calendar.MINUTE, 10);

		long totalSeconds = clearTime.getTimeInMillis() - now.getTimeInMillis();
		GameTimer.getScheduled().scheduleAtFixedRate(new MyTask(), totalSeconds, 60 * 1000 * 10, TimeUnit.MILLISECONDS);
	}

	public static void setMaxOnlineNum(int num) {
		if (num > max_online_num) {
			max_online_num = num;
		}
	}

	public static int getMaxOnlineNum() {
		return max_online_num;
	}
}
