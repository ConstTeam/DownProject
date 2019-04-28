package util;

public class AList {
	public static final Object[] NULL = new Object[0];

	Object[] array = NULL;

	public AList() {
		this(NULL);
	}

	public AList(Object[] array) {
		this.array = array;
	}

	public AList(int len) {
		this.array = new Object[len];
	}

	public AList(AList list) {
		this.array = new Object[list.array.length];
		System.arraycopy(list.array, 0, this.array, 0, this.array.length);
	}

	public int size() {
		return this.array.length;
	}

	public Object[] getArray() {
		return this.array;
	}

	public boolean contain(Object o) {
		Object[] tmp = this.array;
		for (int i = 0; i < tmp.length; i++) {
			if (o.equals(tmp[i]))
				return true;
		}
		return false;
	}

	public synchronized void add(Object o) {
		if (o == null)
			return;
		int i = this.array.length;
		Object[] tmp = new Object[i + 1];
		if (i > 0)
			System.arraycopy(this.array, 0, tmp, 0, i);
		tmp[i] = o;
		this.array = tmp;
	}

	public synchronized void add(Object[] objs) {
		if (objs == null)
			return;
		int i = this.array.length;
		if (i == 0) {
			this.array = objs;
			return;
		}
		Object[] tmp = new Object[i + objs.length];
		System.arraycopy(this.array, 0, tmp, 0, i);
		System.arraycopy(objs, 0, tmp, i, objs.length);
		this.array = tmp;
	}

	public synchronized boolean remove(Object o) {
		if (o == null)
			return false;
		int i = 0;
		for (; i < this.array.length; i++) {
			if (o.equals(this.array[i]))
				break;
		}
		if (i == this.array.length)
			return false;
		if (this.array.length == 1) {
			this.array = NULL;
			return true;
		}
		Object[] tmp = new Object[this.array.length - 1];
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