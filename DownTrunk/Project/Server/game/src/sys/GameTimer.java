package sys;

import java.util.Calendar;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import util.ErrorPrint;
import util.TimeFormat;

public class GameTimer {
	
	private static int SIZE = 10;
	
	private static GameTimer instance;
	
	private static ScheduledExecutorService scheduledExecutorService;
	
	public static GameTimer getInstance() {
		if (instance == null) {
			instance = new GameTimer();
		}

		return instance;
	}

	public static ScheduledExecutorService getScheduled() {
		if (scheduledExecutorService == null) {
			scheduledExecutorService = Executors.newScheduledThreadPool(SIZE); 
		}
		return scheduledExecutorService;
	}
	
	public boolean registerOnceTask(Runnable command, String time) {
		try {
			Calendar runTime = TimeFormat.getTimeByStr(time);
			Calendar now = Calendar.getInstance();
			if (runTime.getTimeInMillis() > now.getTimeInMillis()) {
				getScheduled().schedule(command, runTime.getTimeInMillis() - now.getTimeInMillis(), TimeUnit.MILLISECONDS);
				return true;
			}
			return false;
		} catch (Exception e) {
			ErrorPrint.print(e);
			return false;
		}
	}
}
