package util;

public class TimerEvent {
	public static final int INFINITE_CYCLE = -1;
	private TimerListener listener = null;

	private Object parameter = null;

	private int intervalTime = 0;

	private int count = 0;

	private int initTime = 0;

	private boolean absolute = false;

	private long startTime = 0L;

	private long currentTime = 0L;

	private long nextTime = 0L;

	private boolean debug = true;

	public TimerEvent(TimerListener listener, Object parameter, int intervalTime) {
		this(listener, parameter, intervalTime, -1, 0, false);
	}

	public TimerEvent(TimerListener listener, Object parameter,
			int intervalTime, boolean absolute) {
		this(listener, parameter, intervalTime, -1, 0, absolute);
	}

	public TimerEvent(TimerListener listener, Object parameter,
			int intervalTime, int count) {
		this(listener, parameter, intervalTime, count, 0, false);
	}

	public TimerEvent(TimerListener listener, Object parameter,
			int intervalTime, int count, boolean absolute) {
		this(listener, parameter, intervalTime, count, 0, absolute);
	}

	public TimerEvent(TimerListener listener, Object parameter,
			int intervalTime, int count, int initTime) {
		this(listener, parameter, intervalTime, count, initTime, false);
	}

	public TimerEvent(TimerListener listener, Object parameter,
			int intervalTime, int count, int initTime, boolean absolute) {
		if (listener == null)
			throw new IllegalArgumentException(getClass().getName()
					+ " listener is null");
		this.listener = listener;
		if (parameter == null)
			throw new IllegalArgumentException(getClass().getName()
					+ " parameter is null");
		this.parameter = parameter;
		this.intervalTime = intervalTime;
		this.count = count;
		this.initTime = initTime;
		this.absolute = absolute;
		this.startTime = System.currentTimeMillis();
		this.nextTime = (this.startTime + initTime);
	}

	public TimerListener getTimerListener() {
		return this.listener;
	}

	public Object getParameter() {
		return this.parameter;
	}

	public void setParameter(Object parameter) {
		this.parameter = parameter;
	}

	public int getIntervalTime() {
		return this.intervalTime;
	}

	public void setIntervalTime(int time) {
		this.intervalTime = time;
	}

	public int getCount() {
		return this.count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public int getInitTime() {
		return this.initTime;
	}

	public boolean isAbsolute() {
		return this.absolute;
	}

	public void setAbsolute(boolean b) {
		this.absolute = b;
	}

	public long getStartTime() {
		return this.startTime;
	}

	public long getCurrentTime() {
		return this.currentTime;
	}

	public long getNextTime() {
		return this.nextTime;
	}

	public void setNextTime(long time) {
		this.nextTime = time;
	}

	public void setDebug(boolean b) {
		this.debug = b;
	}

	void fire(long currentTime) {
		this.count -= 1;
		this.currentTime = currentTime;
		try {
			this.listener.onTimer(this);
		} catch (Throwable e) {
			if (this.debug) {
				e.printStackTrace();
			}
		}
		this.nextTime = (this.absolute ? this.nextTime + this.intervalTime
				: currentTime + this.intervalTime);
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(getClass().getName());
		sb.append('@').append(hashCode());
		sb.append('[');
		sb.append(this.listener.getClass().getName());
		sb.append('@').append(this.listener.hashCode());
		sb.append(',');
		if (!(this.parameter instanceof String)) {
			sb.append(this.parameter.getClass().getName());
			sb.append('@').append(this.parameter.hashCode());
		} else {
			sb.append(this.parameter);
		}
		sb.append(',').append(this.intervalTime);
		sb.append(',').append(this.count);
		sb.append(',').append(this.initTime);
		sb.append(',').append(this.absolute);
		sb.append(',').append(this.startTime);
		sb.append(',').append(this.nextTime);
		sb.append(']');
		sb.append(' ');
		return sb.toString();
	}
}