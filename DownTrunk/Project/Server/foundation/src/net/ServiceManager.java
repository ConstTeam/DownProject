package net;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class ServiceManager {
	
	/** 默认返回id */
	public static final byte RESPONSE_ID = 0;

	/** service集合 */
	public HashMap<Integer, IService> services;
	
	public ServiceManager() {
		services = new HashMap<Integer, IService>();
	}

	/**
	 * 添加一个service
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


	/** 根据模块id得到服务类 */
	public IService getService(int id) {
		return services.get(id);

	}

	/**
	 * 关闭每个服务
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
