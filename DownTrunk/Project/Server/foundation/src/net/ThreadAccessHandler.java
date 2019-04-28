package net;

/**
 * 类说明：线程访问处理类， 用于线程通讯的发送，线程休眠，及存放线程通讯数据
 * 
 */

public abstract class ThreadAccessHandler {

	/* fields */
	/** 线程通讯号 */
	int accessId = hashCode();

	/** 线程通讯数据 */
	Object accessResult = ThreadAccess.NONE;

	/* properties */
	/** 获得线程通讯号 */
	public int getAccessId() {
		return accessId;
	}

	/** 获得线程通讯数据 */
	public Object getAccessResult() {
		return accessResult;
	}

	/* methods */
	/** 线程访问处理方法 */
	public abstract void handle();

	/** 唤醒线程 */
	public abstract void doNotify();

}