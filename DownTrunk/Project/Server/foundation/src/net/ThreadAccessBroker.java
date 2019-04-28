package net;

import java.util.ArrayList;

/**
 * ��˵�����̷߳��ʴ�����
 * 
 */

public final class ThreadAccessBroker implements ThreadAccess {

	/* fields */
	/** �߳����߶�����б� */
	private ArrayList<ThreadAccessHandler> handlerList = new ArrayList<ThreadAccessHandler>();

	/* methods */
	// /** ���һ��ָ�������ŵ��߳����߰� */
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
	/** ����һ��ָ�����߳����߰� */
	private void addHandler(ThreadAccessHandler handler) {
		// log.debug("addHandler: " + handler.accessId);
		synchronized (handlerList) {
			handlerList.add(handler);
		}
	}

	/** �Ƴ�һ��ָ�����߳����߰� */
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

	/** �Ƴ�һ��ָ�����߳����߰� */
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

	/** �첽�̷߳��ʷ��� */
	public void access4nonwait(ThreadAccessHandler handler) {
		addHandler(handler);
		handler.handle();
	}

	/** �̷߳��ʷ��� */
	public Object access(ThreadAccessHandler handler) {
		return access(handler, TIMEOUT);
	}

	/** �̷߳��ʷ��� */
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
					// ��ʱ���Ƴ��ȴ�����
					if (handler.accessResult == NONE) {
						removeHandler(handler);
					}
				}
			}
		}
		return handler.accessResult;
	}

	/** �̻߳��ѷ��� */
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