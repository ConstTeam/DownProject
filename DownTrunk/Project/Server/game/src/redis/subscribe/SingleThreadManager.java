package redis.subscribe;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SingleThreadManager {
	
	private static ExecutorService instance;
	
	public static ExecutorService getInstance() {
		if (instance == null) {
			instance = Executors.newSingleThreadExecutor();
		}

		return instance;
	}
}

