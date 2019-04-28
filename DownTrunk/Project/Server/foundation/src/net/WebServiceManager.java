package net;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class WebServiceManager {
	
	/** 默认返回id */
	public static final byte RESPONSE_ID = 0;

	/** service集合 */
	public HashMap<Integer, IWebService> services;
	
	public WebServiceManager() {
		services = new HashMap<Integer, IWebService>();
	}

	/**
	 * 添加一个service
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


	/** 根据模块id得到服务类 */
	public IWebService getService(int id) {
		return services.get(id);

	}

	/**
	 * 关闭每个服务
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
