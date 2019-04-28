package config;

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Method;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.ErrorPrint;
import util.Tools;

public class Listner implements Runnable, UncaughtExceptionHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(Listner.class);
	
	private WatchService service;
	private Class<?> loader;
	private HashMap<String, Long> lastModifyTime = new HashMap<>();

	public Listner(WatchService service, String rootPath) throws ClassNotFoundException {
		this.service = service;
		this.loader = Class.forName("config.ConfigLoader");
	}

	public void run() {
		try {
			while (true) {
				String cfg = "";
				WatchKey watchKey = null;
				try {
					watchKey = service.take();
					List<WatchEvent<?>> watchEvents = watchKey.pollEvents();
					for (WatchEvent<?> event : watchEvents) {
						cfg = event.context().toString();
						String methodName = ConfigLoader.reloadMapping.get(cfg);
						if (Tools.isEmptyString(methodName)) {
							continue;
						}
						
						if (lastModifyTime.get(methodName) != null) {
							long oldTime = lastModifyTime.get(methodName);
							long time = Calendar.getInstance().getTimeInMillis();
							if (oldTime > time) {
								continue;
							}
						}
						Thread.sleep(Tools.random(30, 300));
						Method method = loader.getMethod(methodName);
						method.invoke(null);
						lastModifyTime.put(methodName, Calendar.getInstance().getTimeInMillis() + 100);
					}
				} catch (Exception e) {
					ErrorPrint.print(e);
					logger.info("{}‘ÿ»Î ß∞‹°£", cfg);
				} finally {
					if (watchKey != null) {
						watchKey.reset();
					}
				}
			}
		} finally {
			try {
				service.close();
			} catch (IOException e) {
				ErrorPrint.print(e);
			}
		}
	}
	
	@Override
	public void uncaughtException(Thread t, Throwable e) {
		ErrorPrint.print(e);
		System.err.println("	at sys.manager.Listner(Listner.class:0)");
	}
}
