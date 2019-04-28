package net;

import java.util.ArrayList;

/**
 * 类说明：线程访问代理器
 * 
 */

public final class ThreadAccessBroker implements ThreadAccess {

	/* fields */
	/** 线程休眠对象的列表 */
	private ArrayList<ThreadAccessHandler> handlerList = new ArrayList<ThreadAccessHandler>();

	/* methods */
	// /** 获得一个指定索引号的线程休眠包 */
	// private ThreadAccessHandler getHandler(int id)
	// {
	// ThreadAccessHandler handler = null;
	// synchronized (handlerList)
	// {
	// for (int i = 0, n = handlerList.size(); i < n; i++)
	// {
	// handler = (ThreadAccessHandler) (handlerList.get(i));
	// if (handler.accessId == id)
	// return handler;
	// }
	// }
	// return null;
	// }
	/** 增加一个指定的线程休眠包 */
	private void addHandler(ThreadAccessHandler handler) {
		// log.debug("addHandler: " + handler.accessId);
		synchronized (handlerList) {
			handlerList.add(handler);
		}
	}

	/** 移除一个指定的线程休眠包 */
	public void removeHandler(ThreadAccessHandler handler) {
		// log.debug("removeHandler: " + handler.accessId);
		synchronized (handlerList) {
			for (int i = 0, n = handlerList.size(); i < n; i++) {
				if (handler == handlerList.get(i)) {
					handlerList.remove(i);
					break;
				}
			}
		}
	}

	/** 移除一个指定的线程休眠包 */
	private ThreadAccessHandler removeHandler(int id) {
		synchronized (handlerList) {
			for (int i = 0, n = handlerList.size(); i < n; i++) {
				ThreadAccessHandler handle = handlerList.get(i);
				if (handle != null && id == handle.accessId) {
					// log.debug("removeHandler: " + id);
					return handlerList.remove(i);
				}
			}
		}
		return null;
	}

	/** 异步线程访问方法 */
	public void access4nonwait(ThreadAccessHandler handler) {
		addHandler(handler);
		handler.handle();
	}

	/** 线程访问方法 */
	public Object access(ThreadAccessHandler handler) {
		return access(handler, TIMEOUT);
	}

	/** 线程访问方法 */
	public Object access(ThreadAccessHandler handler, int timeout) {
		addHandler(handler);
		handler.handle();
		synchronized (handler) {
			if (handler.accessResult == NONE) {
				try {
					handler.wait(timeout);
				} catch (InterruptedException e) {
					e.printStackTrace();
				} finally {
					// 超时，移除等待对象
					if (handler.accessResult == NONE) {
						removeHandler(handler);
					}
				}
			}
		}
		return handler.accessResult;
	}

	/** 线程唤醒方法 */
	public void notify(int id, Object obj) {
		ThreadAccessHandler handler = removeHandler(id);
		if (handler == null) {
			Thread.dumpStack();
			return;
		}
		handler.accessResult = obj;
		handler.doNotify();
	}

}