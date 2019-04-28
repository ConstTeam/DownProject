package net;

public interface ThreadAccess {
	/* static fields */
	/** 无对象类型 */
	public static final Object NONE = new Object();

	/** 无返回的类型 */
	public static final Object VOID = new Object();

	/** 默认的超时常量，30秒 */
	public static final int TIMEOUT = 30000;

	/**
	 * 异步访问方法， 参数handler为线程访问处理接口。
	 */
	void access4nonwait(ThreadAccessHandler handler);

	/**
	 * 线程访问方法， 参数handler为线程访问处理接口， 返回值可能等于NONE,VOID
	 */
	Object access(ThreadAccessHandler handler);

	/**
	 * 线程访问方法， 参数handler为线程访问处理接口， 参数timeout为超时时间， 返回值可能等于NONE,VOID
	 */
	Object access(ThreadAccessHandler handler, int timeout);

	/**
	 * 线程唤醒方法， 参数id为通讯索引号， 参数obj为通讯数据，注意不能为NONE
	 */
	void notify(int id, Object obj);

	/** 移除休眠对象 */
	void removeHandler(ThreadAccessHandler handler);

}
