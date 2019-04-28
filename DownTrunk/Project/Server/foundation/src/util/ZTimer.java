package util;

import java.util.ArrayList;

public class ZTimer {
	public static final int RUN_TIME = 50;
	public static final boolean DEBUG = false;
	private static ZTimer timer = null;

	public static ArrayList<ZTimer> timerList = new ArrayList<ZTimer>();

	public long time = 0L;

	public int timeOutNumber = 0;

	public String name = null;

	public TimerEvent currentEvent = null;

	private TimerEventList list = null;

	private boolean active = false;

	public int runTime = 0;

	private Thread run = null;

	public static ZTimer getZTimer() {
		if (timer == null) {
			timer = getNewTimer("nomal");
		}
		return timer;
	}

	private ZTimer() {
		this(50);
	}

	private ZTimer(int time) {
		this.runTime = time;
		this.list = new TimerEventList();
	}

	public static ZTimer getNewTimer(String name, int time) {
		ZTimer zt = new ZTimer(time);
		zt.name = name;
		timerList.add(zt);
		return zt;
	}

	public static ZTimer getNewTimer() {
		return getNewTimer(null);
	}

	public static ZTimer getNewTimer(String name) {
		return getNewTimer(name, 50);
	}

	public synchronized boolean isActive() {
		return this.active;
	}

	public int getRunTime() {
		return this.runTime;
	}

	public void setRunTime(int time) {
		this.runTime = time;
	}

	public int getPriority() {
		if (this.run == null)
			return -1;
		return this.run.getPriority();
	}

	public void setPriority(int priority) {
		if (this.run == null)
			return;
		this.run.setPriority(priority);
	}

	public synchronized void start() {
		if (this.active)
			return;
		this.active = true;
		this.time = System.currentTimeMillis();
		this.timeOutNumber = 0;
		this.run = new Run();
		this.run.setName("ZTimer");
		this.run.setDaemon(true);
		this.run.start();
	}

	public Thread getRun() {
		return this.run;
	}

	public TimerEvent[] getTimerEvents() {
		return this.list.getArray();
	}

	public boolean hasEvent(TimerListener listener) {
		TimerEvent[] tes = this.list.getArray();
		for (int i = 0; i < tes.length; i++) {
			if (tes[i].getTimerListener() == listener)
				return true;
		}
		return false;
	}

	public void addTimerEvent(TimerEvent e) {
		this.list.add(e);
	}

	public void removeTimerEvent(TimerEvent e) {
		this.list.remove(e);
	}

	public void removeTimerEvent(TimerListener listener) {
		removeTimerEvent(listener, null);
	}

	public void removeTimerEvent(TimerListener listener, Object parameter) {
		TimerEvent[] es = this.list.getArray();
		for (int i = 0; i < es.length; i++) {
			if (listener != es[i].getTimerListener())
				continue;
			if ((parameter != null)
					&& (!parameter.equals(es[i].getParameter())))
				continue;
			this.list.remove(es[i]);
		}
	}

	public void fire(long currentTime) {
		TimerEvent[] es = this.list.getArray();
		for (int i = 0; i < es.length; i++) {
			this.time = System.currentTimeMillis();
			if ((es[i].getCount() == 0)
					|| (!es[i].getTimerListener().timeListening())) {
				removeTimerEvent(es[i]);
			} else if (currentTime >= es[i].getNextTime()) {
				this.currentEvent = es[i];
				es[i].fire(currentTime);
				this.currentEvent = null;
			}
		}
		this.time = System.currentTimeMillis();
	}

	public void clear() {
		this.list.clear();
	}

	public void restart() {
		this.active = false;
		if (this.run != null) {
			this.run.interrupt();
		} else
			return;
		try {
			Thread.sleep(1000L);
		} catch (Exception ee) {
			ee.printStackTrace();
		}
		start();
	}

	public void close() {
		this.active = false;
		if (this.run != null)
			this.run.interrupt();
		clear();
	}

	public String toString() {
		return super.toString() + "[" + this.active + this.runTime + ","
				+ this.list.size() + "] ";
	}

	class Run extends Thread {
		Run() {
		}

		public void run() {
			while (ZTimer.this.isActive())
				try {
					ZTimer.this.fire(System.currentTimeMillis());
					Thread.sleep(ZTimer.this.runTime);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
	}
}