package util;

/**
 * ��˵�������������˫�˶���
 * 
 */

public class Deque {

	/* fields */
	/** ���еĶ������� */
	private Object[] objArr;

	/** ���е�ͷ */
	private int head;

	/** ���е�β */
	private int tail;

	/** ���еĳ��� */
	private int top;

	/* constructors */
	/** ��ָ���Ĵ�С����һ��˫�˶��� */
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
	/** ��ö��еĳ��� */
	public int size() {
		return top;
	}

	/** ��ö��е��ݻ� */
	public int capacity() {
		return objArr.length;
	}

	/** �ж϶����Ƿ�Ϊ�� */
	public boolean isEmpty() {
		return top == 0;
	}

	/** �ж϶����Ƿ����� */
	public boolean isFull() {
		return top == objArr.length;
	}

	/** �õ����еĶ������� */
	public Object[] getArray() {
		return objArr;
	}

	/* methods */
	/** ��������뵽����ͷ�� */
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

	/** ��������뵽����β�� */
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

	/** ��������ͷ���Ķ��� */
	public Object peekHead() {
		if (top == 0)
			throw new ArrayIndexOutOfBoundsException(
					"ZDeque peekHead, queue is empty");
		return objArr[head];
	}

	/** ��������β���Ķ��� */
	public Object peekTail() {
		if (top == 0)
			throw new ArrayIndexOutOfBoundsException(
					"ZDeque peekTail, queue is empty");
		return objArr[tail];
	}

	/** ��������ͷ���Ķ��� */
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

	/** ��������β���Ķ��� */
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

	/** ������� */
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