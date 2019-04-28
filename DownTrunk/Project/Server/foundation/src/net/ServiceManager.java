package net;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class ServiceManager {
	
	/** Ĭ�Ϸ���id */
	public static final byte RESPONSE_ID = 0;

	/** service���� */
	public HashMap<Integer, IService> services;
	
	public ServiceManager() {
		services = new HashMap<Integer, IService>();
	}

	/**
	 * ���һ��service
	 * 
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 */
	public void addService(int id, IService service)
			throws InstantiationException, IllegalAccessException,
			ClassNotFoundException {
		this.services.put(id, service);
	}


	/** ����ģ��id�õ������� */
	public IService getService(int id) {
		return services.get(id);

	}

	/**
	 * �ر�ÿ������
	 */
	public void destory() {
		Iterator<Entry<Integer, IService>> iterator = services.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Integer, IService> entry = iterator.next();
			IService service = entry.getValue();

			service.destroy();
		}
	}

}
