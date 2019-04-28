package util;

/**
 * 类说明：基于数组的双端队列
 * 
 */

public class Deque {

	/* fields */
	/** 队列的对象数组 */
	private Object[] objArr;

	/** 队列的头 */
	private int head;

	/** 队列的尾 */
	private int tail;

	/** 队列的长度 */
	private int top;

	/* constructors */
	/** 按指定的大小构造一个双端队列 */
	public Deque(int capacity) {
		if (capacity < 1)
			throw new IllegalArgumentException(
					"ZDeque <init>, invalid capacity");
		objArr = new Object[capacity];
		head = 0;
		tail = 0;
		top = 0;
	}

	/* properties */
	/** 获得队列的长度 */
	public int size() {
		return top;
	}

	/** 获得队列的容积 */
	public int capacity() {
		return objArr.length;
	}

	/** 判断队列是否为空 */
	public boolean isEmpty() {
		return top == 0;
	}

	/** 判断队列是否已满 */
	public boolean isFull() {
		return top == objArr.length;
	}

	/** 得到队列的对象数组 */
	public Object[] getArray() {
		return objArr;
	}

	/* methods */
	/** 将对象放入到队列头部 */
	public void pushHead(Object obj) {
		if (top == objArr.length)
			throw new ArrayIndexOutOfBoundsException(
					"ZDeque pushHead, queue is full");
		if (top == 0) {
			tail = 0;
			head = 0;
			objArr[0] = obj;
		} else {
			head--;
			if (head < 0)
				head = objArr.length - 1;
			objArr[head] = obj;
		}
		top++;
	}

	/** 将对象放入到队列尾部 */
	public void pushTail(Object obj) {
		if (top == objArr.length)
			throw new ArrayIndexOutOfBoundsException(
					"ZDeque pushTail, queue is full");
		if (top == 0) {
			tail = 0;
			head = 0;
			objArr[0] = obj;
		} else {
			tail++;
			if (tail == objArr.length)
				tail = 0;
			objArr[tail] = obj;
		}
		top++;
	}

	/** 检索队列头部的对象 */
	public Object peekHead() {
		if (top == 0)
			throw new ArrayIndexOutOfBoundsException(
					"ZDeque peekHead, queue is empty");
		return objArr[head];
	}

	/** 检索队列尾部的对象 */
	public Object peekTail() {
		if (top == 0)
			throw new ArrayIndexOutOfBoundsException(
					"ZDeque peekTail, queue is empty");
		return objArr[tail];
	}

	/** 弹出队列头部的对象 */
	public Object popHead() {
		if (top == 0)
			throw new ArrayIndexOutOfBoundsException(
					"ZDeque popHead, queue is empty");
		Object obj = objArr[head];
		objArr[head] = null;
		top--;
		if (top > 0) {
			head++;
			if (head == objArr.length)
				head = 0;
		}
		return obj;
	}

	/** 弹出队列尾部的对象 */
	public Object popTail() {
		if (top == 0)
			throw new ArrayIndexOutOfBoundsException(
					"ZDeque popTail, queue is empty");
		Object obj = objArr[tail];
		top--;
		if (top > 0) {
			tail--;
			if (tail < 0)
				tail = objArr.length - 1;
		}
		return obj;
	}

	/** 清除队列 */
	public void clear() {
		for (int i = head, n = tail > head ? tail : objArr.length; i < n; i++)
			objArr[i] = null;
		for (int i = 0, n = tail > head ? 0 : tail; i < n; i++)
			objArr[i] = null;
		tail = 0;
		head = 0;
		top = 0;
	}

	/* common methods */
	@Override
	public String toString() {
		return super.toString() + "[" + top + "] ";
	}

}