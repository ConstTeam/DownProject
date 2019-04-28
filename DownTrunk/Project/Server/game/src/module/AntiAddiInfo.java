package module;

import java.util.ArrayList;
import java.util.Calendar;

public class AntiAddiInfo {

	public static int ANTI_TIME_ONE = 1;
	public static int ANTI_TIME_TWO = 2;
	public static int ANTI_TIME_THREE = 3;
	public static long ANTI_TIME_ONLINE_TWO = 3*60*60*1000;// 3Сʱ
	public static long ANTI_TIME_ONLINE_THREE = 5*60*60*1000;// 5Сʱ
	public static long ANTI_TIME_OFFLINE_TIME = 5*60*60*1000;// 5Сʱ
	

	public static long ANTI_TIME_NOTICE_ONE = 1*60*60*1000;// 1Сʱ
	public static long ANTI_TIME_NOTICE_TWO = 30*60*1000;// 30����
	public static long ANTI_TIME_NOTICE_THREE = 15*60*1000;// 15����
	
	public static ArrayList<Long> antiAddiTimes = null;
	/** ���Id */
	private int playerId;
	/** ����ʱ�� */
	private Long onLineTime;
	/** ����ʱ�� */
	private Calendar loginTime;
	/** ����ʱ�� */
	private Calendar logoutTime;
	
	public AntiAddiInfo() {
		loginTime = Calendar.getInstance();
		logoutTime = Calendar.getInstance();
	}
	
	public int getPlayerId() {
		return playerId;
	}
	public void setPlayerId(int playerId) {
		this.playerId = playerId;
	}
	public Long getOnLineTime() {
		return onLineTime;
	}
	public void setOnLineTime(Long onLineTime) {
		this.onLineTime = onLineTime;
	}
	public Calendar getLoginTime() {
		return loginTime;
	}
	public void setLoginTime(Calendar loginTime) {
		this.loginTime = loginTime;
	}
	public Calendar getLogoutTime() {
		return logoutTime;
	}
	public void setLogoutTime(Calendar logoutTime) {
		this.logoutTime = logoutTime;
	}
	public static ArrayList<Long> getAntiAddiTimes(){
		if(antiAddiTimes == null){
			antiAddiTimes = new ArrayList<>();
			antiAddiTimes.add((long)0);
			antiAddiTimes.add((long)(5*60*1000));
			antiAddiTimes.add((long)(10*60*1000));
			antiAddiTimes.add((long)(15*60*1000));
			antiAddiTimes.add((long)(20*60*1000));
		}
		return antiAddiTimes;
	}
}
