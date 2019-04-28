package message.web;

import java.util.HashMap;

public class WebServiceConfig {

	protected HashMap<Integer, Class<?>> clientServices = new HashMap<>();

	private static WebServiceConfig instance;

	public static WebServiceConfig getInstance() {
		if (instance == null) {
			instance = new WebServiceConfig();
		}

		return instance;
	}

	public HashMap<Integer, Class<?>> getServices() {
		return clientServices;
	}

}
