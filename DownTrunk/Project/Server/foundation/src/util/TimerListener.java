package util;

import java.util.EventListener;

public abstract interface TimerListener extends EventListener {
	public abstract boolean timeListening();

	public abstract void onTimer(TimerEvent paramTimerEvent);
}