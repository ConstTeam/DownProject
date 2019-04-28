package net;

public interface ThreadAccess {
	/* static fields */
	/** �޶������� */
	public static final Object NONE = new Object();

	/** �޷��ص����� */
	public static final Object VOID = new Object();

	/** Ĭ�ϵĳ�ʱ������30�� */
	public static final int TIMEOUT = 30000;

	/**
	 * �첽���ʷ����� ����handlerΪ�̷߳��ʴ���ӿڡ�
	 */
	void access4nonwait(ThreadAccessHandler handler);

	/**
	 * �̷߳��ʷ����� ����handlerΪ�̷߳��ʴ���ӿڣ� ����ֵ���ܵ���NONE,VOID
	 */
	Object access(ThreadAccessHandler handler);

	/**
	 * �̷߳��ʷ����� ����handlerΪ�̷߳��ʴ���ӿڣ� ����timeoutΪ��ʱʱ�䣬 ����ֵ���ܵ���NONE,VOID
	 */
	Object access(ThreadAccessHandler handler, int timeout);

	/**
	 * �̻߳��ѷ����� ����idΪͨѶ�����ţ� ����objΪͨѶ���ݣ�ע�ⲻ��ΪNONE
	 */
	void notify(int id, Object obj);

	/** �Ƴ����߶��� */
	void removeHandler(ThreadAccessHandler handler);

}
