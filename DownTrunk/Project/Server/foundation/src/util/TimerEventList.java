package util;

public final class TimerEventList {
	public static final TimerEvent[] NULL = new TimerEvent[0];

	TimerEvent[] array = NULL;

	public TimerEventList() {
		this(NULL);
	}

	public TimerEventList(TimerEvent[] array) {
		this.array = array;
	}

	public TimerEventList(TimerEventList list) {
		this.array = new TimerEvent[list.array.length];
		System.arraycopy(list.array, 0, this.array, 0, this.array.length);
	}

	public int size() {
		return this.array.length;
	}

	public TimerEvent[] getArray() {
		return this.array;
	}

	public boolean contain(TimerEvent e) {
		TimerEvent[] tmp = this.array;
		for (int i = 0; i < tmp.length; i++) {
			if (e.equals(tmp[i]))
				return true;
		}
		return false;
	}

	public synchronized void add(TimerEvent e) {
		if (e == null)
			return;
		int i = this.array.length;
		TimerEvent[] tmp = new TimerEvent[i + 1];
		if (i > 0)
			System.arraycopy(this.array, 0, tmp, 0, i);
		tmp[i] = e;
		this.array = tmp;
	}

	public synchronized void add(TimerEvent[] es) {
		if (es == null)
			return;
		int i = this.array.length;
		if (i == 0) {
			this.array = es;
			return;
		}
		TimerEvent[] tmp = new TimerEvent[i + es.length];
		System.arraycopy(this.array, 0, tmp, 0, i);
		System.arraycopy(es, 0, tmp, i, es.length);
		this.array = tmp;
	}

	public synchronized boolean remove(TimerEvent e) {
		if (e == null)
			return false;
		int i = 0;
		for (; i < this.array.length; i++) {
			if (e.equals(this.array[i]))
				break;
		}
		if (i == this.array.length)
			return false;
		if (this.array.length == 1) {
			this.array = NULL;
			return true;
		}
		TimerEvent[] tmp = new TimerEvent[this.array.length - 1];
		if (i > 0)
			System.arraycopy(this.array, 0, tmp, 0, i);
		if (i < tmp.length)
			System.arraycopy(this.array, i + 1, tmp, i, tmp.length - i);
		this.array = tmp;
		return true;
	}

	public synchronized void clear() {
		this.array = NULL;
	}
}