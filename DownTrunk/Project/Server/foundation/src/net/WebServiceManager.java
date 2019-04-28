package net;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class WebServiceManager {
	
	/** Ĭ�Ϸ���id */
	public static final byte RESPONSE_ID = 0;

	/** service���� */
	public HashMap<Integer, IWebService> services;
	
	public WebServiceManager() {
		services = new HashMap<Integer, IWebService>();
	}

	/**
	 * ���һ��service
	 * 
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public void addService(int id, IWebService service)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		this.services.put(id, service);
	}


	/** ����ģ��id�õ������� */
	public IWebService getService(int id) {
		return services.get(id);

	}

	/**
	 * �ر�ÿ������
	 */
	public void destory() {
		Iterator<Entry<Integer, IWebService>> iterator = services.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Integer, IWebService> entry = iterator.next();
			IWebService service = entry.getValue();

			service.destroy();
		}
	}

}
