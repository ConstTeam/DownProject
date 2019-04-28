package net;

/**
 * ��˵�����̷߳��ʴ����࣬ �����߳�ͨѶ�ķ��ͣ��߳����ߣ�������߳�ͨѶ����
 * 
 */

public abstract class ThreadAccessHandler {

	/* fields */
	/** �߳�ͨѶ�� */
	int accessId = hashCode();

	/** �߳�ͨѶ���� */
	Object accessResult = ThreadAccess.NONE;

	/* properties */
	/** ����߳�ͨѶ�� */
	public int getAccessId() {
		return accessId;
	}

	/** ����߳�ͨѶ���� */
	public Object getAccessResult() {
		return accessResult;
	}

	/* methods */
	/** �̷߳��ʴ����� */
	public abstract void handle();

	/** �����߳� */
	public abstract void doNotify();

}